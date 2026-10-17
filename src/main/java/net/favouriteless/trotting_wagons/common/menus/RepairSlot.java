package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RepairSlot extends SlotItemHandler {

    private final AbstractWagon wagon;

    public RepairSlot(AbstractWagon wagon, IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.wagon = wagon;
    }

    @Override
    public boolean isActive() {
        return getSlotIndex() < wagon.repairSlots();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive() && wagon.acceptsRepair(getSlotIndex(), stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
