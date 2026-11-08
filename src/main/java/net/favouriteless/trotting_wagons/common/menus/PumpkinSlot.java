package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

/** Debug-wagon pumpkins. They sit with the other custom fittings. */
public class PumpkinSlot extends DriverSlot {

    public PumpkinSlot(AbstractWagon wagon, Player viewer, IItemHandler handler, int index, int x, int y) {
        super(wagon, viewer, handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Items.PUMPKIN) && allowed();
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
