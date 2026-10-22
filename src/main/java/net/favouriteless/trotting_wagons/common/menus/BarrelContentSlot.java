package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.wagons.BarrelWagon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BarrelContentSlot extends SlotItemHandler {

    private final BarrelWagon wagon;
    private final int barrel;

    public BarrelContentSlot(BarrelWagon wagon, IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.wagon = wagon;
        this.barrel = BarrelWagon.barrelOf(index);
    }

    @Override
    public boolean isActive() {
        return wagon != null && wagon.barrelFilled(barrel);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive() && !stack.isEmpty();
    }

    @Override
    public boolean mayPickup(Player player) {
        return isActive() && super.mayPickup(player);
    }

}
