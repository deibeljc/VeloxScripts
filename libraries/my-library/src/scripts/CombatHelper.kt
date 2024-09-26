package scripts

import kotlin.jvm.optionals.getOrNull
import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Skill
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Npc

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
      val attack = Skill.ATTACK.currentLevel
      val defence = Skill.DEFENCE.currentLevel
      val strength = Skill.STRENGTH.currentLevel

      val combatStyle =
          when {
            strength <= attack -> Combat.AttackStyle.AGGRESSIVE
            attack < strength -> Combat.AttackStyle.ACCURATE
            defence < minOf(attack, strength) -> Combat.AttackStyle.DEFENSIVE
            else -> Combat.AttackStyle.CONTROLLED // Fallback to controlled if all are roughly equal
          }
      return combatStyle
    }

    /** Fight the nearest enemy. */
    fun fightNearestEnemy(monsterArea: MonsterArea?): Boolean {
      val npc = getNearestEnemy(monsterArea)

      npc?.interact("Attack")

      return Waiting.waitUntil { isInCombat() }
    }

    /** Get health of my player */
    fun getPlayerHealth(): Int {
      return MyPlayer.getCurrentHealth()
    }
  }
}
