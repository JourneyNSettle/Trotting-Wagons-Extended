package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

}
