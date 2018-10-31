package food;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.items.Item;

public class EatNode extends TaskNode {
    private Item itemToEat = null;

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public boolean accept() {
        itemToEat = getInventory().get(item -> item != null && item.getName().contains("Salmon"));
        boolean shouldEat = getLocalPlayer().getHealthPercent() <= 30;

        return itemToEat != null && shouldEat;
    }

    @Override
    public int execute() {
        itemToEat.interact("Eat");
        return 0;
    }
}
