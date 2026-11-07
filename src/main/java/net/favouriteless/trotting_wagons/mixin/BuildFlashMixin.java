package net.favouriteless.trotting_wagons.mixin;

import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionLogic;
import net.favouriteless.trotting_wagons.common.entities.BaseWagon;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractGeckolibVehicle;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BonePartSelectionLogic.class)
public class BuildFlashMixin {

    /** BoneHitboxLib paints the struck bone red. Wagons keep collision and skip that flash. */
    @Inject(method = "recordAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void trottingWagons$noBoneFlash(Entity target, CallbackInfo callback) {
        if(target instanceof AbstractGeckolibVehicle || target instanceof BaseWagon)
            callback.cancel();
    }

}
