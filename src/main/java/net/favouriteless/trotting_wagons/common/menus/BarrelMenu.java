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
    public static final int IMAGE_HEIGHT = 270;
    public static final int BARREL_Y = 6;
    public static final int LEFT_BARREL_X = 14;
    public static final int CENTER_BARREL_X = 169;
    /** Background between a barrel frame and the frame of its first item slot. */
    public static final int CONTENT_GAP = 8;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 117;
    public static final int HEALTH_W = 294;
    /** Kibitka gap: 8 pixels under the 10-pixel bar, then the condition 10 pixels lower. */
    public static final int DURABILITY_Y = HEALTH_Y + 18;
    public static final int DRAFT_X = 14;
    public static final int DRAFT_Y = 135;
    public static final int DRAFT_STRIDE = 26;
    public static final int SEAT_X = 80;
    public static final int SEAT_Y = 135;
    /** First pixel of the wagon name, to the right of the driver well. */
    public static final int TITLE_X = 112;
    public static final int TITLE_Y = 137;
    /** Pixels of background between the wagon name and the distance counter. */
    public static final int DISTANCE_GAP = 27;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 186;
    public static final int INV_X = 80;
    public static final int INV_Y = 186;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 186;
    /** Painted lock. No widget until the button is wired. */
    public static final int LOCK_X = 20;
    public static final int LOCK_Y = 234;
    public static final int LOCK_SIZE = 26;

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
            addSlot(new WheelSlot(wagon.getWheels(), i, WHEEL_X + (i % 2) * 18, WHEEL_Y + (i / 2) * 18));

        this.playerStart = slots.size();
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
        return barrelX(barrel) + 16 + 1 + CONTENT_GAP + 1 + offset * 18;
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
        return RepairShift.fill(stack, start, end, this::moveItemStackTo);
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    private static BarrelWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof BarrelWagon wagon ? wagon : null;
    }

}
