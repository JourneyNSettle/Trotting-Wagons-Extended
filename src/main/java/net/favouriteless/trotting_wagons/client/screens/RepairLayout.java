package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.minecraft.world.inventory.Slot;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** Puts the open repair slots in a group centered under the repair header. */
public final class RepairLayout {

    /** Slot interior sits this far under the header line: one condition line, then a gap. */
    public static final int UNDER_HEADER = 22;

    private RepairLayout() {}

    public static void place(Iterable<Slot> slots, AbstractWagon wagon, int centerX, int headerY) {
        int count = wagon == null ? 0 : wagon.repairSlots();
        int seen = 0;
        for(Slot slot : slots) {
            if(!(slot instanceof RepairSlot))
                continue;
            if(seen >= count) {
                move(slot, -2000, -2000);
            } else {
                int[] at = offset(seen, count);
                move(slot, centerX + at[0], headerY + UNDER_HEADER + at[1]);
            }
            seen++;
        }
    }

    /** Interior offset from the header center and the first row. */
    private static int[] offset(int index, int count) {
        if(count <= 1)
            return new int[] {-8, 0};
        if(count == 3 && index == 2)
            return new int[] {-8, 18};
        return new int[] {-17 + (index % 2) * 18, (index / 2) * 18};
    }

    private static void move(Slot slot, int x, int y) {
        Move.UNSAFE.putInt(slot, Move.X, x);
        Move.UNSAFE.putInt(slot, Move.Y, y);
    }

    private static final class Move {
        private static final Unsafe UNSAFE;
        private static final long X;
        private static final long Y;

        static {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                UNSAFE = (Unsafe)field.get(null);
                X = UNSAFE.objectFieldOffset(Slot.class.getDeclaredField("x"));
                Y = UNSAFE.objectFieldOffset(Slot.class.getDeclaredField("y"));
            } catch(ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }

}
