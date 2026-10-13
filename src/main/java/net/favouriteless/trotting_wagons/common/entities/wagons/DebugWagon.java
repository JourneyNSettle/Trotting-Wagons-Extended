package net.favouriteless.trotting_wagons.common.entities.wagons;

import net.favouriteless.trotting_wagons.ServerConfig;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonSeat;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.items.WagonItem;
import net.favouriteless.trotting_wagons.common.menus.DebugMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

public class DebugWagon extends AbstractInventoryWagon {

    public static final int PUMPKIN_SLOTS = 4;

    private static final EntityDataAccessor<Integer> DATA_PUMPKINS = SynchedEntityData.defineId(DebugWagon.class, EntityDataSerializers.INT);

    public static final WagonSeat[] SEATS = new WagonSeat[] {
            new WagonSeat(pivot(0, 27.5D, -28.5D), WagonSeat.Kind.DRIVER),
            new WagonSeat(pivot(-14.5D, 26, -10.25D), WagonSeat.Kind.PLAYER),
            new WagonSeat(pivot(-11, 15.65124D, 9.34415D).add(0, 0.7D, 0), WagonSeat.Kind.ANIMAL),
            new WagonSeat(pivot(8, 16.65124D, -13.65585D).add(0, 0.7D, 0), WagonSeat.Kind.UNIVERSAL)
    };

    public DebugWagon(EntityType<DebugWagon> type, Level level) {
        super(type, level, 1.5D, 1.5D, SEATS, ServerConfig.INSTANCE.debugSpeed.get(), ServerConfig.INSTANCE.debugAcceleration.get(),
                ServerConfig.INSTANCE.debugTurnRate.get(), ServerConfig.INSTANCE.debugHealth.get(), 0.3125D, 4.805D, 13, PUMPKIN_SLOTS);
    }

    @Override
    public int hitchSlots() {
        return 1;
    }

    @Override
    protected Vec3 hitchOffset(Side side) {
        return new Vec3(1D / 16D, 0, 76.88461D / 16D);
    }

    private static Vec3 pivot(double x, double y, double z) {
        return new Vec3(x / 16D, y / 16D, -z / 16D);
    }

    @Override
    protected double tongueLength() {
        return 59.12D / 16D;
    }

    @Override
    protected double tongueHeight() {
        return 8.5D / 16D;
    }

    @Override
    protected ItemStackHandler createHandler(NonNullList<ItemStack> inventory) {
        return new ItemStackHandler(inventory) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.is(Items.PUMPKIN);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                syncPumpkins();
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PUMPKINS, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        syncPumpkins();
    }

    public int getPumpkinCount() {
        return entityData.get(DATA_PUMPKINS);
    }

    private void syncPumpkins() {
        int count = 0;
        for(int i = 0; i < getItems().getSlots(); i++) {
            if(!getItems().getStackInSlot(i).isEmpty())
                count++;
        }
        entityData.set(DATA_PUMPKINS, count);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DebugMenu(id, inventory, this);
    }

    @Override
    public ItemStack getPickResult() {
        return WagonItem.setupComponents(new ItemStack(TWItems.DEBUG_WAGON.get()), getColor());
    }

}
