package scripts

import kotlinx.coroutines.runBlocking
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.ScriptListening
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.IBehaviorNode
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintLocation
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.behaviors.initState
import scripts.behaviors.stateMachine
import scripts.gui.VeloxCombatGUIState
import scripts.statemachine.StateMachineVisualizer
import java.awt.Color

@TribotScriptManifest(
  name = "VeloxCombat", author = "Dibes", category = "Combat", description = "A combat script."
)
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
          .build()
      )
      .row(
        template
          .toBuilder()
          .label("Looptime")
          .value((System.currentTimeMillis() - lastFrameTime).toString())
          .build()
      )
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
        .build()

    return paint
  }

  private fun parseArgs(args: String?) {
    if (args.isNullOrEmpty()) return

    Log.info("Parsing args: $args")

    args.split(",").forEach { arg ->
      val parts = arg.split(":")
      if (parts.size == 2) {
        val (setting, value) = parts.map { it.trim() }
        when (setting.lowercase()) {
          "eathealth" ->
            VeloxCombatGUIState.eatHealthPercentage.value = value.toFloatOrNull() ?: 50f

          "eattofull" ->
            VeloxCombatGUIState.eatToFull.value = value.toBooleanStrictOrNull() ?: false
        }
      }
    }
  }

  override fun execute(args: String) {
    // Parse args and update GUI state
    parseArgs(args)

    val renderer = StateMachineVisualizer(stateMachine)

    // Set up paint rendering
    Painting.addPaint { g ->
      paint.render(g)
      renderer.render(g, 1000, 1000)
    }

    ScriptListening.addPreEndingListener(
      Runnable {
        VeloxCombatGUIState.closeGUI()
        VeloxCombatGUIState.stopRunning()
        Log.info("Script is ending")
      })

    stateMachine.setInitialState(initState)

    // Launch Jetpack Compose GUI
    VeloxCombatGUIState.launchGUI()

    // Main script loop
    runBlocking {
      while (VeloxCombatGUIState.shouldRun) {
        if (VeloxCombatGUIState.isRunning.value) {
          paint = setupPaint()
          stateMachine.step()
          lastFrameTime = System.currentTimeMillis()
        } else {
          // Don't needlessly eat CPU cycles while waiting
          Waiting.wait(50)
        }
      }
    }
  }
}
