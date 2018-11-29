package com.dibes.walking;

import com.dibes.banking.BankNode;
import com.dibes.utility.Priorities;
import com.dibes.utility.Priority;
import com.dibes.utility.PriorityNode;

public class WalkNode extends PriorityNode {

    @Override
    public Priorities getPriorities() {
        return new Priorities(
            Priority.NORMAL,
            Priority.LOW
        );
    }

    @Override
    public boolean accept() {
        return !BankNode
                .FightTile
                .getArea(25)
                .contains(getLocalPlayer());
    }

    @Override
    public int execute() {
        if (getWalking().shouldWalk()) {
            getWalking().walk(BankNode.FightTile);
        }
        return 0;
    }
}
