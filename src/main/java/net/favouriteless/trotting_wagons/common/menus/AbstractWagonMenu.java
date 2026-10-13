package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public abstract class AbstractWagonMenu extends AbstractContainerMenu {

    private final AbstractInventoryWagon wagon;
    private final int cargoSlots;
    private final int wheelStart;

    public AbstractWagonMenu(MenuType<? extends AbstractWagonMenu> type, int id, Inventory inventory, AbstractInventoryWagon wagon,
                             int perRow, int xSlots, int xInv, int yInv, int wheelTop) {
        this(type, id, inventory, wagon, perRow, xSlots, xInv, yInv, wheelTop, 18);
    }

    public AbstractWagonMenu(MenuType<? extends AbstractWagonMenu> type, int id, Inventory inventory, AbstractInventoryWagon wagon,
                             int perRow, int xSlots, int xInv, int yInv, int wheelTop, int cargoY) {
        super(type, id);
        this.wagon = wagon;
        this.cargoSlots = wagon.getItems().getSlots();

        for(int y = 0; y < cargoSlots / perRow; y++) {
            for(int x = 0; x < perRow; x++) {
                addSlot(new SlotItemHandler(wagon.getItems(), x + y*perRow, xSlots + x*18, cargoY + y*18));
            }
        }

        this.wheelStart = slots.size();
        for(int i = 0; i < AbstractWagon.WHEEL_COUNT; i++)
            addSlot(new WheelSlot(wagon.getWheels(), i, WagonLayout.wheelX(i), WagonLayout.wheelY(wheelTop, i)));

        for(int x = 0; x < 9; x++) {
            addSlot(new Slot(inventory, x, xInv + x*18, yInv + 58));
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 9; x++) {
                addSlot(new Slot(inventory, 9 + x + y*9, xInv + x*18, yInv + y*18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if(slot.hasItem()) {
            ItemStack item = slot.getItem();
            copy = item.copy();

            int wheelEnd = wheelStart + AbstractWagon.WHEEL_COUNT;
            if(index < wheelEnd) {
                if(!moveItemStackTo(item, wheelEnd, slots.size(), false))
                    return ItemStack.EMPTY;
            } else if(item.is(TWItems.WHEEL.get())) {
                if(!moveItemStackTo(item, wheelStart, wheelEnd, false))
                    return ItemStack.EMPTY;
            } else if(!moveItemStackTo(item, 0, cargoSlots, false)) {
                return ItemStack.EMPTY;
            }

            if(item.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return copy;
    }

    public AbstractInventoryWagon getWagon() {
        return wagon;
    }

    @Override
    public boolean stillValid(Player player) {
        return wagon != null && wagon.isAlive() && player.getVehicle() == wagon;
    }

    public static AbstractInventoryWagon getWagon(Inventory inventory, FriendlyByteBuf data) {
        return inventory.player.level().getEntity(data.readInt()) instanceof AbstractInventoryWagon wagon ? wagon : null;
    }

}
