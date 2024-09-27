package scripts

import kotlinx.coroutines.*
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintLocation
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import java.awt.Color
import java.awt.Graphics

class PaintManager(private val experienceTracker: ExperienceTracker) {
  private val PAINT_UPDATE_INTERVAL = 16L
  private val template = PaintTextRow.builder().background(Color.blue.darker()).build()
  private var paint: BasicPaintTemplate.BasicPaintTemplateBuilder = BasicPaintTemplate.builder()
  private val renderStack = mutableMapOf<String, RenderItem>()
  private val renderOrder = mutableListOf<String>()
  private val maxStackSize = 15
  private val animationDuration = 100L // milliseconds
  private val spacing = 5 // Space between items in pixels
  private val leftMargin = 10 // New: Left margin in pixels
  private val topMargin = 10 // New: Top margin in pixels
  private val TARGET_FPS = 60
  private val FRAME_TIME = 1000L / TARGET_FPS

  data class RenderItem(
    var renderFunction: (Graphics, Float, Float, Float) -> Unit,
    var width: Int,
    var height: Int,
    var y: Float,
    var alpha: Float = 0f,
    var targetY: Float = 0f
  )

  fun setupPaint() {
    Painting.addPaint { g ->
      paint.build().render(g)
      renderStack(g)
    }
  }

  fun startPaintJob(coroutineScope: CoroutineScope): Job {
    return coroutineScope.launch {
      while (isActive) {
        val startTime = System.currentTimeMillis()

        updatePaint()
        animateRenderStack()

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
            .label("Script State")
            .value(VeloxState.getStateHistories())
            .build()
        )
        .row(
          template
            .toBuilder()
            .label("Location")
            .value(Locations.getBestTrainingArea().name)
            .build()
        )
        .row(
          template
            .toBuilder()
            .label("Monster to fight")
            .value(Locations.getBestTrainingArea().monsters.joinToString(", "))
            .build()
        )
        .location(PaintLocation.BOTTOM_LEFT_VIEWPORT)

    return paint
  }

  private fun animateRenderStack() {
    var currentY = 0f
    val animationSpeed = 1f / animationDuration

    renderOrder.forEach { key ->
      val item = renderStack[key] ?: return@forEach
      item.targetY = currentY
      val targetAlpha = 1f

      item.y += (item.targetY - item.y) * animationSpeed * FRAME_TIME
      item.alpha += (targetAlpha - item.alpha) * animationSpeed * FRAME_TIME

      currentY += item.height + spacing
    }
  }

  private fun renderStack(g: Graphics) {
    renderOrder.forEach { key ->
      val item = renderStack[key] ?: return@forEach
      item.renderFunction(g, leftMargin.toFloat(), topMargin + item.y, item.alpha)
    }
  }

  fun pushToRenderStack(
    key: String,
    renderFunction: (Graphics, Float, Float, Float) -> Unit,
    width: Int,
    height: Int
  ) {
    if (key in renderStack) {
      // Update existing item
      renderStack[key]?.apply {
        this.renderFunction = renderFunction
        this.width = width
        this.height = height
      }
    } else {
      // Add new item
      renderStack[key] = RenderItem(renderFunction, width, height, 0f, targetY = 0f)
      renderOrder.add(0, key)
      if (renderOrder.size > maxStackSize) {
        val removedKey = renderOrder.removeAt(renderOrder.size - 1)
        renderStack.remove(removedKey)
      }
    }
  }

  fun removeFromRenderStack(key: String) {
    renderStack.remove(key)
    renderOrder.remove(key)
  }
}
