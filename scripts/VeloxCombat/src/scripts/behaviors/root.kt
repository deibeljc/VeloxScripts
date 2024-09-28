package scripts.behaviors

import org.tribot.script.sdk.Inventory
import scripts.InventoryHelper
import scripts.behaviortree.behaviorTree
import scripts.frameworks.createState
import scripts.frameworks.createStateMachine
import scripts.gui.VeloxCombatGUIState

val combatState = createState("Combat") { tree { behaviorTree { combatNode() } } }
val cookState = createState("Cook") { tree { behaviorTree { cookNode() } } }
val fishState = createState("Fish") { tree { behaviorTree { fishNode() } } }
val bankState = createState("Bank") { tree { behaviorTree { depositItems() } } }
val setupState = createState("Init") { tree { behaviorTree { setupNode() } } }

val stateMachine = createStateMachine {
  setupState on { InventoryHelper.hasRequiredItems() } to combatState
  setupState on { !Inventory.isFull() && !InventoryHelper.hasFood() } to fishState

  combatState on { !InventoryHelper.hasFood() } to fishState

  any {
    on { !InventoryHelper.hasRequiredItems() } to setupState
    on { Inventory.isFull() && InventoryHelper.hasNonFood() } to bankState
  }

  fishState on
      {
        InventoryHelper.hasRawFood() &&
            InventoryHelper.rawFoodCount() >= VeloxCombatGUIState.foodToReplenish.value
      } to
      cookState
  cookState on { InventoryHelper.hasFood() && InventoryHelper.rawFoodCount() == 0 } to combatState
  bankState on { !Inventory.isFull() } to combatState
}
