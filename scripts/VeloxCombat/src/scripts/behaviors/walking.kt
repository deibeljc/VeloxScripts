package scripts.behaviors

import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.WorldTile
import scripts.VeloxState
import scripts.frameworks.IParentNode
import scripts.frameworks.condition
import scripts.frameworks.perform
import scripts.frameworks.selector
import scripts.walkTo

fun IParentNode.walk(destination: WorldTile, name: String?) = selector {
  condition("At spot") { atSpot(destination) }
  condition { walkTo(destination, name, VeloxState::setState) }
}

fun IParentNode.walk(destination: Area, name: String?) = selector {
  condition("At spot") { atSpot(destination) }
  condition { walkTo(destination.center, name, VeloxState::setState) }
}

fun atSpot(destination: WorldTile): Boolean {
  return MyPlayer.getTile().distanceTo(destination) <= 7
}

fun atSpot(destination: Area): Boolean {
  return destination.contains(MyPlayer.getTile())
}
