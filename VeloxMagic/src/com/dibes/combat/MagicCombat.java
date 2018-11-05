package com.dibes.combat;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.NPC;

public class MagicCombat extends TaskNode {
    private NPC cow;

    @Override
    public boolean accept() {
        /**
         * - Have Magic Supplies (air and earth runes)
         * - Have a Cow to fight
         * - We aren't fighting anything
         */
        boolean canCastSpell = getMagic().canCast(Normal.WIND_STRIKE);
        if (!getLocalPlayer().isInCombat() || !cow.canAttack()) {
            cow = getNpcs().closest(npc -> npc != null
                    && npc.getName() != null
                    && npc.getName().contains("Cow")
                    && ((npc.isInCombat() && npc.isInteracting(getLocalPlayer())) || !npc.isInCombat())
            );
        }

        return canCastSpell
                && cow != null;
    }

    @Override
    public int execute() {
        getMagic().castSpellOn(Normal.WIND_STRIKE, cow);
        sleepUntil(() -> !getLocalPlayer().isAnimating(), 2500);
        return Calculations.random(1000, 1500);
    }
}
