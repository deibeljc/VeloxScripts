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
val setupState = createState("Init") { action { behaviorTree { setupNode() }.tick() } }

val stateMachine = createStateMachine {
  setupState on { InventoryHelper.hasRequiredItems() } to combatState
  setupState on { !Inventory.isFull() && !InventoryHelper.hasFood() } to fishState

  combatState on { !InventoryHelper.hasFood() } to fishState

  any {
    on { !InventoryHelper.hasRequiredItems() } to setupState
    on { Inventory.isFull() && InventoryHelper.hasNonFood() } to bankState
  }

  fishState on { InventoryHelper.hasRawFood() && InventoryHelper.rawFoodCount() >= 10 } to cookState
  cookState on { InventoryHelper.hasFood() && InventoryHelper.rawFoodCount() == 0 } to combatState
  bankState on { !Inventory.isFull() } to combatState
}
