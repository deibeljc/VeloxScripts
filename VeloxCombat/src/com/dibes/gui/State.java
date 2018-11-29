package com.dibes.gui;

import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;

public class State {
    private boolean scriptStarted = false;
    private boolean shouldEat = false;
    private String foodName = "";
    private int eatPercent = 0;
    private ListModel npcListModel = new DefaultListModel();
    private ListModel selectedNpcListModel = new DefaultListModel();
    private HashSet<String> npcSet = new HashSet<>();
    private HashSet<String> selectedNpcSet = new HashSet<>();
    private TaskScript scriptInstance = null;

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

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getEatPercent() {
        return eatPercent;
    }

    public void setEatPercent(int eatPercent) {
        this.eatPercent = eatPercent;
    }

    public TaskScript getScriptInstance() {
        return scriptInstance;
    }

    public void setScriptInstance(TaskScript scriptInstance) {
        this.scriptInstance = scriptInstance;
    }
}
