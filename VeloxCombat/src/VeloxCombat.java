import combat.FightNode;
import food.EatNode;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;


/**
 * Goals:
 *  Have a simple AIO combat bot that does the following
 *  - Attacks enemies it can actually path to
 *  - Eats food in a reasonable manner
 *  - While fighting it goes AFKish similar to an actual player
 *  - Easily select and manage what you are fighting and how
 *  - Banking support.
 *
 */
@ScriptManifest(
        author = "Dibes",
        category = Category.COMBAT,
        description = "Simple AIO Combat bot",
        name = "Velox Combat",
        version = 0.05
)
public class VeloxCombat extends TaskScript {
    @Override
    public void onStart() {
        addNodes(new EatNode(), new FightNode());
    }
}
