package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.BarrelWagon;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BarrelMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 320;
    public static final int IMAGE_HEIGHT = 250;
    /** Three columns by four rows, barrel items only. */
    public static final int BARREL_COLUMNS = 3;
    public static final int BARREL_X = 20;
    public static final int BARREL_Y = 15;
    /** Twelve columns by four rows. Each barrel opens the four wells in its column group. */
    public static final int CONTENT_COLUMNS = 12;
    public static final int CONTENT_X = 86;
    public static final int CONTENT_Y = 15;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 96;
    public static final int HEALTH_W = 294;
    public static final int DRAFT_Y = 114;
    public static final int DRAFT_SINGLE_X = 21;
    public static final int DRAFT_PAIR_X = 8;
    public static final int DRAFT_STRIDE = 26;
    public static final int SEAT_X = 69;
    public static final int SEAT_Y = 114;
    public static final int TITLE_X = 101;
    public static final int TITLE_Y = 116;
    public static final int DISTANCE_GAP = 27;
    public static final int HARNESS_X = 26;
    public static final int HARNESS_Y = 158;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 206;
    public static final int INV_X = 80;
    public static final int INV_Y = 166;
    public static final int REPAIR_X = 269;

    private final BarrelWagon wagon;
    private final int contentStart;
    private final int wheelStart;
    private final int playerStart;
    private final int repairStart;
    private final int harnessSlot;

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
                        contentX(barrel, offset), contentY(barrel, offset)));
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

        this.harnessSlot = slots.size();
        addSlot(new HarnessSlot(wagon, wagon.getHarnessItems(), 0, HARNESS_X, HARNESS_Y));
        this.repairStart = slots.size();
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(wagon, wagon.getRepair(), i, REPAIR_X, SEAT_Y));
    }

    public static int draftX(int slots, int index) {
        if(slots < 2)
            return DRAFT_SINGLE_X;
        return DRAFT_PAIR_X + index * DRAFT_STRIDE;
    }

    public BarrelMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public static int barrelX(int index) {
        return BARREL_X + (index % BARREL_COLUMNS) * 18;
    }

    public static int barrelY(int index) {
        return BARREL_Y + (index / BARREL_COLUMNS) * 18;
    }

    public static int contentX(int barrel, int offset) {
        int index = barrel * BarrelWagon.CONTENTS + offset;
        return CONTENT_X + (index % CONTENT_COLUMNS) * 18;
    }

    public static int contentY(int barrel, int offset) {
        int index = barrel * BarrelWagon.CONTENTS + offset;
        return CONTENT_Y + (index / CONTENT_COLUMNS) * 18;
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
        if(index == harnessSlot || index < wheelStart || index >= repairStart || (index >= wheelStart && index < playerStart)) {
            moved = moveItemStackTo(item, playerStart, harnessSlot, false);
        } else if(TWItems.isHarness(item)) {
            moved = fill(item, harnessSlot, harnessSlot + 1);
            specific = true;
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
