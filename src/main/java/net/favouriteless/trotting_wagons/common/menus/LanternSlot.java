package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LanternSlot extends SlotItemHandler {

    public LanternSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(TrottingWagons.LANTERNS);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

}
