package com.dibes.utility;

public enum Priority {
    LOW(0),
    NORMAL(1),
    HIGH(2);

    private int priority;
    Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}

