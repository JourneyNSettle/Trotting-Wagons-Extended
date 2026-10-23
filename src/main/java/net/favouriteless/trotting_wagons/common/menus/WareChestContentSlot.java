package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.wagons.WareWagon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** One cell inside a placed chest. Active only on that chest's tab. */
public class WareChestContentSlot extends SlotItemHandler {

    private final WareMenu menu;
    private final WareWagon wagon;
    private final int chest;

    public WareChestContentSlot(WareMenu menu, WareWagon wagon, IItemHandler handler, int index, int chest, int x, int y) {
        super(handler, index, x, y);
        this.menu = menu;
        this.wagon = wagon;
        this.chest = chest;
    }

    @Override
    public boolean isActive() {
        return wagon != null && wagon.hasChest(chest) && menu.tab() == chest + 1;
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
