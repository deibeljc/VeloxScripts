package com.dibes.gui;

import javax.swing.*;

import com.dibes.VeloxCombat;

import java.awt.*;

public class GUI extends JFrame {
    // So we can have a
    public static State state = new State();

    // Get a reference to our script context
    private VeloxCombat ctx;

    // Dimension statics
    private final int HEIGHT = 350;

    public GUI(VeloxCombat ctx) {
        this.ctx = ctx;
        initComponents();
    }

    private void initComponents() {
        // Setup the frame
        setTitle("Velox Combat");
        Container pane = getContentPane();

        // Define out components
        JButton startButton = new JButton("Start");
        JTabbedPane tabs = setupTabs();

        pane.add(tabs, BorderLayout.PAGE_START);
        // Set button height
        startButton.setPreferredSize(new Dimension(300, 30));
        // Handle starting and stopping the script
        startButton.addActionListener(e -> {
            if (!state.isScriptStarted()) {
                state.setScriptStarted(ctx.startScript());
                startButton.setText("Stop");
                return;
            }
            state.setScriptStarted(ctx.stopScript());
            startButton.setText("Start");
        });
        pane.add(startButton, BorderLayout.PAGE_END);

        pane.setPreferredSize(new Dimension(300, HEIGHT));
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private JTabbedPane setupTabs() {
        JTabbedPane tabs = new JTabbedPane();
        GridBagConstraints constraints = new GridBagConstraints();

        // Create our list of NPCs available
        JList npcList = new JList(state.getNpcListModel());
        npcList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        npcList.setLayoutOrientation(JList.VERTICAL);
        npcList.setVisibleRowCount(1);
        JScrollPane npcListScroller = new JScrollPane(npcList);
        npcListScroller.setPreferredSize(new Dimension(300, (HEIGHT - 60) / 2));

        // Add Button
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String npcName = (String) state.getNpcListModel()
                    .getElementAt(npcList.getSelectedIndex());
            state.moveFromNpcSetToSelectedNpcSet(npcName);
            ((DefaultListModel)state.getNpcListModel()).removeElement(npcName);
            ((DefaultListModel)state.getSelectedNpcListModel()).addElement(npcName);
        });
        addButton.setPreferredSize(new Dimension(60, 20));

        // Create our list of NPCs we select
        JList selectedNpcList = new JList(state.getSelectedNpcListModel());
        selectedNpcList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        selectedNpcList.setLayoutOrientation(JList.VERTICAL);
        selectedNpcList.setVisibleRowCount(1);
        JScrollPane selectedNpcsScroller = new JScrollPane(selectedNpcList);
        npcListScroller.setPreferredSize(new Dimension(300, (HEIGHT - 60) / 2));

        JPanel combatPanel = new JPanel(false);
        combatPanel.setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        // Add scroller
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.ipady = (HEIGHT - 60) / 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        combatPanel.add(npcListScroller, constraints);
        // Add button
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.ipady = 0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        combatPanel.add(addButton);
        // Add scroller
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.ipady = (HEIGHT - 60) / 2;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        combatPanel.add(selectedNpcsScroller, constraints);

        JPanel eatPanel = new JPanel(false);
        JLabel eatText = new JLabel("Eat");
        eatText.setHorizontalAlignment(JLabel.CENTER);
        eatPanel.setLayout(new GridLayout(1, 1));
        eatPanel.add(eatText);

        tabs.addTab("Combat", null, combatPanel, "Combat Settings");
        tabs.addTab("Eat", null, eatPanel, "Combat Settings");

        tabs.setPreferredSize(new Dimension(300, HEIGHT - 30));

        return tabs;
    }

}
