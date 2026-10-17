package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
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

    public static final int CARGO_X = 126;
    public static final int CARGO_Y = 21;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 43;
    public static final int HEALTH_W = 294;
    public static final int SEAT_Y = 64;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 117;
    public static final int INV_Y = 112;
    public static final int BANNER_X = 270;
    public static final int LANTERN_X = 288;
    public static final int ACCESSORY_Y = 152;
    public static final int REPAIR_X = 270;
    public static final int REPAIR_Y = 83;
    private static final int PLAYER_START = 8;

    private final int bannerSlot;
    private final int lanternSlot;

    public DebugMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.DEBUG.get(), id, inventory, wagon, 4, CARGO_X, 80, INV_Y, WHEEL_Y, CARGO_Y, WHEEL_X);
        if(!wagon.level().isClientSide)
            wagon.refreshRepairSlots();
        DebugWagon debugWagon = (DebugWagon)wagon;
        this.bannerSlot = slots.size();
        addSlot(new BannerSlot(debugWagon.getBannerItems(), 0, BANNER_X, ACCESSORY_Y));
        this.lanternSlot = slots.size();
        addSlot(new LanternSlot(debugWagon.getLanternItems(), 0, LANTERN_X, ACCESSORY_Y));
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(debugWagon, debugWagon.getRepair(), i, REPAIR_X + (i % 2) * 18, REPAIR_Y + (i / 2) * 18));
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
        int repairStart = lanternSlot + 1;
        boolean accessory = index == bannerSlot || index == lanternSlot;
        boolean fromRepair = index >= repairStart;
        boolean fromPlayer = index >= PLAYER_START && index < bannerSlot;
        AbstractWagon wagon = getWagon();
        if(fromRepair || accessory || (fromPlayer && (item.getItem() instanceof BannerItem || item.is(Items.LANTERN) || wagon != null && wagon.isRepairMaterial(item)))) {
            ItemStack copy = item.copy();
            int start = fromRepair || accessory ? PLAYER_START : item.getItem() instanceof BannerItem ? bannerSlot : item.is(Items.LANTERN) ? lanternSlot : repairStart;
            int end = fromRepair || accessory ? bannerSlot : start == repairStart ? slots.size() : start + 1;
            boolean moved = moveItemStackTo(item, start, end, false);
            if(!moved)
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
