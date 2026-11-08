package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** Tongue or shafts. Stays put while any animal is hitched. */
public class HarnessSlot extends DriverSlot {

    private final AbstractWagon wagon;

    public HarnessSlot(AbstractWagon wagon, Player viewer, IItemHandler handler, int index, int x, int y) {
        super(wagon, viewer, handler, index, x, y);
        this.wagon = wagon;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !wagon.hasDraftAnimal() && TWItems.isHarness(stack) && allowed();
    }

    @Override
    public boolean mayPickup(Player player) {
        return !wagon.hasDraftAnimal() && super.mayPickup(player);
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
