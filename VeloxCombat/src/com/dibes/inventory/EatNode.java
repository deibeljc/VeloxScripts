package com.dibes.inventory;

import com.dibes.gui.GUI;
import com.dibes.utility.Priorities;
import com.dibes.utility.Priority;
import com.dibes.utility.PriorityNode;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.wrappers.items.Item;

public class EatNode extends PriorityNode {
    private Item foodItem;

    @Override
    public Priorities getPriorities() {
        return new Priorities(Priority.NORMAL, Priority.NORMAL);
    }

    @Override
    public boolean accept() {
        foodItem = getInventory().get(item -> item != null && item.getName().contains("Salmon"));
        return foodItem != null
                && getLocalPlayer().getHealthPercent() <= 50
                && GUI.state.isShouldEat();
    }

    @Override
    public int execute() {
        if (!getTabs().isOpen(Tab.INVENTORY)) {
            if (getTabs().open(Tab.INVENTORY)) {
                foodItem.interact("Eat");
            }
        } else {
            foodItem.interact("Eat");
        }
        return 300 + (int) (Math.random() * 300);
    }
}
