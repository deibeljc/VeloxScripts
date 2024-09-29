package scripts.behaviors

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import scripts.Locations
import scripts.VeloxState
import scripts.frameworks.*
import scripts.updateState

fun IParentNode.fishNode() = sequence {
  updateState("Fishing")
  // We want to walk to the fishing spot
  walk(Locations.getBestTrainingArea().fishingArea.center, "Fishing Spot")
  fish()
}

fun IParentNode.fish() = sequence {
  selector {
    condition("Fishing") { MyPlayer.isAnimating() }
    perform("Fish") {
      Log.info("Fishing")
      VeloxState.setState("Fishing")
      val fishingSpot = Query.npcs().nameContains("Fishing spot").findBestInteractable().get()

      val interactionName =
        fishingSpot.actions.first { it.contains("Net") || it.contains("Lure") }
      fishingSpot.interact(interactionName)
      Waiting.waitUntil { MyPlayer.isAnimating() }
    }
  }
}
