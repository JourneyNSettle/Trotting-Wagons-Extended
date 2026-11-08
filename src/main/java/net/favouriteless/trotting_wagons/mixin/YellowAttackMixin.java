package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.client.YellowRay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A tall cover sits above the shortened pick box. Clicks on its yellow face still count. */
@Mixin(Minecraft.class)
public class YellowAttackMixin {

    @Shadow
    protected int missTime;

    @Shadow
    private int rightClickDelay;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$attackYellow(CallbackInfoReturnable<Boolean> callback) {
        Minecraft minecraft = (Minecraft)(Object)this;
        if(minecraft.player == null || minecraft.level == null || minecraft.gameMode == null)
            return;
        if(missTime > 0 || minecraft.hitResult == null || minecraft.player.isHandsBusy())
            return;
        ItemStack stack = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        if(!stack.isItemEnabled(minecraft.level.enabledFeatures()))
            return;
        YellowRay.Strike strike = YellowRay.nearestWagon(minecraft.player);
        if(strike == null || !YellowRay.isFirst(minecraft.player, minecraft.hitResult, strike))
            return;

        var input = ClientHooks.onClickInput(0, minecraft.options.keyAttack, InteractionHand.MAIN_HAND);
        if(!input.isCanceled())
            minecraft.gameMode.attack(minecraft.player, strike.entity());
        if(input.shouldSwingHand())
            minecraft.player.swing(InteractionHand.MAIN_HAND);
        callback.setReturnValue(false);
    }

    /** Holding the button must not mine the block behind that face. */
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$holdYellow(boolean leftClick, CallbackInfo callback) {
        if(!leftClick)
            return;
        Minecraft minecraft = (Minecraft)(Object)this;
        if(minecraft.player == null || minecraft.gameMode == null || minecraft.hitResult == null)
            return;
        if(minecraft.hitResult.getType() != HitResult.Type.BLOCK)
            return;
        YellowRay.Strike strike = YellowRay.nearestWagon(minecraft.player);
        if(strike == null || !YellowRay.isFirst(minecraft.player, minecraft.hitResult, strike))
            return;
        minecraft.gameMode.stopDestroyBlock();
        callback.cancel();
    }

    /** Right click on that face boards or fits a wheel or harness, instead of using the block behind it. */
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void trottingWagons$useYellow(CallbackInfo callback) {
        Minecraft minecraft = (Minecraft)(Object)this;
        if(minecraft.player == null || minecraft.level == null || minecraft.gameMode == null || minecraft.hitResult == null)
            return;
        if(minecraft.gameMode.isDestroying() || minecraft.player.isHandsBusy())
            return;
        YellowRay.Strike strike = YellowRay.nearestWagon(minecraft.player);
        if(strike == null || !YellowRay.isFirst(minecraft.player, minecraft.hitResult, strike))
            return;
        if(!minecraft.level.getWorldBorder().isWithinBounds(strike.entity().blockPosition()))
            return;

        rightClickDelay = 4;
        for(InteractionHand hand : InteractionHand.values()) {
            var input = ClientHooks.onClickInput(1, minecraft.options.keyUse, hand);
            if(input.isCanceled()) {
                if(input.shouldSwingHand())
                    minecraft.player.swing(hand);
                callback.cancel();
                return;
            }
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if(!stack.isItemEnabled(minecraft.level.enabledFeatures())) {
                callback.cancel();
                return;
            }
            InteractionResult result = minecraft.gameMode.interact(minecraft.player, strike.entity(), hand);
            if(result.consumesAction()) {
                if(result.shouldSwing() && input.shouldSwingHand())
                    minecraft.player.swing(hand);
                callback.cancel();
                return;
            }
        }
        callback.cancel();
    }

}
