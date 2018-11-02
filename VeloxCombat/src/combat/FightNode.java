package combat;

import banking.BankNode;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import utilities.Utility;

import javax.rmi.CORBA.Util;
import java.util.List;

public class FightNode extends TaskNode {
    private NPC npc;
    private GameObject doorToOpen;

    @Override
    public boolean accept() {
        npc = getNpcs().closest(npc -> npc != null && npc.getName() != null && npc.getName().contains("warrior"));

        boolean canPathToNpc = getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), npc.getTile()).size() > 0;

        if (!canPathToNpc) {
            doorToOpen = null;
            List<GameObject> doors = getGameObjects().all(object -> object != null && object.getName().contains("door"));
            // Sort by distance to NPC
            doors.sort((door1, door2) -> (int) (npc.distance(door1) - npc.distance(door2)));

            for (GameObject door : doors) {
                Utility utility = new Utility();
                if (utility.canBothPathTo(npc.getTile(), door.getTile())) {
                    doorToOpen = door;
                    break;
                }
            }
        }

        return npc != null
                && npc.canAttack()
                && !getLocalPlayer().isInCombat();
    }

    @Override
    public int execute() {
        BankNode.FightTile = npc.getTile();
        log("Execution " + (doorToOpen != null ? doorToOpen.toString() : ""));
        if (doorToOpen != null) {
            doorToOpen.interact("Open");
            doorToOpen = null;
        } else {
            npc.interact("Attack");
        }
        return 100 + (int) (Math.random() * 300);
    }
}
