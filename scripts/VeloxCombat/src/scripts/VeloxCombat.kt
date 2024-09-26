package scripts

import java.awt.Color
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.ScriptListening
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintLocation
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.behaviors.combatState
import scripts.behaviors.stateMachine
import scripts.statemachine.StateMachineVisualizer

@TribotScriptManifest(
    name = "VeloxCombat", author = "Dibes", category = "Combat", description = "A combat script.")
class VeloxCombat : TribotScript {
  private var tree: IBehaviorNode? = null
  private var shouldRun = true
  val template = PaintTextRow.builder().background(Color.blue.darker()).build()
  var lastFrameTime: Long = System.currentTimeMillis()
  var paint =
      BasicPaintTemplate.builder()
          .row(PaintRows.runtime(template.toBuilder()))
          .row(PaintRows.scriptName(template.toBuilder()))
          .row(
              template
                  .toBuilder()
                  .label("Script State")
                  .value(VeloxState.getStateHistories())
                  .build())
          .row(
              template
                  .toBuilder()
                  .label("Looptime")
                  .value((System.currentTimeMillis() - lastFrameTime).toString())
                  .build())
          .location(PaintLocation.BOTTOM_LEFT_VIEWPORT)
          .build()

  override fun configure(config: ScriptConfig) {
    config.isRandomsAndLoginHandlerEnabled = true
  }

  private fun setupPaint(): BasicPaintTemplate {
    paint =
        BasicPaintTemplate.builder()
            .row(PaintRows.runtime(template.toBuilder()))
            .row(PaintRows.scriptName(template.toBuilder()))
            .row(
                template
                    .toBuilder()
                    .label("Script State")
                    .value(VeloxState.getStateHistories())
                    .build())
            .row(
                template
                    .toBuilder()
                    .label("Looptime")
                    .value((System.currentTimeMillis() - lastFrameTime).toString())
                    .build())
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
            .location(PaintLocation.BOTTOM_LEFT_VIEWPORT)
            .build()

    return paint
  }

  override fun execute(args: String) {
    val renderer = StateMachineVisualizer(stateMachine)

    Painting.addPaint { g ->
      paint.render(g)
      renderer.render(g, 1000, 1000)
    }
    ScriptListening.addPreEndingListener(
        Runnable {
          shouldRun = false
          Log.info("Script is ending")
        })

    stateMachine.setInitialState(combatState)

    while (shouldRun) {
      paint = setupPaint()
      stateMachine.step()
      lastFrameTime = System.currentTimeMillis()
    }
  }
}
