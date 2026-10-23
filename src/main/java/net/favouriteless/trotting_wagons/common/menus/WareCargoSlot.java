package net.favouriteless.trotting_wagons.common.menus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** One cell of the 36-slot page. Hidden while a chest tab is open. */
public class WareCargoSlot extends SlotItemHandler {

    private final WareMenu menu;

    public WareCargoSlot(WareMenu menu, IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.menu = menu;
    }

    @Override
    public boolean isActive() {
        return menu.tab() == 0;
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
