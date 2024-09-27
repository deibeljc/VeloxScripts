package scripts.utils

import java.time.Instant
import org.tribot.script.sdk.Skill

class ExperienceTracker {
  private val skillTrackers = mutableMapOf<Skill, SkillTracker>()
  private var lastUpdateTime: Instant = Instant.now()

  fun update() {
    val currentTime = Instant.now()
    Skill.values().forEach { skill ->
      val currentXp = skill.getXp()
      val tracker = skillTrackers.getOrPut(skill) { SkillTracker(skill, currentXp) }
      tracker.update(currentXp, currentTime)
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

  private inner class SkillTracker(val skill: Skill, initialXp: Int) {
    private var startXp: Int = initialXp
    private var lastXp: Int = initialXp
    private var currentXp: Int = initialXp
    private var startTime: Instant = Instant.now()
    private var xpHistory: MutableList<Pair<Instant, Int>> = mutableListOf()

    fun update(newXp: Int, currentTime: Instant) {
      if (newXp != currentXp) {
        lastXp = currentXp
        currentXp = newXp
        xpHistory.add(currentTime to newXp)
        // Keep only the last hour of data
        xpHistory =
            xpHistory
                .dropWhile { it.first.isBefore(currentTime.minusSeconds(3600)) }
                .toMutableList()
      }
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
          xpHistory = xpHistory.toList())
    }
  }

  data class SkillStats(
      val skill: Skill,
      val totalChange: Int,
      val recentChange: Int,
      val xpPerHour: Double,
      val xpHistory: List<Pair<Instant, Int>>
  )
}
