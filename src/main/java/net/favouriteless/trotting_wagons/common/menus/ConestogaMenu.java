package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ConestogaMenu extends AbstractWagonMenu {

    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 226;

    public ConestogaMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.CONESTOGA.get(), id, inventory, wagon, 17, 8, 80, 185, 144);
        addRepairSlots(REPAIR_X, REPAIR_Y);
    }

    public ConestogaMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

}
