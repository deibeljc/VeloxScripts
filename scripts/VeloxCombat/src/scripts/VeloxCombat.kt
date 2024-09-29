package scripts

import dax.api_lib.DaxWalker
import dax.teleports.Teleport
import java.awt.Color
import java.awt.Graphics
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.ScriptListening
import org.tribot.script.sdk.Skill
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.behaviors.setupState
import scripts.behaviors.stateMachine
import scripts.gui.VeloxCombatGUIState
import scripts.utils.viz.ProgressBar

@TribotScriptManifest(
    name = "VeloxCombat", author = "Dibes", category = "Combat", description = "A combat script.")
class VeloxCombat : TribotScript {
  // Properties
  private val experienceTracker = ExperienceTracker()
  private var lastFrameTime: Long = System.currentTimeMillis()
  private val paintJob = AtomicReference<Job?>(null)
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private var startTime: Long = System.currentTimeMillis()
  private var lastUpdateTime: Long = 0

  private lateinit var paintManager: PaintManager

  override fun configure(config: ScriptConfig) {
    config.isRandomsAndLoginHandlerEnabled = true
  }

  override fun execute(args: String) {
    Log.info("VeloxCombat executing with args: $args")
    setupScript(args)
    runMainLoop()
    cleanup()
    VeloxCombatGUIState.stopRunning()
  }

  private fun setupScript(args: String) {
    parseArgs(args)
    setupTeleports()
    setupStateMachine()
    setupPaintManager()
    setupScriptListeners()
    VeloxCombatGUIState.launchGUI()
  }

  private fun setupTeleports() {
    Teleport.values().forEach { DaxWalker.blacklistTeleports(it) }
  }

  private fun setupStateMachine() {
    startTime = System.currentTimeMillis()
    lastUpdateTime = startTime
    stateMachine.setInitialState(setupState)
  }

  private fun setupPaintManager() {
    paintManager = PaintManager(experienceTracker)
    paintManager.setupPaint()
    paintJob.set(paintManager.startPaintJob(coroutineScope))
    addSkillProgressBars()
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
      while (VeloxCombatGUIState.shouldRun && !VeloxCombatGUIState.isScriptStopping.value) {
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

  private fun addSkillProgressBars() {
    val skillProgressBars = mutableMapOf<Skill, (Graphics, Float, Float, Float) -> Unit>()
    val height = 30

    fun addProgressBarForSkill(skill: Skill) {
      val progressBarRender: (Graphics, Float, Float, Float) -> Unit = { g, x, y, alpha ->
        val stats = experienceTracker.getSkillStats(skill)
        val xpToNextLevel = experienceTracker.getXpToNextLevel(skill)
        val timeToNextLevel = experienceTracker.getTimeToNextLevel(skill)

        if (stats != null && xpToNextLevel != null && timeToNextLevel != null) {
          val progress = skill.xpPercentToNextLevel.toDouble() / 100.0
          val hours = (timeToNextLevel / 3600).toInt()
          val minutes = ((timeToNextLevel % 3600) / 60).toInt()
          val seconds = (timeToNextLevel % 60).toInt()
          val levelsGained = stats.levelsGained
          val label = when {
            hours > 0 -> "${skill.name} (${skill.currentLevel}, +$levelsGained): ${hours}h ${minutes}m ${seconds}s"
            else -> "${skill.name} (${skill.currentLevel}, +$levelsGained): ${minutes}m ${seconds}s"
          }

          ProgressBar(180, height, progress, label, Color.BLACK, Color.GREEN).draw(g, x, y, alpha)
        }
      }

      skillProgressBars[skill] = progressBarRender
      paintManager.pushToRenderStack(skill.name, progressBarRender, 200, height)

      // Add inactivity status listener
      experienceTracker.addActivityStatusListener(skill) { isInactive ->
        if (isInactive) {
          paintManager.removeFromRenderStack(skill.name)
          Log.info("Removed ${skill.name} from render stack due to inactivity")
        } else {
          paintManager.pushToRenderStack(skill.name, skillProgressBars[skill]!!, 200, height)
          Log.info("Added ${skill.name} back to render stack due to activity")
        }
      }
    }

    // Add listener for new skills
    experienceTracker.addNewSkillListener { skill ->
      Log.info("Trying to track ${skill.name}")
      addProgressBarForSkill(skill)
    }

    coroutineScope.launch {
      while (VeloxCombatGUIState.shouldRun) {
        experienceTracker.update()
        delay(1000) // Update every second
      }
    }
  }
}
