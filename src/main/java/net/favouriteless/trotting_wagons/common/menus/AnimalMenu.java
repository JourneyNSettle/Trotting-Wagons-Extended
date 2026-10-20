package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnimalMenu extends AbstractContainerMenu {

    public static final int INV_X = 80;
    public static final int INV_Y = 112;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 112;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 83;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 16;
    public static final int HEALTH_W = 294;
    public static final int IMAGE_HEIGHT = 196;

    private static final int PLAYER_START = AbstractWagon.WHEEL_COUNT;

    private final AbstractWagon wagon;
    private final int repairStart;

    public AnimalMenu(int id, Inventory inventory, AbstractWagon wagon) {
        super(TWMenuTypes.ANIMAL.get(), id);
        this.wagon = wagon;
        if(!wagon.level().isClientSide)
            wagon.refreshRepairSlots();

        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WHEEL_X + (i % 2) * 18, WHEEL_Y + (i / 2) * 18));

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

    public AnimalMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    public AbstractWagon getWagon() {
        return wagon;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        ItemStack copy = item.copy();
        boolean fromWagon = index < PLAYER_START || index >= repairStart;
        int start;
        int end;
        if(fromWagon) {
            start = PLAYER_START;
            end = repairStart;
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            start = 0;
            end = PLAYER_START;
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

    private static AbstractWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof AbstractWagon wagon ? wagon : null;
    }

}
