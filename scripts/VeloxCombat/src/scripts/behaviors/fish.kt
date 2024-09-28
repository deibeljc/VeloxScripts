package scripts.behaviors

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import scripts.FISHING_AREA
import scripts.VeloxState
import scripts.updateState

fun IParentNode.fishNode() = sequence {
  updateState("Fishing")
  // We want to walk to the fishing spot
  walk(FISHING_AREA.center, "Fishing Spot")
  fish()
}

fun IParentNode.fish() = sequence {
  selector {
    condition("Fishing") { MyPlayer.isAnimating() }
    perform("Fish") {
      Log.info("Fishing")
      VeloxState.setState("Fishing")
      val fishingSpot = Query.npcs().nameContains("Fishing spot").findBestInteractable().get()
      fishingSpot.interact("Net")
      Waiting.waitUntil { MyPlayer.isAnimating() }
    }
  }
}
