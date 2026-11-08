package net.favouriteless.trotting_wagons.mixin;

import com.kltyton.bonehitboxlib.client.geometry.bounds.PartBounds;
import com.kltyton.bonehitboxlib.client.selection.model.VisualPartId;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BonePartSelectionLogic.class)
public class HeldItemColliderMixin {

    /** A held item's yellow bones stay out of the world. The player's own box is unchanged. */
    @Inject(method = "record(Lcom/kltyton/bonehitboxlib/client/selection/model/VisualPartId;Lcom/kltyton/bonehitboxlib/client/geometry/bounds/PartBounds;)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void trottingWagons$ignoreHeldItem(VisualPartId id, PartBounds bounds, CallbackInfoReturnable<Boolean> callback) {
        if("held_item".equals(id.source()))
            callback.setReturnValue(false);
    }

}
