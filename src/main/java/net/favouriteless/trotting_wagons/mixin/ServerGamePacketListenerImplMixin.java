package net.favouriteless.trotting_wagons.mixin;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    private boolean clientIsFloating;

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void trottingWagons$ladderNotFlying(ServerboundMovePlayerPacket packet, CallbackInfo callback) {
        if(this.clientIsFloating && AbstractWagon.climbsWagon(this.player))
            this.clientIsFloating = false;
    }

}
