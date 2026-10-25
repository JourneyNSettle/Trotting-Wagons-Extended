package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class KibitkaMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 320;
    public static final int IMAGE_HEIGHT = 234;
    public static final int CARGO_COLUMNS = 15;
    public static final int CARGO_X = 26;
    public static final int CARGO_Y = 22;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 82;
    public static final int HEALTH_W = 294;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 150;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 122;
    public static final int LANTERN_X = 279;
    public static final int LANTERN_Y = 190;
    public static final int INV_X = 80;
    public static final int INV_Y = 150;
    public static final int CUSTOM_Y = 164;

    private static final int WHEEL_END = AbstractWagon.WHEEL_COUNT;
    private static final int LANTERN_END = WHEEL_END + 1;
    private static final int CARGO_END = LANTERN_END + KibitkaWagon.CARGO;

    private final KibitkaWagon wagon;
    private final int playerStart;
    private final int repairStart;

    public KibitkaMenu(int id, Inventory inventory, KibitkaWagon wagon) {
        super(TWMenuTypes.KIBITKA.get(), id);
        this.wagon = wagon;
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WHEEL_X + (i % 2) * 18, WHEEL_Y + (i / 2) * 18));
        addSlot(new LanternSlot(wagon.getLanternItems(), 0, LANTERN_X, LANTERN_Y));
        for(int i = 0; i < KibitkaWagon.CARGO; i++)
            addSlot(new CargoSlot(wagon.getItems(), i, CARGO_X + (i % CARGO_COLUMNS) * 18, CARGO_Y + (i / CARGO_COLUMNS) * 18));

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

    public KibitkaMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public KibitkaWagon getWagon() {
        return wagon;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();
        boolean fromWagon = index < CARGO_END || index >= repairStart;
        boolean moved;
        boolean repairHeld = false;
        if(fromWagon) {
            moved = moveItemStackTo(item, playerStart, repairStart, false);
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            moved = moveItemStackTo(item, 0, WHEEL_END, false);
        } else if(item.is(Items.LANTERN)) {
            moved = moveItemStackTo(item, WHEEL_END, LANTERN_END, false);
        } else if(wagon != null && wagon.isRepairMaterial(item)
                && RepairShift.fill(item, repairStart, slots.size(), this::moveItemStackTo)) {
            moved = true;
            repairHeld = true;
        } else {
            moved = moveItemStackTo(item, LANTERN_END, CARGO_END, false);
        }
        if(!moved)
            return ItemStack.EMPTY;
        if(item.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();
        return repairHeld ? ItemStack.EMPTY : copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    private static KibitkaWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof KibitkaWagon wagon ? wagon : null;
    }

    /** General cargo. Wheels and lanterns stay in their own slots. */
    private static final class CargoSlot extends SlotItemHandler {

        private CargoSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.is(TrottingWagons.WAGON_WHEELS) && !stack.is(Items.LANTERN);
        }

    }

}
