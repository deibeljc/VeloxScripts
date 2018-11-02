package banking;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.AbstractPath;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.core.Instance;
import utility.Utility;

import java.util.List;

public class BankNode extends TaskNode {

    public static Tile FightTile;

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public boolean accept() {
        return getInventory().all(item -> item != null && item.getName().contains("Salmon")).isEmpty();
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
                if (getBank().withdrawAll(item -> item != null && item.getName().contains("Salmon"))) {
                    // Walk back to the fighting area
                    if (FightTile != null) {
                        getWalking().walk(FightTile);
                    } else {
                        ScriptManager manager = new ScriptManager(Instance.getInstance());
                        manager.stop();
                        return 0;
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
