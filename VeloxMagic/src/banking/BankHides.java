package banking;

import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.script.TaskNode;

public class BankHides extends TaskNode {

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean accept() {
        return getInventory().isFull()
                && getInventory().get("Cowhide") == null
                && getInventory().get("Hard leather") != null;
    }

    @Override
    public int execute() {
        if (getWalking().shouldWalk() && !BankLocation.AL_KHARID.getArea(8).contains(getLocalPlayer())) {
            getWalking().walk(BankLocation.AL_KHARID.getCenter());
            return 0;
        }

        if (getBank().openClosest()) {
            getBank().depositAll("Hard leather");
        }

        return 0;
    }
}
