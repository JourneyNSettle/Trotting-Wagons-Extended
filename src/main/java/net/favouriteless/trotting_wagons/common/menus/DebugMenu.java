package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class DebugMenu extends AbstractWagonMenu {

    public static final int SEAT_Y = 56;
    public static final int WHEEL_Y = 57;
    public static final int INV_Y = 128;

    public DebugMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.DEBUG.get(), id, inventory, wagon, 4, 124, 80, INV_Y, WHEEL_Y);
    }

    public DebugMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

}
