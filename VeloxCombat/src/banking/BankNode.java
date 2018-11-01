package banking;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.impl.GlobalPath;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;
import org.dreambot.api.script.TaskNode;

public class BankNode extends TaskNode {

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean accept() {
        // Eventually we want to support all food typed automatically
        String foodName = "Salmon";
        return getInventory().all(item -> item != null && item.getName().contains(foodName)).isEmpty();
    }

    @Override
    public int execute() {
        // We are out of food and need to travel to the nearest bank and get some!
        WebFinder web = getWalking().getWebPathFinder();
        Tile nearestBank = getBank().getClosestBank(BankType.BOOTH).getTile();
        AbstractWebNode bankNode = web.getNearest(nearestBank, 15);
        AbstractWebNode myNode = web.getNearest(getLocalPlayer().getTile(), 20);
        GlobalPath pathToBank = getWalking().getWebPathFinder().calculate(myNode, bankNode);
        while (pathToBank.walk()) { }

        return 0;
    }
}
