package com.dibes.banking;

import com.dibes.gui.GUI;
import com.dibes.utility.Priorities;
import com.dibes.utility.Priority;
import com.dibes.utility.PriorityNode;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.AbstractPath;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import com.dibes.utility.Utility;

public class BankNode extends PriorityNode {

    public static Tile FightTile;

    @Override
    public Priorities getPriorities() {
        return new Priorities(Priority.HIGH, Priority.NORMAL);
    }

    @Override
    public boolean accept() {
        return GUI.state.isShouldEat()
                && getInventory().all(item -> item != null && item.getName().contains(GUI.state.getFoodName())).isEmpty();
    }

    @Override
    public int execute() {
        GameObject doorToOpen = null;
        // If we can't walk there, try to open a door.
        Tile firstWalkingTile = getFirstWalkingTile();
        Utility utility = new Utility(getClient());

        if (getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), firstWalkingTile).isEmpty()) {
            doorToOpen = utility.getDoorBetweenTiles(firstWalkingTile);
        }

        if (doorToOpen != null) {
            final GameObject doorCheck = doorToOpen;
            doorToOpen.interact("Open");
            sleepWhile(() -> doorCheck.hasAction("Open"), 10000);
        }

        if (getWalking().walk(getBank().getClosestBankLocation().getCenter())) {
            if (getBank().openClosest()) {
                if (getBank().withdrawAll(item -> item != null
                        && item.getName().contains(GUI.state.getFoodName()))) {
                    getBank().close();
                    // Walk back to the fighting area
                    getWalking().walk(FightTile);
                } else {
                    log("No More Food :(");
                    if (getBank().close()) {
                        getTabs().logout();
                        GUI.state.getScriptInstance().stop();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Here we want to get the first node in the web that we would want to click to use
     * for our door opening method.
     * @return Tile of the first node in the web path
     */
    private Tile getFirstWalkingTile() {
        AbstractWebNode playerNode = getWalking().getWebPathFinder().getNearest(getLocalPlayer().getTile(), 25);
        AbstractWebNode bankNode = getWalking().getWebPathFinder().getNearest(getBank().getClosestBankLocation().getCenter(), 25);
        AbstractPath path = getWalking().getWebPathFinder().calculate(playerNode, bankNode);
        AbstractWebNode firstTile = (AbstractWebNode) path.first();
        return firstTile.getTile();
    }
}
