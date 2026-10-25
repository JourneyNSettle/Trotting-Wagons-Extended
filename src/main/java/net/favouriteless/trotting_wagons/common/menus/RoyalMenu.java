package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class RoyalMenu extends AbstractWagonMenu {

    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 40;

    public RoyalMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.ROYAL.get(), id, inventory, wagon, 9, 80, 80, 193, 152);
        addRepairSlots(REPAIR_X, REPAIR_Y);
    }

    public RoyalMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

}
