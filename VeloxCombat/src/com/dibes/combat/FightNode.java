package com.dibes.combat;

import com.dibes.banking.BankNode;
import com.dibes.gui.GUI;
import com.dibes.utility.PriorityNode;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import com.dibes.utility.Utility;

public class FightNode extends PriorityNode {
    private NPC npc;
    private GameObject doorToOpen;

    @Override
    public boolean accept() {
        npc = getNpcs().closest(npc -> npc != null
                && npc.getName() != null
                && GUI.state.getSelectedNpcSet().contains(npc.getName())
                && ((npc.isInCombat() && npc.isInteracting(getLocalPlayer())) || !npc.isInCombat())
        );

        boolean canPathToNpc = false;
        if (npc != null) {
             canPathToNpc = getMap().canReach(npc);
        }

        if (!canPathToNpc && npc != null) {
            Utility utility = new Utility(getClient());
            doorToOpen = utility.getDoorBetweenTiles(npc.getTile());
        }

        return npc != null
                && npc.canAttack()
                && !getLocalPlayer().isInCombat();
    }

    @Override
    public int execute() {
        if (BankNode.FightTile == null) {
            BankNode.FightTile = npc.getTile();
        }

        if (getPlayerSettings().getConfig(172) == 1) {
            if (getTabs().open(Tab.COMBAT)) {
                getMouse().click(getWidgets().getWidget(593).getChild(30).getRectangle());
            }
        }

        if (doorToOpen != null) {
            doorToOpen.interact("Open");
            doorToOpen = null;
        } else {
            npc.interact("Attack");
            sleepUntil(() -> getLocalPlayer().isInCombat(), 3000);
            getMouse().moveMouseOutsideScreen();
        }
        return 300 + (int) (Math.random() * 300);
    }
}
