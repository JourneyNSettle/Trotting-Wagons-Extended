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

public class ArmoredMenu extends AbstractContainerMenu {

    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 113;

    private static final int INV_X = 80;
    private static final int INV_Y = 77;

    private final AbstractWagon wagon;
    private final int repairStart;

    public ArmoredMenu(int id, Inventory inventory, AbstractWagon wagon) {
        super(TWMenuTypes.ARMORED.get(), id);
        this.wagon = wagon;

        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WagonLayout.wheelX(i), WagonLayout.wheelY(29, i)));

        for(int x = 0; x < 9; x++)
            addSlot(new Slot(inventory, x, INV_X + x * 18, INV_Y + 58));

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++)
                addSlot(new Slot(inventory, 9 + x + y * 9, INV_X + x * 18, INV_Y + y * 18));
        }

        this.repairStart = slots.size();
        if(wagon != null && !wagon.level().isClientSide)
            wagon.refreshRepairSlots();
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(wagon, wagon.getRepair(), i, REPAIR_X + (i % 2) * 18, REPAIR_Y + (i / 2) * 18));
    }

    public ArmoredMenu(int id, Inventory inventory, FriendlyByteBuf data) {
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
        int wheelEnd = AbstractWagon.WHEEL_COUNT;
        boolean fromWagon = index < wheelEnd || index >= repairStart;
        boolean moved;
        boolean repairHeld = false;
        if(fromWagon) {
            moved = moveItemStackTo(item, wheelEnd, repairStart, false);
        } else if(item.is(TrottingWagons.WAGON_WHEELS)) {
            moved = moveItemStackTo(item, 0, wheelEnd, false);
        } else if(wagon != null && wagon.isRepairMaterial(item)
                && RepairShift.fill(item, repairStart, slots.size(), this::moveItemStackTo)) {
            moved = true;
            repairHeld = true;
        } else {
            return ItemStack.EMPTY;
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

    private static AbstractWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof AbstractWagon wagon ? wagon : null;
    }

}
