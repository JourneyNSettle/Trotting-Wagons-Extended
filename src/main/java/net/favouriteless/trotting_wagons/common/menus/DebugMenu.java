package net.favouriteless.trotting_wagons.common.menus;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.init.TWMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DebugMenu extends AbstractWagonMenu {

    public static final int CARGO_X = 125;
    public static final int CARGO_Y = 22;
    public static final int HEALTH_X = 13;
    public static final int HEALTH_Y = 46;
    public static final int HEALTH_W = 294;
    public static final int SEAT_X0 = 108;
    public static final int SEAT_Y = 61;
    public static final int DRAFT_Y = 61;
    public static final int DRAFT_SINGLE_X = 21;
    public static final int DRAFT_PAIR_X = 8;
    public static final int DRAFT_STRIDE = 26;
    public static final int WHEEL_X = 17;
    public static final int WHEEL_Y = 153;
    public static final int INV_X = 80;
    public static final int INV_Y = 113;
    public static final int HARNESS_X = 26;
    public static final int HARNESS_Y = 105;
    public static final int BANNER_X = 269;
    public static final int LANTERN_X = 287;
    public static final int ACCESSORY_Y = 153;
    public static final int REPAIR_X = 269;
    public static final int REPAIR_Y = 80;
    private static final int PLAYER_START = 8;

    private final int bannerSlot;
    private final int lanternSlot;
    private final int harnessSlot;

    public DebugMenu(int id, Inventory inventory, AbstractInventoryWagon wagon) {
        super(TWMenuTypes.DEBUG.get(), id, inventory, wagon, 4, CARGO_X, INV_X, INV_Y, WHEEL_Y, CARGO_Y, WHEEL_X);
        if(!wagon.level().isClientSide)
            wagon.refreshRepairSlots();
        DebugWagon debugWagon = (DebugWagon)wagon;
        this.bannerSlot = slots.size();
        addSlot(new BannerSlot(debugWagon.getBannerItems(), 0, BANNER_X, ACCESSORY_Y));
        this.lanternSlot = slots.size();
        addSlot(new LanternSlot(debugWagon.getLanternItems(), 0, LANTERN_X, ACCESSORY_Y));
        this.harnessSlot = slots.size();
        addSlot(new HarnessSlot(debugWagon, debugWagon.getHarnessItems(), 0, HARNESS_X, HARNESS_Y));
        for(int i = 0; i < AbstractWagon.REPAIR_SLOTS; i++)
            addSlot(new RepairSlot(debugWagon, debugWagon.getRepair(), i, REPAIR_X + (i % 2) * 18, REPAIR_Y + (i / 2) * 18));
    }

    public static int draftX(int slots, int index) {
        if(slots < 2)
            return DRAFT_SINGLE_X;
        return DRAFT_PAIR_X + index * DRAFT_STRIDE;
    }

    public DebugMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getWagon(inventory, data));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if(!slot.hasItem() || !slot.mayPickup(player))
            return ItemStack.EMPTY;
        ItemStack item = slot.getItem();
        int repairStart = harnessSlot + 1;
        boolean accessory = index == bannerSlot || index == lanternSlot;
        boolean fromHarness = index == harnessSlot;
        boolean fromRepair = index >= repairStart;
        boolean fromPlayer = index >= PLAYER_START && index < bannerSlot;
        AbstractWagon wagon = getWagon();
        boolean harnessItem = TWItems.isHarness(item);
        if(fromRepair || accessory || fromHarness || (fromPlayer && (item.getItem() instanceof BannerItem || item.is(Items.LANTERN) || harnessItem || wagon != null && wagon.isRepairMaterial(item)))) {
            ItemStack copy = item.copy();
            int start = fromRepair || accessory || fromHarness ? PLAYER_START : item.getItem() instanceof BannerItem ? bannerSlot : item.is(Items.LANTERN) ? lanternSlot : harnessItem ? harnessSlot : repairStart;
            int end = fromRepair || accessory || fromHarness ? bannerSlot : start == repairStart ? slots.size() : start + 1;
            boolean moved = start == repairStart
                    ? RepairShift.fill(item, start, end, this::moveItemStackTo)
                    : moveItemStackTo(item, start, end, false);
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
