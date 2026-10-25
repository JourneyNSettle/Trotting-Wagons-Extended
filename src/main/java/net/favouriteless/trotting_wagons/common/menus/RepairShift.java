package net.favouriteless.trotting_wagons.common.menus;

import net.minecraft.world.item.ItemStack;

/**
 * Fills every slot in a range. {@code AbstractContainerMenu#moveItemStackTo} places into one empty slot and stops.
 */
public final class RepairShift {

    private RepairShift() {}

    public static boolean fill(ItemStack stack, int start, int end, Mover mover) {
        boolean moved = false;
        int guard = Math.max(0, end - start);
        while(!stack.isEmpty() && guard-- > 0 && mover.move(stack, start, end, false))
            moved = true;
        return moved;
    }

    @FunctionalInterface
    public interface Mover {
        boolean move(ItemStack stack, int start, int end, boolean reverse);
    }

}
