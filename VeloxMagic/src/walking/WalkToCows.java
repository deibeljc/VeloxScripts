package walking;

import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;

public class WalkToCows extends TaskNode {

    private Tile cowArea;

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public boolean accept() {
        // Get Cost * 100 is minimum.
        boolean hasRequiredItems = getMagic().canCast(Normal.WIND_STRIKE)
                && getInventory().get("Coins").getAmount() > 75;
        cowArea = new Tile(3259, 3275);

        return !getInventory().isFull()
                && hasRequiredItems
                && !cowArea.getArea(8).contains(getLocalPlayer());
    }

    @Override
    public int execute() {
        // Should we handle the gate or not
        boolean handleGate = getLocalPlayer().getTile().getX() > 3267;

        if (handleGate) {
            if (getWalking().walk(new Tile(3269, 3227))) {
                GameObject tollGate = getGameObjects().closest(object -> object != null
                    && object.hasAction("Pay-toll(10gp)")
                );
                if (tollGate.interact("Pay-toll(10gp)")) {
                    // There is a stupid bug with the walking method that
                    // screws up around the toll gate
                    Tile workaroundTile = new Tile(3258, 3229);
                    sleepUntil(() -> getLocalPlayer().getTile().getX() <= 3267, 5000);
                    getWalking().walkExact(workaroundTile.getArea(3).getRandomTile());
                    sleepUntil(() -> workaroundTile.getArea(3).contains(getLocalPlayer()), 5000);
                }
            }
            return 0;
        }

        getWalking().walk(cowArea);
        return 0;
    }
}
