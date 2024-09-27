package scripts.utils

import java.time.Instant
import org.tribot.script.sdk.Skill

class ExperienceTracker {
  private val skillTrackers = mutableMapOf<Skill, SkillTracker>()
  private var lastUpdateTime: Instant = Instant.now()
  private val newSkillListeners = mutableListOf<(Skill) -> Unit>()
  private val inactivityThreshold: Long = 60 * 3
  private val inactivityStatusListeners = mutableMapOf<Skill, MutableList<(Boolean) -> Unit>>()

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
        inactivityStatusListeners[skill]?.forEach { it(isNowInactive) }
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
    return skill.getCurrentXpToNextLevel()
  }

  fun getTimeToNextLevel(skill: Skill): Double? {
    val tracker = skillTrackers[skill] ?: return null
    val stats = tracker.getStats()
    val xpToNextLevel = skill.getCurrentXpToNextLevel()
    return if (stats.xpPerHour > 0) {
      (xpToNextLevel / stats.xpPerHour) * 3600 // Convert hours to seconds
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
    private var lastActiveTime: Instant = initialTime
    private var xpHistory: MutableList<Pair<Instant, Int>> = mutableListOf()
    var isNewlyTracked: Boolean = true
    var wasInactive: Boolean = false

    fun update(newXp: Int, currentTime: Instant): Boolean {
      val hasGainedXp = newXp > currentXp
      if (hasGainedXp) {
        lastXp = currentXp
        currentXp = newXp
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
      val duration = java.time.Duration.between(startTime, Instant.now())
      val xpPerHour =
          if (duration.seconds > 0) {
            totalChange.toDouble() / duration.seconds * 3600
          } else {
            0.0
          }

      return SkillStats(
          skill = skill,
          totalChange = totalChange,
          recentChange = recentChange,
          xpPerHour = xpPerHour,
          xpHistory = xpHistory.toList(),
          isInactive = isInactive(Instant.now()))
    }
  }

  data class SkillStats(
      val skill: Skill,
      val totalChange: Int,
      val recentChange: Int,
      val xpPerHour: Double,
      val xpHistory: List<Pair<Instant, Int>>,
      val isInactive: Boolean
  )

  // Add this new method to register listeners
  fun addNewSkillListener(listener: (Skill) -> Unit) {
    newSkillListeners.add(listener)
  }

  // Add this new method to remove listeners if needed
  fun removeNewSkillListener(listener: (Skill) -> Unit) {
    newSkillListeners.remove(listener)
  }

  // Add these new methods to manage inactivity status listeners
  fun addInactivityStatusListener(skill: Skill, listener: (Boolean) -> Unit) {
    inactivityStatusListeners.getOrPut(skill) { mutableListOf() }.add(listener)
  }

  fun removeInactivityStatusListener(skill: Skill, listener: (Boolean) -> Unit) {
    inactivityStatusListeners[skill]?.remove(listener)
  }
}
