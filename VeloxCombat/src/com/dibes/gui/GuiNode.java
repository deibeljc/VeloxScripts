package com.dibes.gui;

import org.dreambot.api.script.TaskNode;

public class GuiNode extends TaskNode {
    @Override
    public boolean accept() {
        return !GUI.state.isScriptStarted();
    }

    @Override
    public int execute() {
        GUI.state.setNpcs(getNpcs().all(npc -> npc.distance(getLocalPlayer()) < 20));
        return 1000;
    }
}
