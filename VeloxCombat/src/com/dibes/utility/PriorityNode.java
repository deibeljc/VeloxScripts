package com.dibes.utility;

import org.dreambot.api.script.TaskNode;

public abstract class PriorityNode extends TaskNode {

    public Priorities getPriorities() {
        return new Priorities();
    }

    @Override
    public int priority() {
        return PriorityManager.getPriority(getPriorities());
    }

    @Override
    public boolean accept() {
        return false;
    }

    @Override
    public int execute() {
        return 0;
    }
}
