package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.BarrelWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BarrelMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 320;
    public static final int BARREL_Y = 20;
    public static final int LEFT_BARREL_X = 13;
    /** Barrel slot of the second column, centered on the 320-wide screen. */
    public static final int CENTER_BARREL_X = 151;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_W = 294;

    private final BarrelWagon wagon;
    private final int contentStart;
    private final int wheelStart;
    private final int playerStart;
    private final int repairStart;

    public BarrelMenu(int id, Inventory inventory, BarrelWagon wagon) {
        super(TWMenuTypes.BARREL.get(), id);
        this.wagon = wagon;
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < BarrelWagon.BARRELS; i++)
            addSlot(new BarrelSlot(wagon.getCargo(), i, barrelX(i), barrelY(i)));

        this.contentStart = slots.size();
        for(int barrel = 0; barrel < BarrelWagon.BARRELS; barrel++) {
            for(int offset = 0; offset < BarrelWagon.CONTENTS; offset++) {
                addSlot(new BarrelContentSlot(wagon, wagon.getCargo(), BarrelWagon.contentIndex(barrel, offset),
                        contentX(barrel, offset), barrelY(barrel)));
            }
        }

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

    public BarrelMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public static int barrelX(int index) {
        return index < BarrelWagon.BARRELS / 2 ? LEFT_BARREL_X : CENTER_BARREL_X;
    }

    public static int barrelY(int index) {
        return BARREL_Y + (index % (BarrelWagon.BARRELS / 2)) * 18;
    }

    public static int contentX(int barrel, int offset) {
        return barrelX(barrel) + 18 + offset * 18;
    }

    public BarrelWagon getWagon() {
        return wagon;
    }

    public int contentStart() {
        return contentStart;
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
        if(!slot.hasItem() || !slot.mayPickup(player))
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();
        boolean moved;
        boolean specific = false;
        if(index < wheelStart || index >= repairStart || (index >= wheelStart && index < playerStart)) {
            moved = moveItemStackTo(item, playerStart, repairStart, false);
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            moved = fill(item, wheelStart, playerStart);
            specific = true;
        } else if(BarrelWagon.isBarrel(copy) || (wagon != null && wagon.isRepairMaterial(item))) {
            boolean took = false;
            if(BarrelWagon.isBarrel(copy))
                took = fill(item, 0, contentStart);
            if(!item.isEmpty() && wagon != null && wagon.isRepairMaterial(item))
                took = fill(item, repairStart, slots.size()) || took;
            if(took) {
                moved = true;
                specific = true;
            } else {
                moved = moveItemStackTo(item, contentStart, wheelStart, false);
            }
        } else {
            moved = moveItemStackTo(item, contentStart, wheelStart, false);
        }
        if(!moved)
            return ItemStack.EMPTY;
        if(item.isEmpty()) {
            if(index < BarrelWagon.BARRELS && BarrelWagon.isBarrel(copy) && wagon != null)
                wagon.dropBarrelContents(slot.getSlotIndex());
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return specific ? ItemStack.EMPTY : copy;
    }

    /** Places into every slot in the range that will take the stack. One call of {@code moveItemStackTo} fills only one empty slot. */
    private boolean fill(ItemStack stack, int start, int end) {
        boolean moved = false;
        while(moveItemStackTo(stack, start, end, false))
            moved = true;
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    private static BarrelWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof BarrelWagon wagon ? wagon : null;
    }

}
