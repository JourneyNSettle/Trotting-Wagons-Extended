package net.favouriteless.trotting_wagons.common;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.items.HorseWhipItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TrottingWagons.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if(event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof AbstractWagon wagon)
            wagon.sendSeatSync(player);
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if(!event.isMounting() || !(event.getEntityBeingMounted() instanceof Mob mob))
            return;
        if(mob.getVehicle() instanceof AbstractWagon) {
            event.setCanceled(true);
            return;
        }
        if(event.getLevel().isClientSide)
            return;
        AbstractWagon wagon = AbstractWagon.findHitch(mob);
        if(wagon != null)
            wagon.unhitch(mob);
    }

    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        Entity target = event.getTarget();

        InteractionHand whipHand = holdingWhip(player);
        if(whipHand != null && player.getVehicle() instanceof AbstractWagon ridden && player == ridden.getControllingPassenger()
                && target instanceof Mob mob && AbstractWagon.findHitch(mob) == ridden) {
            ItemStack stack = player.getItemInHand(whipHand);
            InteractionResultHolder<ItemStack> used = ((HorseWhipItem)stack.getItem()).use(level, player, whipHand);
            event.setCanceled(true);
            event.setCancellationResult(used.getResult().consumesAction() ? used.getResult() : InteractionResult.sidedSuccess(level.isClientSide));
            return;
        }

        if(!player.isCrouching())
            return;

        if(target.getVehicle() instanceof AbstractWagon) {
            if(!level.isClientSide)
                target.stopRiding();
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
        else if(target instanceof Mob mob) {
            AbstractWagon wagon = AbstractWagon.findHitch(mob);
            if(wagon == null)
                return;
            if(!level.isClientSide)
                wagon.unhitch(mob);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if(drivingWithWhip(event.getEntity()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if(drivingWithWhip(event.getEntity()))
            event.setCanceled(true);
    }

    private static boolean drivingWithWhip(Player player) {
        if(!(player.getMainHandItem().getItem() instanceof HorseWhipItem))
            return false;
        return player.getVehicle() instanceof AbstractWagon wagon && player == wagon.getControllingPassenger();
    }

    private static InteractionHand holdingWhip(Player player) {
        if(player.getMainHandItem().getItem() instanceof HorseWhipItem)
            return InteractionHand.MAIN_HAND;
        if(player.getOffhandItem().getItem() instanceof HorseWhipItem)
            return InteractionHand.OFF_HAND;
        return null;
    }

}
