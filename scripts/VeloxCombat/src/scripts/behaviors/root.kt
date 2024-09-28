package scripts.behaviors

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.behaviorTree
import scripts.InventoryHelper
import scripts.statemachine.createState
import scripts.statemachine.createStateMachine

val combatState = createState("Combat") { action { behaviorTree { combatNode() }.tick() } }

val cookState = createState("Cook") { action { behaviorTree { cookNode() }.tick() } }
val fishState = createState("Fish") { action { behaviorTree { fishNode() }.tick() } }
val bankState = createState("Bank") { action { behaviorTree { depositItems() }.tick() } }
val initState = createState("Init") { action { behaviorTree { initNode() }.tick() } }

val stateMachine = createStateMachine {
  initState on { InventoryHelper.hasRequiredItems() } to combatState
  combatState on { !InventoryHelper.hasFood() } to fishState
  combatState on { Inventory.isFull() } to bankState
  bankState on { !Inventory.isFull() } to combatState
  fishState on { InventoryHelper.hasRawFood() && InventoryHelper.rawFoodCount() >= 10 } to cookState
  fishState on { Inventory.isFull() && InventoryHelper.hasNonFood() } to bankState
  cookState on { InventoryHelper.hasFood() && InventoryHelper.rawFoodCount() == 0 } to combatState
}
