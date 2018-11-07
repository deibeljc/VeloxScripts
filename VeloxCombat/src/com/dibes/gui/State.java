package com.dibes.gui;

import org.dreambot.api.wrappers.interactive.NPC;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;

public class State {
    private boolean scriptStarted = false;
    private boolean shouldEat = false;
    private ListModel npcListModel = new DefaultListModel();
    private ListModel selectedNpcListModel = new DefaultListModel();
    private HashSet<String> npcSet = new HashSet<>();
    private HashSet<String> selectedNpcSet = new HashSet<>();

    public boolean isScriptStarted() {
        return scriptStarted;
    }

    public void setScriptStarted(boolean scriptStarted) {
        this.scriptStarted = scriptStarted;
    }

    public ListModel getNpcListModel() {
        return npcListModel;
    }

    public ListModel getSelectedNpcListModel() {
        return selectedNpcListModel;
    }

    public HashSet<String> getSelectedNpcSet() {
        return selectedNpcSet;
    }

    public void moveFromNpcSetToSelectedNpcSet(String name) {
        npcSet.remove(name);
        selectedNpcSet.add(name);
    }

    public void setNpcs(List<NPC> npcs) {
        for (NPC npc : npcs) {
            npcSet.add(npc.getName());
        }

        for (String npcName : npcSet) {
            if (!((DefaultListModel) npcListModel).contains(npcName)
                        && !((DefaultListModel) selectedNpcListModel).contains(npcName)) {
                ((DefaultListModel) npcListModel).addElement(npcName);
            }
        }
    }

    public boolean isShouldEat() {
        return shouldEat;
    }

    public void setShouldEat(boolean shouldEat) {
        this.shouldEat = shouldEat;
    }
}
