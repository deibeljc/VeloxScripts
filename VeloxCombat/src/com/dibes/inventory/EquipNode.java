package com.dibes.inventory;

import com.dibes.utility.Priorities;
import com.dibes.utility.Priority;
import com.dibes.utility.PriorityNode;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;

public class EquipNode extends PriorityNode {

    @Override
    public Priorities getPriorities() {
        return new Priorities(Priority.NORMAL, Priority.NORMAL);
    }

    @Override
    public boolean accept() {
        boolean hasSword = getEquipment().contains(item -> item != null
                && item.getName().contains("sword"));
        boolean hasShield = getEquipment().contains(item -> item != null
                && item.getName().contains("shield"));

        return (!hasSword || !hasShield)
                && getInventory().contains(item -> item != null
                    && (item.getName().contains("sword") || item.getName().contains("shield")));
    }

    @Override
    public int execute() {
        getEquipment().equip(EquipmentSlot.WEAPON, item -> item != null && item.getName().contains("sword"));
        getEquipment().equip(EquipmentSlot.SHIELD, item -> item != null && item.getName().contains("shield"));
        return 0;
    }
}
