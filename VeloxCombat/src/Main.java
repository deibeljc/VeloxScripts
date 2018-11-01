import banking.BankNode;
import combat.FightNode;
import food.EatNode;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * Goals:
 * Have a simple AIO combat bot that does the following:
 * - Attack enemies it can path to
 *      - Possibly open doors in the way
 * - Eat food in a reasonable manner
 * - Be AFKish while fighting, similar to a real person
 * - Bank for more food
 *
 * Stretch Goal:
 *  - Auto switch between combat styles
 *
 * ---
 * GUI: Selecting enemies, selecting loot options, creating a custom path, etc, etc.
 */

@ScriptManifest(
        author = "Dibes",
        category = Category.COMBAT,
        description = "Fights some stuff",
        name = "Velox Combat",
        version = 0.1
)
public class Main extends TaskScript {
    @Override
    public void onStart() {
        getSkillTracker().start();
        addNodes(new FightNode(), new EatNode(), new BankNode());
    }

    @Override
    public void onPaint(Graphics g) {
        long TTLAttack = getSkillTracker().getTimeToLevel(Skill.ATTACK);

        g.setColor(new Color(0, 0, 0));
        g.drawString("Time to level attack: " + String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(TTLAttack),
            TimeUnit.MILLISECONDS.toSeconds(TTLAttack) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TTLAttack))
        ), 275, 360);
    }
}
