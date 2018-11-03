package tanning;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

public class TanHides extends TaskNode {

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean accept() {
        return getInventory().isFull()
                && getInventory().all(
                        item -> item != null && item.getName().equals("Cowhide")
                    ).size() > 0;
    }

    @Override
    public int execute() {
        Tile tanningTile = new Tile(3274, 3192);

        if (getLocalPlayer().distance(tanningTile) > 10) {
            getWalking().walk(tanningTile);
        }

        // Get the npc!!
        NPC ellis = getNpcs().closest("Ellis");
        if (ellis != null) {
            ellis.interact("Trade");
            WidgetChild hardLeather = getWidgets().getWidget(324).getChild(125);
            sleepUntil(hardLeather::isVisible, 5000);
            if (hardLeather.isVisible()) {
                hardLeather.interact("Tan all");
            }
        }

        return Calculations.random(200, 500);
    }
}
