package net.favouriteless.trotting_wagons.common.entities.wagons;

import net.favouriteless.trotting_wagons.ServerConfig;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractInventoryWagon;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.favouriteless.trotting_wagons.common.items.WagonItem;
import net.favouriteless.trotting_wagons.common.menus.RoyalMenu;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.favouriteless.trotting_wagons.common.entities.base.WagonSeat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RoyalWagon extends AbstractInventoryWagon {

    public static final WagonSeat[] SEATS = new WagonSeat[] {
            new WagonSeat(new Vec3(0, 1.3D, 1.8D), WagonSeat.Kind.DRIVER),
            new WagonSeat(new Vec3(0, 1.45D, -0.7D), WagonSeat.Kind.PLAYER),
            new WagonSeat(new Vec3(0.4375D, 1.45D, 0.7D), WagonSeat.Kind.PLAYER),
            new WagonSeat(new Vec3(-0.4375D, 1.45D, 0.7D), WagonSeat.Kind.PLAYER)
    };

    public RoyalWagon(EntityType<RoyalWagon> type, Level level) {
        super(type, level, 1.1D, 1.3D, SEATS, ServerConfig.INSTANCE.royalSpeed.get(),
                ServerConfig.INSTANCE.royalAcceleration.get(), ServerConfig.INSTANCE.conestogaTurnRate.get(),
                ServerConfig.INSTANCE.royalHealth.get(), 0.6D, 4.3D, 7, 54);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RoyalMenu(id, inventory, this);
    }

    @Override
    public ItemStack getPickResult() {
        return WagonItem.setupComponents(new ItemStack(TWItems.ROYAL_WAGON.get()), this.getColor());
    }

}
