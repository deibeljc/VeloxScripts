package scripts.behaviors

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.Npc
import scripts.*
import scripts.gui.VeloxCombatGUIState

var lastEnemy: Npc? = null

fun IParentNode.combatNode() = sequence {
  updateState("Combat")
  balanceCombatStyle()
  // resetLastEnemy()
  val trainingArea = Locations.getBestTrainingArea()
  walk(trainingArea.area ?: Area.fromPolygon(), trainingArea.name)
  eatFood()
  selector {
    loot()
    fightNearestEnemy(trainingArea)
  }
}

fun IParentNode.resetLastEnemy() = perform {
  // If last enemy is not targeting us, reset it
  if (lastEnemy?.isInteractingWithMe() == false) {
    lastEnemy = null
  }
}

fun IParentNode.balanceCombatStyle() = sequence {
  perform("Balance Combat Style") {
    Combat.setAttackStyle(CombatHelper.getCombatStyleToUse())
    Waiting.waitUntil({ Combat.getCurrentAttackStyle() == CombatHelper.getCombatStyleToUse() })
  }
}

fun IParentNode.loot() = sequence {
  val itemsToLoot = VeloxCombatGUIState.lootItems.map { it.lowercase() }
  // Ensure there are coins on the ground
  condition("Has Loot") {
    (Query.groundItems().filter { it.name.lowercase() in itemsToLoot }.count() > 0 ||
        lastEnemy?.isValid == true) && !CombatHelper.isInCombat()
  }
  perform("Loot Item") {
    // If lastEnemy is valid, wait for ground items to be on their tile
    if (lastEnemy?.isValid == false) {
      Waiting.waitUntil(2000) {
        Query.groundItems().filter { it.tile == lastEnemy?.tile }.count() > 0
      }
    }

    val loot =
        Query.groundItems()
            .filter { itemsToLoot.any { lootItem -> it.name.lowercase().contains(lootItem) } }
            .isInLineOfSight()
            .findBestInteractable()
    if (loot.isPresent) {
      val lootItem = loot.get()
      val itemName = lootItem.name
      val initialCount = Query.inventory().nameEquals(itemName).count()

      lootItem.interact("Take")

      val lootedSuccessfully =
          Waiting.waitUntil(1500) {
            val newCount = Query.inventory().nameEquals(itemName).count()
            newCount > initialCount
          }

      if (VeloxCombatGUIState.buryBones.value) {
        // Bury the bones in your inventory now
        Query.inventory().actionEquals("Bury").forEach {
          it.click()
          Waiting.waitUntil({ MyPlayer.isAnimating() })
          Waiting.waitUntil({ !MyPlayer.isAnimating() })
        }
        Waiting.waitUntil { !Query.inventory().actionEquals("Bury").findFirst().isPresent }
      } else if (lootedSuccessfully) {
        val finalCount = Query.inventory().nameEquals(itemName).count()
        val amountLooted = finalCount - initialCount

        // Add the looted item to the EconomyTracker
        EconomyTracker.getInstance().addLootedItem(lootItem.id, amountLooted)
      }
    }
  }
}

fun IParentNode.eatFood() = sequence {
  selector {
    condition("Health Below Eat Percentage or Eat to Full") {
      MyPlayer.getCurrentHealthPercent() > VeloxCombatGUIState.eatHealthPercentage.value
    }
    // Eat food until we are above the eat health percentage or we are eating to full depending on
    // the setting
    repeatUntil({
      MyPlayer.getCurrentHealthPercent() == 100.0 && VeloxCombatGUIState.eatToFull.value ||
          (!VeloxCombatGUIState.eatToFull.value &&
              MyPlayer.getCurrentHealthPercent() > VeloxCombatGUIState.eatHealthPercentage.value)
    }) {
      perform("Eat Food") { InventoryHelper.eatFood() }
    }
  }
}

fun IParentNode.fightNearestEnemy(trainingArea: MonsterArea?) = selector {
  // Don't get an enemy if we are already in combat or if there are coins to be looted
  condition("Wait Until In Combat") { CombatHelper.isInCombat() || lastEnemy?.isValid == true }
  perform("Fight Nearest Enemy") { lastEnemy = CombatHelper.fightNearestEnemy(trainingArea) }
}
