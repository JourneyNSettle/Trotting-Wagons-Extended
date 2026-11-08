package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** A fitting only the seated driver may change. An empty driver seat leaves it open. */
class DriverSlot extends SlotItemHandler {

    private final AbstractWagon wagon;
    private final Player viewer;

    DriverSlot(AbstractWagon wagon, Player viewer, IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.wagon = wagon;
        this.viewer = viewer;
    }

    protected boolean allowed() {
        return wagon == null || wagon.driverAllows(viewer);
    }

    @Override
    public boolean mayPickup(Player player) {
        return wagon == null || wagon.driverAllows(player);
    }

}
