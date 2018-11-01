package banking;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.TaskNode;
import org.dreambot.core.Instance;

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
}
