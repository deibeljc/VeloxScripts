package com.dibes.utility;

public class Priorities {
    private Priority main = Priority.LOW;
    private Priority sub = Priority.LOW;

    public Priorities() {}

    public Priorities(Priority sub) {
        this.sub = sub;
    }

    public Priorities(Priority main, Priority sub) {
        this.main = main;
        this.sub = sub;
    }

    public Priority getMainPriority() {
        return main;
    }

    public Priority getSubPriority() {
        return sub;
    }
}
