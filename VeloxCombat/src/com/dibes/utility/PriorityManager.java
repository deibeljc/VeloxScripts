package com.dibes.utility;

/**
 *
 * setPriority(PriorityNode)
 *  (1-3)   [Fight, Walking, Eat]
 *  (4-6) [Walking, Equip, Eat]
 *  (7-10)   [Banking]
 *
 * getPriority(Node)
 *
 * Low 0
 * Normal 1
 * High 2
 * Highest 3
 *
 */
public class PriorityManager {
    public static int getPriority(Priorities priorities) {
        return priorities.getMainPriority().getPriority() * Priority.values().length
                + (1 + priorities.getSubPriority().getPriority());
    }
}
