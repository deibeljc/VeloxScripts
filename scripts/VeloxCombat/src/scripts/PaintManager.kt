package scripts

import java.awt.Color
import java.awt.Graphics
import kotlinx.coroutines.*
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintLocation
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import scripts.behaviors.stateMachine
import scripts.frameworks.StateTreeVisualizer

class PaintManager(private val experienceTracker: ExperienceTracker) {
  private val template = PaintTextRow.builder().background(Color.blue.darker()).build()
  private var paint: BasicPaintTemplate.BasicPaintTemplateBuilder = BasicPaintTemplate.builder()
  private val renderStack = RenderStack(10, 10, RenderStack.Direction.VERTICAL)
  private val TARGET_FPS = 60
  private val FRAME_TIME = 1000L / TARGET_FPS
  private val stateTreeVisualizer = StateTreeVisualizer(stateMachine)

  fun setupPaint() {
    stateTreeVisualizer.setupScrollListener()

    Painting.addPaint { g ->
      paint.build().render(g)
      renderStack.renderStack(g)
      stateTreeVisualizer.render(g, GameState.getViewportWidth(), GameState.getViewportHeight())
    }
  }

  fun startPaintJob(coroutineScope: CoroutineScope): Job {
    return coroutineScope.launch {
      while (isActive) {
        val startTime = System.currentTimeMillis()

        updatePaint()
        renderStack.animateRenderStack(FRAME_TIME)

        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = FRAME_TIME - elapsedTime

        if (remainingTime > 0) {
          delay(remainingTime)
        }
      }
    }
  }

  private fun updatePaint() {
    paint = setupPaintBuilder()
  }

  private fun setupPaintBuilder(): BasicPaintTemplate.BasicPaintTemplateBuilder {
    paint =
        BasicPaintTemplate.builder()
            .row(PaintRows.runtime(template.toBuilder()))
            .row(PaintRows.scriptName(template.toBuilder()))
            .row(
                template
                    .toBuilder()
                    .label("Location")
                    .value(Locations.getBestTrainingArea().name)
                    .build())
            .row(
                template
                    .toBuilder()
                    .label("Monster to fight")
                    .value(Locations.getBestTrainingArea().monsters.joinToString(", "))
                    .build())
            .row(
                template
                    .toBuilder()
                    .label("Total Earnings")
                    .value("${EconomyTracker.getInstance().getTotalEarnings()} gp")
                    .build())
            .row(
                template
                    .toBuilder()
                    .label("GP/hour")
                    .value("${EconomyTracker.getInstance().getGpPerHour()} gp/h")
                    .build())
            .location(PaintLocation.BOTTOM_LEFT_VIEWPORT)

    return paint
  }

  fun pushToRenderStack(
      key: String,
      renderFunction: (Graphics, Float, Float, Float) -> Unit,
      width: Int,
      height: Int
  ) {
    renderStack.pushToRenderStack(key, renderFunction, width, height)
  }

  fun removeFromRenderStack(key: String) {
    renderStack.removeFromRenderStack(key)
  }
}
