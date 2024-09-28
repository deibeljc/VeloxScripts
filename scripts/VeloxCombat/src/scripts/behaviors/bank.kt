package scripts.behaviors

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.walking.GlobalWalking
import scripts.InventoryHelper
import scripts.behaviortree.*
import scripts.gui.VeloxCombatGUIState
import scripts.updateState

fun IParentNode.openBank() = sequence {
  selector {
    condition("At Bank") { Bank.isNearby() }
    condition("Walk to bank") { GlobalWalking.walkToBank() }
  }
  selector {
    condition("Bank open") { Bank.isOpen() }
    condition("Open Bank") {
      val bankBooth =
        Query.gameObjects().nameContains("Bank booth", "Bank chest").findBestInteractable().get()
      if (bankBooth.actions.contains("Bank")) {
        bankBooth.interact("Bank")
      } else if (bankBooth.actions.contains("Use")) {
        bankBooth.interact("Use")
      }
      Waiting.waitUntil { Bank.isOpen() }
    }
  }
}

fun IParentNode.depositItems() = sequence {
  updateState("Despositing Items")
  openBank()
  selector {
    condition("Deposit All Non Food") { !InventoryHelper.hasNonFood() }
    perform("Deposit All Non Food") {
      val itemIDsToDeposit = InventoryHelper.getNonFoodItems().toList()
      val itemsToDeposit =
        itemIDsToDeposit.associate { item ->
          item.id to Query.inventory().idEquals(item.id).count()
        }

      itemsToDeposit.forEach { (item, count) -> Bank.deposit(item, count) }
    }
  }
}

fun IParentNode.setupNode() = sequence {
  updateState("Init")
  selector {
    condition("Has Required Items") { InventoryHelper.hasRequiredItems() }
    sequence {
      openBank()
      condition("Withdraw required items") {
        val allItemsFound =
          InventoryHelper.getRequiredItems().all { sublist ->
            sublist.any { item -> Query.bank().nameContains(item).findFirst().isPresent }
          }

        if (!allItemsFound) {
          val missingItems = InventoryHelper.getRequiredItems().filter { sublist ->
            sublist.none { item -> Query.bank().nameContains(item).findFirst().isPresent }
          }.flatten()
          Log.error(
            "Not all required items found in bank. Missing items: ${missingItems.joinToString(", ")}. Stopping script."
          )
          VeloxCombatGUIState.initiateStop()
        }

        InventoryHelper.getRequiredItems().forEach { sublist ->
          val nameToFind =
            sublist.find { item -> Query.bank().nameContains(item).findFirst().isPresent }
          nameToFind?.let { name ->
            val itemCount = Query.bank().nameContains(name).count()
            val item = Query.bank().nameContains(name).findFirst().orElse(null)
            item?.let {
              Log.info("Withdrawing $itemCount $name")
              Waiting.waitUntil { Bank.withdraw(it, it.stack) }
            }
          }
        }
        Bank.close()
        Waiting.waitUntil { !Bank.isOpen() }
      }
    }
  }
}
