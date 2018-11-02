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
 * X Be AFKish while fighting, similar to a real person
 *      - Move mouse off screen sometimes
 * X Bank for more food
 *      X Handles closed door in the way of path
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
        version = 0.5
)
public class Main extends TaskScript {
    @Override
    public void onStart() {
        getSkillTracker().start();
        addNodes(new FightNode(), new EatNode(), new BankNode());
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(0, 0, 0));
        g.fillRect(120, 340, 360, 60);
        g.setColor(new Color(255, 255, 255));
        g.drawString(formatSkillInfo(Skill.ATTACK), 130, 360);
        g.drawString(formatSkillInfo(Skill.DEFENCE), 130, 375);
        g.drawString(formatSkillInfo(Skill.STRENGTH), 130, 390);
    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private String formatSkillInfo(Skill skill) {
        long ttl = getSkillTracker().getTimeToLevel(skill);
        return padRight(skill.getName(), 10)
                + ": TTL " +  padRight(formatMillis(ttl), 15)
                + " | " + " LVL " + padRight(getSkills().getRealLevel(Skill.ATTACK) + " (" + getSkillTracker().getGainedLevels(skill) + ")", 6)
                + " | Exp/hr " + padRight(String.valueOf(getSkillTracker().getGainedExperiencePerHour(skill)), 10);
    }

    private String formatMillis(long milli) {
        return String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(milli),
            TimeUnit.MILLISECONDS.toSeconds(milli) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))
        );
    }
}
