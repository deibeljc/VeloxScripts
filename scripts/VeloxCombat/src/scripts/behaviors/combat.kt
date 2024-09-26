package scripts.behaviors

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Area
import scripts.CombatHelper
import scripts.InventoryHelper
import scripts.Locations
import scripts.MonsterArea
import scripts.updateState

fun IParentNode.combatNode() = sequence {
  updateState("Combat")
  balanceCombatStyle()
  val trainingArea = Locations.getBestTrainingArea()
  walk(trainingArea.area ?: Area.fromPolygon(), trainingArea.name)
  eatFood()
  lootCoins()
  fightNearestEnemy(trainingArea)
}

fun IParentNode.balanceCombatStyle() = sequence {
  perform("Balance Combat Style") {
    Combat.setAttackStyle(CombatHelper.getCombatStyleToUse())
    Waiting.waitUntil({ Combat.getCurrentAttackStyle() == CombatHelper.getCombatStyleToUse() })
  }
}

fun IParentNode.lootCoins() = selector {
  // Ensure there are coins on the ground
  condition("Has Coins") { Query.groundItems().nameContains("Coins").count() == 0 }
  perform("Loot Coins") {
    val coins = Query.groundItems().nameContains("Coins").findBestInteractable()
    val numCoins = Query.groundItems().nameContains("Coins").count()
    if (coins.isPresent) {
      coins.get().interact("Take")
      // Wait until it is looted
      Waiting.waitUntil { numCoins > Query.groundItems().nameContains("Coins").count() }
    }
  }
}

fun IParentNode.eatFood() = sequence {
  selector {
    condition("Health Below 50%") { MyPlayer.getCurrentHealthPercent() > 50 }
    perform("Eat Food") { InventoryHelper.eatFood() }
  }
}

fun IParentNode.fightNearestEnemy(trainingArea: MonsterArea?) = sequence {
  selector {
    // Don't get an enemy if we are already in combat or if there are coins to be looted
    condition("Wait Until In Combat") {
      CombatHelper.isInCombat() || Query.groundItems().nameContains("Coins").count() > 0
    }
    perform("Fight Nearest Enemy") { CombatHelper.fightNearestEnemy(trainingArea) }
  }
}
