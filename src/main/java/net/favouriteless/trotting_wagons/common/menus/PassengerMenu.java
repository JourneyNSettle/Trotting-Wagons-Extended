package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.PassengerWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PassengerMenu extends AbstractContainerMenu {

    public static final int INV_X = 80;
    public static final int INV_Y = 112;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 58;
    public static final int LANTERN_X = 142;
    public static final int LANTERN_Y = 76;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 58;
    public static final int IMAGE_HEIGHT = 196;

    private static final int WHEEL_END = AbstractWagon.WHEEL_COUNT;
    private static final int LANTERN_END = WHEEL_END + PassengerWagon.LANTERNS;

    private final PassengerWagon wagon;
    private final int repairStart;

    public PassengerMenu(int id, Inventory inventory, PassengerWagon wagon) {
        super(TWMenuTypes.PASSENGER.get(), id);
        this.wagon = wagon;
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WHEEL_X + (i % 2) * 18, WHEEL_Y + (i / 2) * 18));
        for(int i = 0; i < PassengerWagon.LANTERNS; i++)
            addSlot(new LanternSlot(wagon.getLanternItems(), i, LANTERN_X + i * 18, LANTERN_Y));

        for(int x = 0; x < 9; x++)
            addSlot(new Slot(inventory, x, INV_X + x * 18, INV_Y + 58));

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++)
                addSlot(new Slot(inventory, 9 + x + y * 9, INV_X + x * 18, INV_Y + y * 18));
        }

        this.repairStart = slots.size();
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(wagon, wagon.getRepair(), i, REPAIR_X + (i % 2) * 18, REPAIR_Y + (i / 2) * 18));
    }

    public PassengerMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public PassengerWagon getWagon() {
        return wagon;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();
        boolean fromWagon = index < LANTERN_END || index >= repairStart;
        int start;
        int end;
        if(fromWagon) {
            start = LANTERN_END;
            end = repairStart;
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            start = 0;
            end = WHEEL_END;
        } else if(item.is(Items.LANTERN)) {
            start = WHEEL_END;
            end = LANTERN_END;
        } else if(wagon != null && wagon.isRepairMaterial(item)) {
            start = repairStart;
            end = slots.size();
        } else {
            return ItemStack.EMPTY;
        }
        if(!moveItemStackTo(item, start, end, false))
            return ItemStack.EMPTY;
        if(item.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    private static PassengerWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof PassengerWagon wagon ? wagon : null;
    }

}
