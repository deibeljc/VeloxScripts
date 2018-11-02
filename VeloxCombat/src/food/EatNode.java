package food;

import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.items.Item;

public class EatNode extends TaskNode {
    private Item foodItem;

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean accept() {
        foodItem = getInventory().get(item -> item != null && item.getName().contains("Salmon"));
        return foodItem != null && getLocalPlayer().getHealthPercent() <= 50;
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
