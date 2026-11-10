package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClimbMixin {

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$wagonLadder(CallbackInfoReturnable<Boolean> callback) {
        if(!((Object)this instanceof Player player))
            return;
        if(player.isSpectator() || player.isPassenger() || player.getAbilities().flying)
            return;
        if(AbstractWagon.climbsWagon(player))
            callback.setReturnValue(true);
    }

    /** A seated player does not walk. That step hits the seat and the view bobs. */
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$holdSeat(Vec3 travelVector, CallbackInfo callback) {
        if(!((Object)this instanceof Player player))
            return;
        if(!(player.getVehicle() instanceof AbstractWagon))
            return;
        player.setDeltaMovement(Vec3.ZERO);
        callback.cancel();
    }

}
