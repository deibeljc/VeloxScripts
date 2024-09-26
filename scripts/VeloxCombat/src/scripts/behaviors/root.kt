package scripts.behaviors

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.InventoryHelper
import scripts.statemachine.createState
import scripts.statemachine.createStateMachine

val combatState =
    createState("Combat") {
      action {
        Log.info("Combat state")
        behaviorTree { combatNode() }.tick()
      }
    }

val cookState = createState("Cook") { action { behaviorTree { cookNode() }.tick() } }

val fishState = createState("Fish") { action { behaviorTree { fishNode() }.tick() } }

val stateMachine = createStateMachine {
  combatState on { !InventoryHelper.hasFood() } to fishState
  fishState on { InventoryHelper.hasRawFood() && Inventory.getEmptySlots() <= 1 } to cookState
  cookState on { InventoryHelper.hasFood() && !InventoryHelper.hasRawFood() } to combatState
}
