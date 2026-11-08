package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class LanternSlot extends DriverSlot {

    public LanternSlot(AbstractWagon wagon, Player viewer, IItemHandler handler, int index, int x, int y) {
        super(wagon, viewer, handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(TrottingWagons.LANTERNS) && allowed();
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
