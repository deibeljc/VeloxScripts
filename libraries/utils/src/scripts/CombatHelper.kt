package scripts

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Skill
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Npc
import scripts.gui.VeloxCombatGUIState
import kotlin.jvm.optionals.getOrNull

class CombatHelper {
  companion object {
    /**
     * Checks if the player is in combat.
     *
     * @return true if the player is in combat, false otherwise
     */
    fun isInCombat(): Boolean {
      val player = MyPlayer.get().orElse(null)
      val interacting = player.isInteracting

//      val anythingInteractingWithMe = Query.npcs().isInteractingWithMe().actionContains("Attack").count() > 0
//
//      Log.info("NPCs interacting with me: ${Query.npcs().isInteractingWithMe().actionContains("Attack").toList()}")

      return interacting
    }

    /**
     * Get the nearest enemy to the player.
     *
     * @return the nearest enemy to the player
     */
    private fun getNearestEnemy(monsterArea: MonsterArea?): Npc? {
      val npcNameToSearch = monsterArea?.monsters?.map { it.monsterName }
      var npc =
        Query.npcs()
          .inArea(monsterArea?.area)
          .filter { npcNameToSearch?.contains(it.name) ?: false }
          .isHealthBarNotEmpty()
          .filter(Npc::isValid)
          .isNotBeingInteractedWith()
          .isReachable()
          .findBestInteractable()
          .getOrNull()

      val npcTaretingMe = Query.npcs().isInteractingWithMe().findBestInteractable().getOrNull()

      if (npcTaretingMe?.isValid == true) {
        npc = npcTaretingMe
      }

      return npc
    }

    fun enemyNearby(monsterArea: MonsterArea?): Boolean {
      return getNearestEnemy(monsterArea)?.isValid ?: false
    }

    fun getCombatStyleToUse(): Combat.AttackStyle {
      // Use the preferred combat style from the GUI
      val preferredStyle = VeloxCombatGUIState.preferredCombatStyle.value

      val attack = Skill.ATTACK.currentLevel
      val strength = Skill.STRENGTH.currentLevel
      val defence = Skill.DEFENCE.currentLevel

      val maxLevel = maxOf(attack, strength, defence)
      val minLevel = minOf(attack, strength, defence)

      // If all skills are within 5 levels, use the preferred style
      if (maxLevel - minLevel <= 5) {
        return preferredStyle
      }

      return when (preferredStyle) {
        Combat.AttackStyle.AGGRESSIVE -> {
          if (strength < maxLevel - 5) Combat.AttackStyle.AGGRESSIVE
          else if (attack < maxLevel - 5) Combat.AttackStyle.ACCURATE
          else Combat.AttackStyle.DEFENSIVE
        }

        Combat.AttackStyle.ACCURATE -> {
          if (attack < maxLevel - 5) Combat.AttackStyle.ACCURATE
          else if (strength < maxLevel - 5) Combat.AttackStyle.AGGRESSIVE
          else Combat.AttackStyle.DEFENSIVE
        }

        Combat.AttackStyle.DEFENSIVE -> {
          if (defence < maxLevel - 5) Combat.AttackStyle.DEFENSIVE
          else if (strength < maxLevel - 5) Combat.AttackStyle.AGGRESSIVE
          else Combat.AttackStyle.ACCURATE
        }

        Combat.AttackStyle.CONTROLLED -> preferredStyle
        else -> {
          // If no specific condition is met, balance the skills
          val skillDifference = 10
          when {
            strength <= attack - skillDifference || strength <= defence - skillDifference ->
              Combat.AttackStyle.AGGRESSIVE

            attack <= strength - skillDifference || attack <= defence - skillDifference ->
              Combat.AttackStyle.ACCURATE

            defence <= strength - skillDifference || defence <= attack - skillDifference ->
              Combat.AttackStyle.DEFENSIVE

            else -> {
              // If all skills are relatively balanced, prioritize Strength for max hit
              Combat.AttackStyle.AGGRESSIVE
            }
          }
        }
      }
    }

    /** Fight the nearest enemy. */
    fun fightNearestEnemy(monsterArea: MonsterArea?): Npc? {
      val npc = getNearestEnemy(monsterArea)
      npc?.interact("Attack")

      Waiting.waitUntil { npc?.isInteracting == true }

      return npc
    }

    /** Get health of my player */
    fun getPlayerHealth(): Int {
      return MyPlayer.getCurrentHealth()
    }
  }
}
