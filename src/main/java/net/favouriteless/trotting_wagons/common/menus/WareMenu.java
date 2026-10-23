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
    public static final int IMAGE_HEIGHT = 250;
    public static final int GRID_X = 80;
    public static final int GRID_Y = 54;
    public static final int CHEST_X = 80;
    public static final int CHEST_Y = 132;
    public static final int LANTERN_X = 152;
    public static final int LANTERN_Y = 132;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 54;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 78;
    public static final int INV_X = 80;
    public static final int INV_Y = 166;
    public static final int TAB_X = 80;
    public static final int TAB_Y = 28;

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
            addSlot(new ChestSlot(wagon.getCargo(), i, CHEST_X + i * 18, CHEST_Y));

        this.cargoStart = slots.size();
        for(int i = 0; i < WareWagon.CARGO; i++) {
            int column = i % 9;
            int row = i / 9;
            addSlot(new WareCargoSlot(this, wagon.getCargo(), WareWagon.CHESTS + i, GRID_X + column * 18, GRID_Y + row * 18));
        }

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
        boolean moved = false;
        while(moveItemStackTo(stack, start, end, false))
            moved = true;
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    private static WareWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof WareWagon wagon ? wagon : null;
    }

}
