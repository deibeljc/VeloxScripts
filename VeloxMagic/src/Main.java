import com.dibes.banking.BankHides;
import com.dibes.combat.MagicCombat;
import looting.LootHides;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import tanning.TanHides;
import walking.WalkToCows;

@ScriptManifest(
    version = 0.01,
    name = "VeloxMagic",
    description = "Uses Magic to kill cows from a safespot, " +
            "then tans the hides, then banks them in Al Kharid",
    category = Category.MISC,
    author = "Dibes"
)
public class Main extends TaskScript {

    /**
     * - Uses Magic to kill cows - 1
     *      - Do it from a safe spot (part of that task)
     *      - Withdraw more runes if we are out - 3
     * - Loot hides - 2
     * - Tan hides when our inventory is full and we have at least one cowhide - 2
     * - Bank cow hides in a bank - 2/3
     *
     * Withdraw Runes
     * Tanning and Banking Tanned Hides
     * Kill Cow
     *
     *            -> (Withdraw Runes)
     *          no runes?
     * (Kill Cow) -> (Tanning Hides) -> (Bank Hides)
     *   Our inventory is full   No Raw Hides
     *
     */

    @Override
    public void onStart() {
        // Add nodes on start
        addNodes(new MagicCombat(), new LootHides(), new TanHides(), new BankHides(), new WalkToCows());
    }
}
