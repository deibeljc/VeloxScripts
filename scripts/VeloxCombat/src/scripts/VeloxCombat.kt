package scripts

import dax.api_lib.DaxWalker
import dax.teleports.Teleport
import java.awt.Color
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.ScriptListening
import org.tribot.script.sdk.Waiting
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
import scripts.utils.ExperienceTracker
import scripts.utils.LineChart

@TribotScriptManifest(
    name = "VeloxCombat", author = "Dibes", category = "Combat", description = "A combat script.")
class VeloxCombat : TribotScript {
  // Constants
  private val PAINT_UPDATE_INTERVAL = 200L
  private val XP_UPDATE_INTERVAL = 60000L
  private val MAX_XP_DATA_POINTS = 60

  // Properties
  private val experienceTracker = ExperienceTracker()
  private var chart: LineChart? = null
  private val template = PaintTextRow.builder().background(Color.blue.darker()).build()
  private var lastFrameTime: Long = System.currentTimeMillis()
  private val paintJob = AtomicReference<Job?>(null)
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private val xpGainedPerHour = mutableListOf<Pair<Double, Double>>()
  private var startTime: Long = System.currentTimeMillis()
  private var lastUpdateTime: Long = 0

  private lateinit var paint: BasicPaintTemplate.BasicPaintTemplateBuilder

  override fun configure(config: ScriptConfig) {
    config.isRandomsAndLoginHandlerEnabled = true
  }

  override fun execute(args: String) {
    setupScript(args)
    runMainLoop()
    cleanup()
  }

  private fun setupScript(args: String) {
    parseArgs(args)
    setupTeleports()
    setupStateMachine()
    setupPaintJob()
    setupScriptListeners()
    VeloxCombatGUIState.launchGUI()
  }

  private fun setupTeleports() {
    Teleport.values().forEach { DaxWalker.blacklistTeleports(it) }
  }

  private fun setupStateMachine() {
    startTime = System.currentTimeMillis()
    lastUpdateTime = startTime
    stateMachine.setInitialState(initState)
  }

  private fun setupPaintJob() {
    Painting.addPaint { g -> paint.build().render(g) }

    paintJob.set(
        coroutineScope.launch {
          while (isActive) {
            updatePaint()
            delay(PAINT_UPDATE_INTERVAL)
          }
        })
  }

  private fun setupScriptListeners() {
    ScriptListening.addPreEndingListener(
        Runnable {
          VeloxCombatGUIState.closeGUI()
          VeloxCombatGUIState.stopRunning()
          Log.info("Script is ending")
        })
  }

  private fun runMainLoop() {
    runBlocking {
      while (VeloxCombatGUIState.shouldRun) {
        if (VeloxCombatGUIState.isRunning.value) {
          performScriptIteration()
        } else {
          Waiting.wait(50)
        }
      }
    }
  }

  private fun performScriptIteration() {
    experienceTracker.update()
    updateXpGainedPerHour()
    stateMachine.step()
    lastFrameTime = System.currentTimeMillis()
  }

  private fun cleanup() {
    paintJob.get()?.cancel()
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

  private fun updateXpGainedPerHour() {
    val currentTime = System.currentTimeMillis()
    val elapsedMinutes = (currentTime - lastUpdateTime) / 60000.0

    // Update every minute
    if (elapsedMinutes >= 1.0) {
      val totalRuntime = (currentTime - startTime) / 3600000.0 // Convert to hours
      val totalXpGained =
          experienceTracker.getChangedSkills().sumOf { skill ->
            experienceTracker.getSkillStats(skill)?.totalChange ?: 0
          }
      val xpPerHour = if (totalRuntime > 0) totalXpGained / totalRuntime else 0.0
      xpGainedPerHour.add(totalRuntime to xpPerHour)

      // Keep only the last 60 data points (1 hour of data)
      if (xpGainedPerHour.size > MAX_XP_DATA_POINTS) {
        xpGainedPerHour.removeAt(0)
      }

      lastUpdateTime = currentTime
    }
  }

  private fun updatePaint() {
    paint = setupPaint()
  }

  private fun setupPaint(): BasicPaintTemplate.BasicPaintTemplateBuilder {
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

    experienceTracker.getChangedSkills().forEach { skill ->
      // Compute the various stats per skill into a string
      val xpPerHour = experienceTracker.getSkillStats(skill)?.xpPerHour
      val xpToNextLevel = experienceTracker.getXpToNextLevel(skill)
      val xpPerHourString = if (xpPerHour != null) "%.2f".format(xpPerHour) else "N/A"
      val xpToNextLevelString = xpToNextLevel?.let { "%d".format(it) } ?: "N/A"
      val timeToNextLevel = experienceTracker.getTimeToNextLevel(skill)
      val timeToNextLevelString =
          if (timeToNextLevel != null) {
            val minutes = (timeToNextLevel / 60).toInt()
            val seconds = (timeToNextLevel % 60).toInt()
            "${minutes}m ${seconds}s"
          } else "N/A"
      // Combine into a single string
      val xpStatsString =
          "XP/Hour: $xpPerHourString, XP to Next Level: $xpToNextLevelString, Time to Next Level: $timeToNextLevelString"

      chart?.setData(xpGainedPerHour)
      paint.row(template.toBuilder().label(skill.name).value(xpStatsString).build())
    }

    return paint
  }
}
