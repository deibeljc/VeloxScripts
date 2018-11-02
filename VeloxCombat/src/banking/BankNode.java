package banking;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.AbstractPath;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.core.Instance;
import utilities.Utility;

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
        Tile tile = getFirstWalkingTile();
        if (getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), tile).isEmpty()) {
            List<GameObject> doors = getGameObjects().all(object -> object != null && object.getName().contains("door"));
            // Sort by distance to NPC
            doors.sort((door1, door2) -> (int) (tile.distance(door1) - tile.distance(door2)));

            for (GameObject door : doors) {
                Utility utility = new Utility();
                if (utility.canBothPathTo(tile, door.getTile())) {
                    doorToOpen = door;
                    break;
                }
            }
        }

        if (doorToOpen != null) {
            doorToOpen.interact("Open");
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

    private Tile getFirstWalkingTile() {
        AbstractPath<AbstractWebNode> path = getWalking().getWebPathFinder().calculate(getLocalPlayer().getTile(), getBank().getClosestBankLocation().getCenter());
        return path.next().getTile();
    }
}
