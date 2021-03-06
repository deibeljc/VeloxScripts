package com.dibes;

import com.dibes.banking.BankNode;
import com.dibes.combat.FightNode;
import com.dibes.inventory.EatNode;
import com.dibes.gui.GUI;
import com.dibes.gui.GuiNode;
import com.dibes.inventory.EquipNode;
import com.dibes.utility.PriorityNode;
import com.dibes.walking.WalkNode;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Goals:
 * Have a simple AIO main.combat bot that does the following:
 * X Attack enemies it can path to
 *      X Possibly open doors in the way
 * X Eat main inventory in a reasonable manner
 *      - Eat anything that classifies as food
 * X Be AFKish while fighting, similar to a real person
 *      X Move mouse off screen sometimes
 * X Bank for more food
 *      X Handles closed door in the way of path
 *
 * Stretch Goal:
 *  - Auto switch between main combat styles
 *  - Auto move to next area
 *  - Auto buy more food
 *
 * ---
 * GUI: Selecting enemies, selecting loot options, creating a custom path, etc, etc.
 */

@ScriptManifest(
        author = "Dibes",
        category = Category.COMBAT,
        description = "Fights some stuff",
        name = "Velox Combat",
        version = 0.7
)
public class VeloxCombat extends TaskScript {

    private GUI gui;
    private final PriorityNode[] nodes = {
        new FightNode(),
        new EatNode(),
        new BankNode(),
        new WalkNode(),
        new EquipNode()
    };

    @Override
    public void onStart() {
        // Show the gui.
        gui = new GUI(this);
        gui.setVisible(true);
        GUI.state.setScriptInstance(this);
        getSkillTracker().start();
        addNodes(new GuiNode());
    }

    @Override
    public void onExit() {
        gui.setVisible(false);
        gui.dispose();
    }

    public boolean startScript() {
        if (BankNode.FightTile == null && getClient().isLoggedIn()) {
            BankNode.FightTile = getLocalPlayer().getTile();
        }
        addNodes(nodes);
        return true;
    }

    public boolean stopScript() {
        removeNodes(nodes);
        return false;
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(0, 0, 0));
        g.fillRect(120, 340, 360, 160);
        g.setColor(new Color(255, 255, 255));
        // Do some debug paint
        debugPaint(g);
        g.drawString(formatSkillInfo(Skill.ATTACK), 130, 360);
        g.drawString(formatSkillInfo(Skill.DEFENCE), 130, 375);
        g.drawString(formatSkillInfo(Skill.STRENGTH), 130, 390);
    }

    private void debugPaint(Graphics g) {
        final PriorityNode[] sortedNodes = nodes.clone();
        Arrays.sort(sortedNodes, Comparator.comparingInt(PriorityNode::priority));
        int index = 0;
        for (PriorityNode node : sortedNodes) {
            g.drawString(node.priority() + " " + node.getClass().getSimpleName(), 560, 224 + (14 * index));
            index++;
        }
    }

    private String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private String formatSkillInfo(Skill skill) {
        long ttl = getSkillTracker().getTimeToLevel(skill);

        return padRight(skill.getName(), 15)
                + padRight("TTL " + formatMillis(ttl), 15)
                + " | " + padRight( "LVL " + getSkills().getRealLevel(skill) + " (" + getSkillTracker().getGainedLevels(skill) + ")", 15)
                + " | " + padRight("Exp/hr " + getSkillTracker().getGainedExperiencePerHour(skill), 15);
    }

    private String formatMillis(long milli) {
        return String.format("%02d min, %02d sec",
            TimeUnit.MILLISECONDS.toMinutes(milli),
            TimeUnit.MILLISECONDS.toSeconds(milli) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))
        );
    }
}
