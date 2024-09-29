package scripts

import java.time.Instant
import org.tribot.script.sdk.Skill

class ExperienceTracker {
  private val skillTrackers = mutableMapOf<Skill, SkillTracker>()
  private var lastUpdateTime: Instant = Instant.now()
  private val newSkillListeners = mutableListOf<(Skill) -> Unit>()
  private val inactivityThreshold: Long = 60 * 3
  private val activityStatusListeners = mutableMapOf<Skill, MutableList<(Boolean) -> Unit>>()

  fun update() {
    val currentTime = Instant.now()
    Skill.values().forEach { skill ->
      val currentXp = skill.getXp()
      val tracker = skillTrackers.getOrPut(skill) { SkillTracker(skill, currentXp, currentTime) }
      val hasGainedXp = tracker.update(currentXp, currentTime)

      // Notify listeners when a new skill is tracked and has gained XP
      if (hasGainedXp && tracker.isNewlyTracked) {
        newSkillListeners.forEach { listener -> listener(skill) }
        tracker.isNewlyTracked = false
      }

      // Check for inactivity status change and notify listeners
      val wasInactive = tracker.wasInactive
      val isNowInactive = tracker.isInactive(currentTime)
      if (wasInactive != isNowInactive) {
        tracker.wasInactive = isNowInactive
        activityStatusListeners[skill]?.forEach { it(isNowInactive) }
      }
    }
    lastUpdateTime = currentTime
  }

  fun getChangedSkills(): List<Skill> {
    return skillTrackers.values.filter { it.hasChanged() }.map { it.skill }
  }

  fun getSkillStats(skill: Skill): SkillStats? {
    return skillTrackers[skill]?.getStats()
  }

  fun getXpToNextLevel(skill: Skill): Int? {
    return skill.currentXpToNextLevel
  }

  fun getTimeToNextLevel(skill: Skill): Double? {
    val tracker = skillTrackers[skill] ?: return null
    val stats = tracker.getStats()
    val xpToNextLevel = skill.currentXpToNextLevel
    return if (stats.xpPerHour > 0) {
      val activeTime = stats.activeTimeInSeconds
      if (activeTime > 0) {
        (xpToNextLevel / (stats.totalChange.toDouble() / activeTime)) // Calculate time in seconds
      } else {
        null
      }
    } else {
      null
    }
  }

  fun isSkillInactive(skill: Skill): Boolean {
    return skillTrackers[skill]?.isInactive(Instant.now()) ?: false
  }

  private inner class SkillTracker(val skill: Skill, initialXp: Int, initialTime: Instant) {
    private var startXp: Int = initialXp
    private var lastXp: Int = initialXp
    private var currentXp: Int = initialXp
    private var startTime: Instant = initialTime
    private var startLevel: Int = skill.getCurrentLevel()
    private var currentLevel: Int = startLevel
    private var levelsGained: Int = 0
    private var lastActiveTime: Instant = initialTime
    private var xpHistory: MutableList<Pair<Instant, Int>> = mutableListOf()
    var isNewlyTracked: Boolean = true
    var wasInactive: Boolean = false

    fun update(newXp: Int, currentTime: Instant): Boolean {
      val hasGainedXp = newXp > currentXp
      if (hasGainedXp) {
        lastXp = currentXp
        currentXp = newXp
        val newLevel = skill.getCurrentLevel()
        if (newLevel > currentLevel) {
          levelsGained += newLevel - currentLevel
          currentLevel = newLevel
        }
        wasInactive = isInactive(currentTime)
        lastActiveTime = currentTime
        xpHistory.add(currentTime to newXp)
        // Keep only the last hour of data
        xpHistory =
            xpHistory
                .dropWhile { it.first.isBefore(currentTime.minusSeconds(3600)) }
                .toMutableList()
      }
      return hasGainedXp
    }

    fun isInactive(currentTime: Instant): Boolean {
      return java.time.Duration.between(lastActiveTime, currentTime).seconds > inactivityThreshold
    }

    fun hasChanged(): Boolean = currentXp != lastXp

    fun getStats(): SkillStats {
      val totalChange = currentXp - startXp
      val recentChange = currentXp - lastXp
      val activeTime = calculateActiveTime()
      val xpPerHour =
          if (activeTime > 0) {
            totalChange.toDouble() / activeTime * 3600
          } else {
            0.0
          }

      return SkillStats(
          skill = skill,
          totalChange = totalChange,
          recentChange = recentChange,
          xpPerHour = xpPerHour,
          xpHistory = xpHistory.toList(),
          isInactive = isInactive(Instant.now()),
          activeTimeInSeconds = activeTime,
          levelsGained = levelsGained)
    }

    private fun calculateActiveTime(): Long {
      var activeTime = 0L
      var lastXpTime = startTime

      for ((time, _) in xpHistory) {
        val timeSinceLastXp = java.time.Duration.between(lastXpTime, time).seconds
        activeTime += minOf(timeSinceLastXp, inactivityThreshold)
        lastXpTime = time
      }

      val timeSinceLastXp = java.time.Duration.between(lastXpTime, Instant.now()).seconds
      activeTime += minOf(timeSinceLastXp, inactivityThreshold)

      return activeTime
    }
  }

  data class SkillStats(
      val skill: Skill,
      val totalChange: Int,
      val recentChange: Int,
      val xpPerHour: Double,
      val xpHistory: List<Pair<Instant, Int>>,
      val isInactive: Boolean,
      val activeTimeInSeconds: Long,
      val levelsGained: Int
  )

  fun addNewSkillListener(listener: (Skill) -> Unit) {
    newSkillListeners.add(listener)
  }

  fun removeNewSkillListener(listener: (Skill) -> Unit) {
    newSkillListeners.remove(listener)
  }

  fun addActivityStatusListener(skill: Skill, listener: (Boolean) -> Unit) {
    activityStatusListeners.getOrPut(skill) { mutableListOf() }.add(listener)
  }

  fun removeActivityStatusListener(skill: Skill, listener: (Boolean) -> Unit) {
    activityStatusListeners[skill]?.remove(listener)
  }
}
