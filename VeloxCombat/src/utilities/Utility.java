package utilities;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.map.Tile;

public class Utility extends MethodContext {

    public static Client client;

    public Utility() {
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
}
