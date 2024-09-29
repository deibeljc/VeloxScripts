package scripts.behaviors

import org.tribot.script.sdk.Inventory
import scripts.InventoryHelper
import scripts.frameworks.behaviorTree
import scripts.frameworks.createState
import scripts.frameworks.createStateMachine
import scripts.gui.VeloxCombatGUIState

val combatState = createState("Combat") { tree { behaviorTree { combatNode() } } }
val cookState = createState("Cook") { tree { behaviorTree { cookNode() } } }
val fishState = createState("Fish") { tree { behaviorTree { fishNode() } } }
val bankState = createState("Bank") { tree { behaviorTree { depositItems() } } }
val setupState = createState("Init") { tree { behaviorTree { setupNode() } } }

val stateMachine = createStateMachine {
  setupState.withPriority(3) on { InventoryHelper.hasRequiredItems() && InventoryHelper.hasFood() } to combatState
  setupState.withPriority(1) on { !Inventory.isFull() && !InventoryHelper.hasFood() } to fishState
  setupState.withPriority(2) on { InventoryHelper.hasRawFood() } to cookState

  combatState on { !InventoryHelper.hasFood() } to fishState

  any {
    withPriority(1) on { !InventoryHelper.hasRequiredItems() } to setupState
    withPriority(2) on { Inventory.isFull() && InventoryHelper.hasNonFood() } to bankState
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
