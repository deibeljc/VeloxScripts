package combat;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.NPC;

public class FightNode extends TaskNode {

    private NPC npc = null;

    private NPC getClosestNPC() {
        return getNpcs().closest(npc -> {
            if (npc == null || npc.getName() == null) {
                return false;
            }

            return npc.getName().contains("warrior")
                    && !getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), npc.getTile()).isEmpty();
        });
    }

    @Override
    public boolean accept() {
        npc = getClosestNPC();

        return npc != null
                && npc.canAttack()
                && !getLocalPlayer().isInCombat();
    }

    @Override
    public int execute() {
        npc.interact("Attack");
        return 300 + (int) (Math.random() * 300 + 1);
    }
}
