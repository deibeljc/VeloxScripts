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
 * X Attack enemies it can path to
 *      X Possibly open doors in the way
 * - Eat food in a reasonable manner
 * - Be AFKish while fighting, similar to a real person
 *      - Move mouse off screen sometimes
 * X Bank for more food
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
        long TTLDefense = getSkillTracker().getTimeToLevel(Skill.DEFENCE);
        long TTLStrength = getSkillTracker().getTimeToLevel(Skill.STRENGTH);

        g.setColor(new Color(0, 0, 0));
        g.fillRect(260, 340, 280, 100);
        g.drawRect(260, 340, 280, 100);
        g.setColor(new Color(255, 255, 255));
        g.drawString("Time to level attack: " + String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(TTLAttack),
            TimeUnit.MILLISECONDS.toSeconds(TTLAttack) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TTLAttack))
        ), 275, 360);
        g.drawString("Time to level defense: " + String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(TTLDefense),
            TimeUnit.MILLISECONDS.toSeconds(TTLDefense) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TTLDefense))
        ), 275, 375);
        g.drawString("Time to level strength: " + String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(TTLStrength),
            TimeUnit.MILLISECONDS.toSeconds(TTLStrength) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TTLStrength))
        ), 275, 390);
    }
}
