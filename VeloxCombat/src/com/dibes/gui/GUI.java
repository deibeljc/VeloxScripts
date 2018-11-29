package com.dibes.gui;

import javax.swing.*;

import com.dibes.VeloxCombat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
        JPanel combatPanel = getCombatPanel();

        JPanel eatPanel = getEatPanel();

        tabs.addTab("Combat", null, combatPanel, "Combat Settings");
        tabs.addTab("Eat", null, eatPanel, "Combat Settings");

        tabs.setPreferredSize(new Dimension(300, HEIGHT - 30));

        return tabs;
    }

    private JPanel getEatPanel() {
        Dimension size;

        JPanel eatPanel = new JPanel(false);
        eatPanel.setLayout(null);

        JCheckBox shouldEat = new JCheckBox("Should Eat");
        shouldEat.setSelected(false);
        shouldEat.addActionListener(e -> state.setShouldEat(shouldEat.isSelected()));
        size = shouldEat.getPreferredSize();
        shouldEat.setBounds(10, 10, size.width, size.height);


        JSlider eatSlider = new JSlider(10, 100, 40);
        eatSlider.setPaintTicks(true);
        eatSlider.setPaintLabels(true);
        eatSlider.setMajorTickSpacing(20);
        eatSlider.setMinorTickSpacing(5);
        eatSlider.addChangeListener(e -> state.setEatPercent(eatSlider.getValue()));
        size = eatSlider.getPreferredSize();
        eatSlider.setBounds(100, 13, 200, size.height);

        JLabel foodNameLabel = new JLabel("Food name");
        size = foodNameLabel.getPreferredSize();
        foodNameLabel.setBounds(10, 70, size.width, size.height);

        JTextField foodName = new JTextField(30);
        foodName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\b') {
                    state.setFoodName(
                        foodName.getText().substring(0, foodName.getText().length() - 2)
                    );
                } else {
                    state.setFoodName(foodName.getText() + e.getKeyChar());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        size = foodName.getPreferredSize();
        foodName.setBounds(100, 70, 200, size.height);

        eatPanel.add(shouldEat);
        eatPanel.add(eatSlider);
        eatPanel.add(foodNameLabel);
        eatPanel.add(foodName);

        return eatPanel;
    }

    private JPanel getCombatPanel() {

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
        return combatPanel;
    }

}
