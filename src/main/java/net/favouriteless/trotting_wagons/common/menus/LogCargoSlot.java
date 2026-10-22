package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.wagons.LogWagon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LogCargoSlot extends SlotItemHandler {

    private final LogWagon wagon;

    public LogCargoSlot(LogWagon wagon, IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.wagon = wagon;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return wagon.roomIn(getSlotIndex());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        wagon.mergeSameKinds();
        wagon.syncCargo();
    }

    @Override
    public boolean isActive() {
        return hasItem() || getSlotIndex() == wagon.placeholderSlot();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive() && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return isActive();
    }

}
