package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.client.YellowRay;
import net.favouriteless.trotting_wagons.common.entities.BaseWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonParts;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class WagonAttackMixin {

    /** A wagon is struck only on a yellow face or a wheel. The harness frame and empty air do not. */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$yellowOrWheel(Player player, Entity target, CallbackInfo callback) {
        if(!allows(player, target))
            callback.cancel();
    }

    private static boolean allows(Player player, Entity target) {
        double reach = player.entityInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        Vec3 yellow = YellowRay.closest(target, eye, end);
        double yellowDistance = yellow == null ? Double.MAX_VALUE : yellow.distanceToSqr(eye);
        if(target instanceof AbstractWagon wagon) {
            WagonParts.Part part = WagonParts.pick(wagon, player);
            Vec3 point = WagonParts.closestPoint(wagon, player);
            double partDistance = point == null ? Double.MAX_VALUE : point.distanceToSqr(eye);
            if(part != null && partDistance <= yellowDistance)
                return part.kind() == WagonParts.Kind.WHEEL;
        } else if(!(target instanceof BaseWagon))
            return true;
        return yellow != null && yellowDistance <= reach * reach;
    }

}
