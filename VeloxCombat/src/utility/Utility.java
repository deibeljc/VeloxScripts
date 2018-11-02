package utility;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.List;

public class Utility extends MethodContext {
    public Utility(Client client) {
        this.registerContext(client);
    }

    public boolean canBothPathTo(Tile targetTile, Tile tile) {
        Tile[] tiles = new Tile[] {
            new Tile(tile.getX(), tile.getY() + 1),
            new Tile(tile.getX() + 1, tile.getY()),
            new Tile(tile.getX(), tile.getY() - 1),
            new Tile(tile.getX() - 1, tile.getY())
        };

        Boolean playerCanPathTo = false;
        Boolean npcCanPathTo = false;
        // Can the player path to any tiles?
        for (Tile dTile : tiles) {
            if (getWalking().getAStarPathFinder().calculate(getLocalPlayer().getTile(), dTile).size() > 0) {
                playerCanPathTo = true;
            }
            if (getWalking().getAStarPathFinder().calculate(targetTile, dTile).size() > 0) {
                npcCanPathTo = true;
            }
        }

        return playerCanPathTo && npcCanPathTo;
    }

    public GameObject getDoorBetweenTiles(Tile targetTile) {
        List<GameObject> doors = getGameObjects().all(object -> object != null && object.getName().contains("door"));
        // Sort by distance to NPC
        doors.sort((door1, door2) -> (int) (targetTile.distance(door1) - targetTile.distance(door2)));

        for (GameObject door : doors) {
            Utility utility = new Utility(getClient());
            if (utility.canBothPathTo(targetTile, door.getTile())) {
                return door;
            }
        }

        return null;
    }
}
