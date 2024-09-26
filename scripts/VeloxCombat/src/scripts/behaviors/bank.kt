package scripts.behaviors

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.walking.GlobalWalking
import scripts.InventoryHelper
import scripts.requiredItemsToHave
import scripts.updateState

fun IParentNode.openBank() = sequence {
  updateState("Going to Bank")
  selector {
    condition("At Bank") { Bank.isNearby() }
    condition("Walk to bank") { GlobalWalking.walkToBank() }
  }
  selector {
    condition("Bank open") { Bank.isOpen() }
    perform("Open Bank") {
      val bankBooth = Query.gameObjects().nameContains("Bank booth").findBestInteractable().get()
      bankBooth.interact("Bank")
      Waiting.waitUntil { Bank.isOpen() }
    }
  }
}

fun IParentNode.depositItems() = sequence {
  updateState("Despositing Items")
  openBank()
  selector {
    condition("Deposit All Non Food") { InventoryHelper.hasNonFood() }
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

fun IParentNode.initNode() = sequence {
  updateState("Init")
  selector {
    condition("Has Required Items") { InventoryHelper.hasRequiredItems() }
    sequence {
      openBank()
      perform("Withdraw required items") {
        requiredItemsToHave.forEach { sublist ->
          val nameToFind =
            sublist.find { item -> Query.bank().nameContains(item).findFirst().isPresent }
          nameToFind?.let { name ->
            val itemCount = Query.bank().nameContains(name).count()
            val item = Query.bank().nameContains(name).findFirst().orElse(null)
            item?.let {
              Log.info("Withdrawing $itemCount $name")
              Waiting.waitUntil { Bank.withdraw(it, 1) }
            }
          }
        }
        Bank.close()
        Waiting.waitUntil { !Bank.isOpen() }
      }
    }
  }
}
