package net.favouriteless.trotting_wagons.common.entities.wagons;

import net.favouriteless.trotting_wagons.ServerConfig;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonSeat;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.items.WagonItem;
import net.favouriteless.trotting_wagons.common.menus.BarrelMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BarrelWagon extends AbstractWagon implements MenuProvider {

    public static final int BARRELS = 12;
    public static final int CONTENTS = 6;
    public static final int SLOTS = BARRELS + BARRELS * CONTENTS;
    public static final int PARTIAL_LIMIT = 6;

    private static final int BARREL_MASK = (1 << BARRELS) - 1;
    private static final EntityDataAccessor<Integer> DATA_BARRELS = SynchedEntityData.defineId(BarrelWagon.class, EntityDataSerializers.INT);

    public static final WagonSeat[] SEATS = new WagonSeat[] {
            new WagonSeat(seat(0, 14, -32.25D), WagonSeat.Kind.DRIVER)
    };

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private final int[] sagKinds = new int[WHEEL_COUNT];
    private int sagKey = Integer.MIN_VALUE;
    private WheelSag sagPose = WheelSag.NONE;
    private boolean loading;
    private final ItemStackHandler cargo = new ItemStackHandler(stacks) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if(slot < BARRELS)
                return isBarrel(stack);
            return barrelFilled(barrelOf(slot));
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot < BARRELS ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot < BARRELS && !simulate && amount > 0 && isBarrel(getStackInSlot(slot)))
                dropContents(slot);
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if(slot < BARRELS)
                releaseBarrel(slot, stack);
            super.setStackInSlot(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if(slot < BARRELS)
                syncBarrels();
        }
    };

    public BarrelWagon(EntityType<BarrelWagon> type, Level level) {
        super(type, level, 19.5D / 16D, 17D / 16D, SEATS, ServerConfig.INSTANCE.barrelSpeedEmpty.get(),
                ServerConfig.INSTANCE.barrelTurnRate.get(), ServerConfig.INSTANCE.barrelHealth.get(),
                9.5D / 16D, 77.88461D / 16D, 13);
    }

    public static boolean isBarrel(ItemStack stack) {
        return stack.is(Items.BARREL);
    }

    public static int barrelOf(int slot) {
        return slot < BARRELS ? slot : (slot - BARRELS) / CONTENTS;
    }

    public static int contentIndex(int barrel, int offset) {
        return BARRELS + barrel * CONTENTS + offset;
    }

    public ItemStackHandler getCargo() {
        return cargo;
    }

    public boolean barrelFilled(int index) {
        return index >= 0 && index < BARRELS && isBarrel(stacks.get(index)) && !stacks.get(index).isEmpty();
    }

    public int barrelCount() {
        if(level() != null && level().isClientSide)
            return Integer.bitCount(entityData.get(DATA_BARRELS) & BARREL_MASK);
        int count = 0;
        for(int i = 0; i < BARRELS; i++) {
            if(barrelFilled(i))
                count++;
        }
        return count;
    }

    @Override
    protected double speedCoefficient() {
        int count = barrelCount();
        if(count <= 0)
            return ServerConfig.INSTANCE.barrelSpeedEmpty.get();
        if(count <= PARTIAL_LIMIT)
            return ServerConfig.INSTANCE.barrelSpeedPartial.get();
        return ServerConfig.INSTANCE.barrelSpeedFull.get();
    }

    /**
     * One remaining wheel, and a diagonal pair, settle onto the frame.
     * The shared diagonal correction lifts this body, and a quarter-turn on one wheel leaves it perched.
     */
    @Override
    public WheelSag wheelSag() {
        int mask = getWheelMask() & 0x0F;
        int count = Integer.bitCount(mask);
        if(count != 1 && mask != 0b0110 && mask != 0b1001)
            return super.wheelSag();
        int key = mask;
        for(int slot = 0; slot < WHEEL_COUNT; slot++) {
            int kind = getWheelKind(slot).ordinal();
            sagKinds[slot] = kind;
            key |= kind << (4 + slot * 2);
        }
        if(key != sagKey) {
            sagKey = key;
            sagPose = BarrelWagonSag.pose(mask, sagKinds);
        }
        return sagPose;
    }

    @Override
    protected double tongueLength() {
        return 57D / 16D;
    }

    @Override
    protected DyeColor getDefaultColor() {
        return null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!isAlive())
            return InteractionResult.PASS;
        if(!player.isSecondaryUseActive() && player.getVehicle() != this && isBarrel(player.getItemInHand(hand))) {
            if(!level().isClientSide)
                offerBarrel(player, hand);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.interact(player, hand);
    }

    private void offerBarrel(Player player, InteractionHand hand) {
        int slot = firstEmptyBarrel();
        if(slot < 0)
            return;
        ItemStack stack = player.getItemInHand(hand);
        ItemStack one = stack.copyWithCount(1);
        if(!cargo.insertItem(slot, one, false).isEmpty())
            return;
        if(!player.getAbilities().instabuild)
            stack.shrink(1);
    }

    private int firstEmptyBarrel() {
        for(int i = 0; i < BARRELS; i++) {
            if(stacks.get(i).isEmpty())
                return i;
        }
        return -1;
    }

    /**
     * A stack whose count is already zero reports as air, so a shift-click that emptied the slot is handled by the menu.
     */
    private void releaseBarrel(int slot, ItemStack next) {
        if(loading || level() == null || level().isClientSide)
            return;
        ItemStack previous = stacks.get(slot);
        if(previous == next || !isBarrel(previous))
            return;
        dropContents(slot);
    }

    public void dropBarrelContents(int barrel) {
        dropContents(barrel);
    }

    private void dropContents(int barrel) {
        if(level() == null || level().isClientSide)
            return;
        for(int i = 0; i < CONTENTS; i++) {
            int slot = contentIndex(barrel, i);
            ItemStack stack = stacks.get(slot);
            if(stack.isEmpty())
                continue;
            stacks.set(slot, ItemStack.EMPTY);
            spawnAtLocation(stack);
        }
    }

    private void syncBarrels() {
        if(loading || level() == null || level().isClientSide)
            return;
        int mask = 0;
        for(int i = 0; i < BARRELS; i++) {
            if(barrelFilled(i))
                mask |= 1 << i;
        }
        entityData.set(DATA_BARRELS, mask);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BARRELS, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        CompoundTag cargoTag = new CompoundTag();
        ContainerHelper.saveAllItems(cargoTag, stacks, registryAccess());
        nbt.put("Cargo", cargoTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        loading = true;
        for(int i = 0; i < stacks.size(); i++)
            stacks.set(i, ItemStack.EMPTY);
        if(nbt.contains("Cargo", Tag.TAG_COMPOUND))
            ContainerHelper.loadAllItems(nbt.getCompound("Cargo"), stacks, registryAccess());
        loading = false;
        syncBarrels();
    }

    @Override
    protected void onDestroyed(boolean dropItem) {
        if(!level().isClientSide) {
            loading = true;
            for(int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                if(stack.isEmpty())
                    continue;
                stacks.set(i, ItemStack.EMPTY);
                spawnAtLocation(stack);
            }
            ItemStackHandler repair = getRepair();
            for(int i = 0; i < REPAIR_SLOTS; i++) {
                ItemStack stack = repair.getStackInSlot(i).copy();
                if(stack.isEmpty())
                    continue;
                repair.setStackInSlot(i, ItemStack.EMPTY);
                spawnAtLocation(stack);
            }
            loading = false;
        }
        super.onDestroyed(dropItem);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BarrelMenu(id, inventory, this);
    }

    @Override
    public ItemStack getPickResult() {
        return WagonItem.setupComponents(new ItemStack(TWItems.BARREL_WAGON.get()), getColor());
    }

    private static Vec3 seat(double x, double y, double z) {
        return new Vec3(x / 16D, y / 16D, -z / 16D).add(0, 0.7D, 0);
    }

}
