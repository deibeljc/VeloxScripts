package scripts.behaviors

import org.tribot.script.sdk.*
import org.tribot.script.sdk.query.Query
import scripts.InventoryHelper
import scripts.Locations
import scripts.VeloxState
import scripts.frameworks.*
import scripts.updateState
import kotlin.jvm.optionals.getOrNull

fun IParentNode.cookNode() = sequence {
  updateState("Cooking")
  ensureFireExists()
  cookFood()
  dropBurntFood()
}

fun IParentNode.dropBurntFood() =
  condition("Drop Burnt Food") {
    if (InventoryHelper.hasBurntFood() && !InventoryHelper.hasRawFood()) {
      val burntFood = Query.inventory().nameContains("Burnt").toList()
      Inventory.drop(burntFood)
    }
    Waiting.waitUntil { !InventoryHelper.hasBurntFood() }
  }

fun IParentNode.ensureFireExists() = selector {
  condition("Fire exists") {
    Query.gameObjects().nameContains("Fire").maxDistance(10.0).findBestInteractable().isPresent
  }
  createFire()
}

fun IParentNode.createFire() =
  sequence("Create Fire") {
    // Ensure we have a log
    selector {
      condition("Has Log") { InventoryHelper.hasLog() }
      sequence {
        // Ensure we have inventory space
        selector {
          condition("Has Empty Slot") { Inventory.getEmptySlots() > 0 }
          perform("Drop One Raw Food") {
            val rawFood = Query.inventory().nameContains("Raw").findFirst().getOrNull()
            rawFood?.let {
              it.click("Drop")
              Waiting.waitUntil { Inventory.getEmptySlots() > 0 }
            }
          }
        }
        walk(Locations.getBestTrainingArea().treeLocation, "Tree")
        // Chop a log
        perform("Chop Log") {
          VeloxState.setState("Chopping")
          val tree =
            Query.gameObjects()
              .filter { it.name == "Tree" || it.name == "Dead tree" }
              .findBestInteractable()
              .getOrNull()
          tree?.let {
            it.interact("Chop down")
            Waiting.waitUntil(5000) { MyPlayer.isAnimating() }
            Waiting.waitUntil { InventoryHelper.hasLog() || !MyPlayer.isAnimating() }
          }
          InventoryHelper.hasLog()
        }
      }
    }
    // Light the fire
    selector {
      condition("Fire exists nearby") {
        Query.gameObjects()
          .nameContains("Fire")
          .maxDistance(10.0)
          .findBestInteractable()
          .isPresent
      }
      lightFire()
    }
  }

fun IParentNode.lightFire() =
  perform("Light fire") {
    VeloxState.setState("Lighting fire to cook")
    val log = Query.inventory().nameContains("Log").findFirst().getOrNull()
    val tinderBox = Query.inventory().nameContains("Tinderbox").findFirst().get()
    log?.useOn(tinderBox)
    Waiting.waitUntil(15000) {
      Query.gameObjects().nameContains("Fire").maxDistance(2.0).findBestInteractable().isPresent
    }
  }

fun IParentNode.cookFood() =
  sequence("Cook Food") {
    condition("Has Raw Food and Fire") {
      InventoryHelper.hasRawFood() &&
              Query.gameObjects()
                .nameContains("Fire")
                .maxDistance(10.0)
                .findBestInteractable()
                .isPresent
    }
    perform("Cook Food") {
      VeloxState.setState("Cooking")
      val rawFood = Query.inventory().nameContains("Raw").findFirst().get()
      val fire =
        Query.gameObjects().nameContains("Fire").maxDistance(10.0).findBestInteractable().get()
      Log.info("Cooking $rawFood.name")
      rawFood.useOn(fire)
      Waiting.waitUntil(5000) { MakeScreen.isOpen() }
      if (MakeScreen.isOpen()) {
        // Wait for a uniform time to make it a little more human-like
        Waiting.waitNormal(500, 3)
        MakeScreen.makeAll(rawFood.id)
      }
      Waiting.waitUntil(5000) { MyPlayer.isAnimating() }

      var notAnimatingStartTime: Long = 0
      Waiting.waitUntil {
        if (!MyPlayer.isAnimating()) {
          if (notAnimatingStartTime == 0L) {
            notAnimatingStartTime = System.currentTimeMillis()
          }
          System.currentTimeMillis() - notAnimatingStartTime >= 3000
        } else {
          notAnimatingStartTime = 0
          false
        }
      }
    }
  }
