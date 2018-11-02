package combat;

import banking.BankNode;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

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
                Tile topTile = new Tile(door.getTile().getX(), door.getTile().getY() + 1);
                Tile rightTile = new Tile(door.getTile().getX() + 1, door.getTile().getY());
                Tile bottomTile = new Tile(door.getTile().getX(), door.getTile().getY() - 1);
                Tile leftTile = new Tile(door.getTile().getX() - 1, door.getTile().getY());

                if (canBothPathTo(npc, topTile, rightTile, bottomTile, leftTile)) {
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
            log("Opening Door");
        } else {
            npc.interact("Attack");
            log("Attacking");
        }
        return 100 + (int) (Math.random() * 300);
    }

    private boolean canBothPathTo(NPC npc, Tile... tiles) {
        Boolean playerCanPathTo = false;
        Boolean npcCanPathTo = false;
        // Can the player path to any tiles?
        for (Tile tile : tiles) {
            if (getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), tile).size() > 0) {
                playerCanPathTo = true;
            }
            if (getWalking().getAStarPathFinder().calculate(npc.getTile(), tile).size() > 0) {
                npcCanPathTo = true;
            }
        }

        return playerCanPathTo && npcCanPathTo;
    }
}
