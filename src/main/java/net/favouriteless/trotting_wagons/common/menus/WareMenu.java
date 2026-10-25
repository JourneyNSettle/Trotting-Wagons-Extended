package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.WareWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WareMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 320;
    public static final int IMAGE_HEIGHT = 267;
    /** Nine columns, three rows, lined up with the player inventory. */
    public static final int GRID_COLUMNS = 9;
    public static final int GRID_X = 80;
    public static final int GRID_Y = 55;
    /** Vertical 16×16 chest item slots, left of the grid. */
    public static final int CHEST_X = 48;
    public static final int CHEST_Y = 55;
    public static final int CHEST_STEP = 18;
    public static final int LANTERN_X = 279;
    public static final int LANTERN_Y = 223;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 183;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 184;
    public static final int INV_X = 80;
    public static final int INV_Y = 183;
    public static final int TAB_X = 102;
    public static final int TAB_Y = 22;
    public static final int TAB_STEP = 30;
    /** Empty band under the chest column and the 27-slot grid, above the draft row. */
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 114;
    public static final int HEALTH_W = 294;
    public static final int DRAFT_X = 14;
    public static final int DRAFT_Y = 132;
    public static final int DRAFT_STEP = 26;
    public static final int SEAT_X = 147;
    public static final int SEAT_Y = 132;
    public static final int LOCK_X = 20;
    public static final int LOCK_Y = 231;
    public static final int LOCK_SIZE = 26;

    public static int gridX(int column) {
        return GRID_X + column * 18;
    }

    public static int gridY(int row) {
        return GRID_Y + row * 18;
    }

    private final WareWagon wagon;
    private final int chestStart;
    private final int cargoStart;
    private final int contentStart;
    private final int playerStart;
    private final int repairStart;
    private int tab;

    public WareMenu(int id, Inventory inventory, WareWagon wagon) {
        super(TWMenuTypes.WARE.get(), id);
        this.wagon = wagon;
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WHEEL_X + (i % 2) * 18, WHEEL_Y + (i / 2) * 18));
        addSlot(new LanternSlot(wagon.getLanternItems(), 0, LANTERN_X, LANTERN_Y));

        this.chestStart = slots.size();
        for(int i = 0; i < WareWagon.CHESTS; i++)
            addSlot(new ChestSlot(wagon.getCargo(), i, CHEST_X, CHEST_Y + i * CHEST_STEP));

        this.cargoStart = slots.size();
        for(int i = 0; i < WareWagon.CARGO; i++)
            addSlot(new WareCargoSlot(this, wagon.getCargo(), WareWagon.CHESTS + i,
                    gridX(i % GRID_COLUMNS), gridY(i / GRID_COLUMNS)));

        this.contentStart = slots.size();
        for(int chest = 0; chest < WareWagon.CHESTS; chest++) {
            for(int offset = 0; offset < WareWagon.CONTENTS; offset++)
                addSlot(new WareChestContentSlot(this, wagon, wagon.getCargo(), WareWagon.contentIndex(chest, offset), chest, -2000, -2000));
        }

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

    public WareMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public WareWagon getWagon() {
        return wagon;
    }

    public int tab() {
        return tab;
    }

    public void setTab(int tab) {
        if(tab < 0 || tab > WareWagon.CHESTS)
            tab = 0;
        if(tab > 0 && (wagon == null || !wagon.hasChest(tab - 1)))
            tab = 0;
        this.tab = tab;
    }

    public int chestStart() {
        return chestStart;
    }

    public int cargoStart() {
        return cargoStart;
    }

    public int contentStart() {
        return contentStart;
    }

    public int playerStart() {
        return playerStart;
    }

    public int openStart() {
        return tab == 0 ? cargoStart : contentStart + (tab - 1) * WareWagon.CONTENTS;
    }

    public int openEnd() {
        return tab == 0 ? contentStart : openStart() + WareWagon.CONTENTS;
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
        if(index < playerStart || index >= repairStart) {
            moved = moveItemStackTo(item, playerStart, repairStart, false);
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            moved = fill(item, 0, AbstractWagon.WHEEL_COUNT);
            specific = true;
        } else if(item.is(Items.LANTERN)) {
            moved = fill(item, AbstractWagon.WHEEL_COUNT, chestStart);
            specific = true;
        } else if(WareWagon.isChest(copy) || (wagon != null && wagon.isRepairMaterial(item))) {
            boolean took = false;
            if(WareWagon.isChest(copy))
                took = fill(item, chestStart, cargoStart);
            if(!item.isEmpty() && wagon != null && wagon.isRepairMaterial(item))
                took = fill(item, repairStart, slots.size()) || took;
            if(took) {
                moved = true;
                specific = true;
            } else {
                moved = fill(item, openStart(), openEnd());
            }
        } else {
            moved = fill(item, openStart(), openEnd());
        }
        if(!moved)
            return ItemStack.EMPTY;
        if(item.isEmpty()) {
            if(index >= chestStart && index < cargoStart && WareWagon.isChest(copy) && wagon != null)
                wagon.dropChestContents(slot.getSlotIndex());
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

    private static WareWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof WareWagon wagon ? wagon : null;
    }

}
