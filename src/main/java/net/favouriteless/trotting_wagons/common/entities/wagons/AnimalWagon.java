package net.favouriteless.trotting_wagons.common.entities.wagons;

import net.favouriteless.trotting_wagons.ServerConfig;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonSeat;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.items.WagonItem;
import net.favouriteless.trotting_wagons.common.menus.AnimalMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AnimalWagon extends AbstractWagon implements MenuProvider {

    private static final int FRONT_LARGE = 3;
    private static final int REAR_LARGE = 6;
    private static final int[] FRONT_GROUP = {1, 2, 4, 5};
    private static final int[] REAR_GROUP = {4, 5, 7, 8};

    public static final WagonSeat[] SEATS = new WagonSeat[] {
            new WagonSeat(seat(0, 14, -32.25D), WagonSeat.Kind.DRIVER),
            new WagonSeat(seat(-6, 16.5D, -14), WagonSeat.Kind.ANIMAL),
            new WagonSeat(seat(6, 16.5D, -14), WagonSeat.Kind.ANIMAL),
            new WagonSeat(seat(0, 16.5D, -4), WagonSeat.Kind.ANIMALBIG),
            new WagonSeat(seat(-6, 16.5D, 10), WagonSeat.Kind.ANIMAL),
            new WagonSeat(seat(6, 16.5D, 10), WagonSeat.Kind.ANIMAL),
            new WagonSeat(seat(0, 16.5D, 24), WagonSeat.Kind.ANIMALBIG),
            new WagonSeat(seat(-6, 16.5D, 34), WagonSeat.Kind.ANIMAL),
            new WagonSeat(seat(6, 16.5D, 34), WagonSeat.Kind.ANIMAL)
    };

    public AnimalWagon(EntityType<AnimalWagon> type, Level level) {
        super(type, level, 1.5D, 1.5D, SEATS, ServerConfig.INSTANCE.animalSpeed.get(),
                ServerConfig.INSTANCE.animalTurnRate.get(), ServerConfig.INSTANCE.animalHealth.get(),
                9.5D / 16D, 77.88461D / 16D, 13);
    }

    @Override
    protected double tongueLength() {
        return 55D / 16D;
    }

    @Override
    protected float missingRearPitch() {
        return -8F;
    }

    @Override
    protected float diagonalPitch() {
        return -8F;
    }

    @Override
    protected boolean acceptsLargeSeat(Entity entity) {
        return isLargePassenger(entity);
    }

    @Override
    public Component seatBlockHint(int index) {
        if(index < 0 || index >= getSeatCount() || getSeatEntityId(index) >= 0)
            return null;
        if((index == FRONT_LARGE && anyTaken(null, FRONT_GROUP)) || (index == REAR_LARGE && anyTaken(null, REAR_GROUP)))
            return Component.translatable("gui.trotting_wagons.tooltip.large_needs_space");
        if((contains(FRONT_GROUP, index) && taken(FRONT_LARGE, null)) || (contains(REAR_GROUP, index) && taken(REAR_LARGE, null)))
            return Component.translatable("gui.trotting_wagons.tooltip.small_blocked");
        return null;
    }

    @Override
    protected boolean seatOpen(int index, Entity entity) {
        if(index == FRONT_LARGE)
            return !anyTaken(entity, FRONT_GROUP);
        if(index == REAR_LARGE)
            return !anyTaken(entity, REAR_GROUP);
        if(contains(FRONT_GROUP, index) && taken(FRONT_LARGE, entity))
            return false;
        return !contains(REAR_GROUP, index) || !taken(REAR_LARGE, entity);
    }

    private boolean anyTaken(Entity entity, int[] seats) {
        for(int seat : seats) {
            if(taken(seat, entity))
                return true;
        }
        return false;
    }

    private boolean taken(int seat, Entity entity) {
        int id = getSeatEntityId(seat);
        return id >= 0 && (entity == null || id != entity.getId());
    }

    private static boolean contains(int[] seats, int index) {
        for(int seat : seats) {
            if(seat == index)
                return true;
        }
        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AnimalMenu(id, inventory, this);
    }

    @Override
    public ItemStack getPickResult() {
        return WagonItem.setupComponents(new ItemStack(TWItems.ANIMAL_WAGON.get()), getColor());
    }

    @Override
    protected DyeColor getDefaultColor() {
        return null;
    }

    private static Vec3 seat(double x, double y, double z) {
        return new Vec3(x / 16D, y / 16D, -z / 16D).add(0, 0.7D, 0);
    }

}
