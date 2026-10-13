package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DebugMenu extends AbstractWagonMenu {

    public static final int CARGO_Y = 12;
    public static final int HEALTH_Y = 34;
    public static final int SEAT_Y = 50;
    public static final int WHEEL_Y = 51;
    public static final int INV_Y = 100;
    public static final int BANNER_X = 268;
    public static final int LANTERN_X = 286;
    public static final int ACCESSORY_Y = 110;
    private static final int PLAYER_START = 8;

    private final int bannerSlot;
    private final int lanternSlot;

    public DebugMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.DEBUG.get(), id, inventory, wagon, 4, 124, 80, INV_Y, WHEEL_Y, CARGO_Y);
        DebugWagon debugWagon = (DebugWagon)wagon;
        this.bannerSlot = slots.size();
        addSlot(new BannerSlot(debugWagon.getBannerItems(), 0, BANNER_X, ACCESSORY_Y));
        this.lanternSlot = slots.size();
        addSlot(new LanternSlot(debugWagon.getLanternItems(), 0, LANTERN_X, ACCESSORY_Y));
    }

    public DebugMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        boolean accessory = index == bannerSlot || index == lanternSlot;
        boolean fromPlayer = index >= PLAYER_START && index < bannerSlot;
        if(accessory || (fromPlayer && (item.getItem() instanceof BannerItem || item.is(Items.LANTERN)))) {
            ItemStack copy = item.copy();
            int start = accessory ? PLAYER_START : item.getItem() instanceof BannerItem ? bannerSlot : lanternSlot;
            int end = accessory ? bannerSlot : start + 1;
            if(!moveItemStackTo(item, start, end, false))
                return ItemStack.EMPTY;
            if(item.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
            return copy;
        }
        return super.quickMoveStack(player, index);
    }

}
