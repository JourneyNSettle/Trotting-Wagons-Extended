package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBedMixin {

    @Inject(method = "getBedOrientation", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$kibitkaBed(CallbackInfoReturnable<Direction> callback) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if(!entity.isSleeping() || !(entity.getVehicle() instanceof KibitkaWagon wagon) || !wagon.isCamped())
            return;
        callback.setReturnValue(Direction.fromYRot(wagon.getYRot()));
    }

}
