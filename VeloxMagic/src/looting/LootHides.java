package looting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.items.GroundItem;

public class LootHides extends TaskNode {

    private GroundItem hide;

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean accept() {
        hide = getGroundItems().closest(item -> item != null
                && item.getName() != null
                && item.getName().contains("Cowhide")
        );

        return !getLocalPlayer().isInCombat()
                && !getLocalPlayer().isHealthBarVisible()
                && hide != null
                && !getInventory().isFull();
    }

    @Override
    public int execute() {
        hide.interact("Take");
        sleepWhile(() -> getLocalPlayer().isMoving(), 3000);
        return Calculations.random(300, 500);
    }
}
