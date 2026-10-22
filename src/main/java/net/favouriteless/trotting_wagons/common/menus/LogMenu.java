package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.LogWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LogMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 15;
    public static final int IMAGE_WIDTH = 320;
    public static final int CARGO_X = 25;
    public static final int CARGO_Y = 20;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_W = 294;

    private final LogWagon wagon;
    private final int wheelStart;
    private final int playerStart;
    private final int repairStart;

    public LogMenu(int id, Inventory inventory, LogWagon wagon) {
        super(TWMenuTypes.LOG.get(), id);
        this.wagon = wagon;
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < LogWagon.TYPE_SLOTS; i++)
            addSlot(new LogCargoSlot(wagon, wagon.getCargo(), i, CARGO_X, CARGO_Y));

        this.wheelStart = slots.size();
        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, 17, 80 + (i / 2) * 18));

        this.playerStart = slots.size();
        for(int x = 0; x < 9; x++)
            addSlot(new Slot(inventory, x, 80 + x * 18, 80 + 58));
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++)
                addSlot(new Slot(inventory, 9 + x + y * 9, 80 + x * 18, 80 + y * 18));
        }

        this.repairStart = slots.size();
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(wagon, wagon.getRepair(), i, 270, 80 + (i / 2) * 18));
    }

    public LogMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public LogWagon getWagon() {
        return wagon;
    }

    public int wheelStart() {
        return wheelStart;
    }

    public int playerStart() {
        return playerStart;
    }

    public int repairStart() {
        return repairStart;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();
        boolean moved;
        if(index < wheelStart || index >= repairStart || (index >= wheelStart && index < playerStart)) {
            moved = moveItemStackTo(item, playerStart, repairStart, false);
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            moved = moveItemStackTo(item, wheelStart, playerStart, false);
        } else if(LogWagon.acceptsCargo(item)) {
            moved = moveItemStackTo(item, 0, wheelStart, false);
            if(!item.isEmpty() && wagon != null && wagon.isRepairMaterial(item))
                moved = moveItemStackTo(item, repairStart, slots.size(), false) || moved;
        } else if(wagon != null && wagon.isRepairMaterial(item)) {
            moved = moveItemStackTo(item, repairStart, slots.size(), false);
        } else {
            return ItemStack.EMPTY;
        }
        if(!moved)
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

    private static LogWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof LogWagon wagon ? wagon : null;
    }

}
