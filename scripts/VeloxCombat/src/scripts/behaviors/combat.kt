package scripts.behaviors

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.Npc
import org.tribot.script.sdk.types.WorldTile
import scripts.*
import scripts.frameworks.*
import scripts.gui.VeloxCombatGUIState

var lastEnemy: Npc? = null

fun IParentNode.combatNode() = sequence {
  updateState("Combat")
  balanceCombatStyle()
  walk(
    Locations.getBestTrainingArea().area ?: Area.fromPolygon(), Locations.getBestTrainingArea().name
  )
  eatFood()
  selector {
    loot()
    fightNearestEnemy(Locations.getBestTrainingArea())
    condition("In combat") { CombatHelper.isInCombat() }
  }
}

fun IParentNode.balanceCombatStyle() = perform("Balance Combat Style") {
  val desiredStyle = CombatHelper.getCombatStyleToUse()
  Combat.setAttackStyle(desiredStyle)
  Waiting.waitUntil { Combat.getCurrentAttackStyle() == desiredStyle }
}

fun IParentNode.loot() = sequence {
  selector("If enemy died, wait for loot to spawn") {
    condition("Enemy Not Dead") {
      var enemyDead = false
      MyPlayer.get().ifPresent { player ->
        player.interactingCharacter.ifPresent {
          enemyDead = it.healthBarPercent == 0.0
        }
      }
      !enemyDead
    }
    perform("Wait for Loot to Spawn") {
      Waiting.waitUntil(3000) { Query.groundItems().tileEquals(lastEnemy?.tile ?: WorldTile(0, 0)).count() > 0 }
    }
  }
  condition("Not in Combat") { !CombatHelper.isInCombat() }
  condition("Loot Available") {
    val itemsToLoot = VeloxCombatGUIState.lootItems.map { it.lowercase() }
    Query.groundItems().filter {
      itemsToLoot.any { playerDefinedLoot -> it.name.lowercase().contains(playerDefinedLoot) }
    }.isInLineOfSight().toList().isNotEmpty()
  }
  perform("Loot Item") {
    val itemsToLoot = VeloxCombatGUIState.lootItems.map { it.lowercase() }
    val loot = Query.groundItems().filter {
      itemsToLoot.any { playerDefinedLoot ->
        it.name.lowercase().contains(playerDefinedLoot)
      }
    }.isInLineOfSight().findBestInteractable()
    if (loot.isPresent) {
      val lootItem = loot.get()
      val itemName = lootItem.name
      val initialCount = InventoryHelper.getCount(itemName)

      lootItem.interact("Take")

      val lootedSuccessfully = Waiting.waitUntil(1500) { InventoryHelper.getCount(itemName) > initialCount }

      if (lootedSuccessfully) {
        val finalCount = InventoryHelper.getCount(itemName)
        val amountLooted = finalCount - initialCount

        // Track loot unless it's bones we're burying
        if (!(itemName.contains("bones", ignoreCase = true) && VeloxCombatGUIState.buryBones.value)) {
          EconomyTracker.getInstance().addLootedItem(lootItem.id, amountLooted)
        }
      }

      if (VeloxCombatGUIState.buryBones.value) {
        // Bury bones in inventory
        Query.inventory().nameContains("bones").forEach {
          it.click("Bury")
          Waiting.waitUntil { MyPlayer.isAnimating() }
          Waiting.waitUntil { !MyPlayer.isAnimating() }
        }
      }
    }
  }
}

fun IParentNode.eatFood() = selector {
  condition("Needs to Eat") {
    (VeloxCombatGUIState.eatToFull.value && MyPlayer.getCurrentHealthPercent() == 100.0) || (!VeloxCombatGUIState.eatToFull.value && MyPlayer.getCurrentHealthPercent() >= VeloxCombatGUIState.eatHealthPercentage.value)
  }
  repeatUntil({
    (VeloxCombatGUIState.eatToFull.value && MyPlayer.getCurrentHealthPercent() == 100.0) || (!VeloxCombatGUIState.eatToFull.value && MyPlayer.getCurrentHealthPercent() > VeloxCombatGUIState.eatHealthPercentage.value)
  }) {
    perform("Eat Food") { InventoryHelper.eatFood() }
  }
}

fun IParentNode.fightNearestEnemy(trainingArea: MonsterArea?) = sequence {
  condition("Not in Combat") { !CombatHelper.isInCombat() }
  perform("Fight Nearest Enemy") { CombatHelper.fightNearestEnemy(trainingArea) }
}
