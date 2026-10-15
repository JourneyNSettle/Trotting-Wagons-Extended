package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.init.TWSoundEvents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WhipLowerGearPayload() implements CustomPacketPayload {

    public static final Type<WhipLowerGearPayload> TYPE = new Type<>(TrottingWagons.id("whip_lower_gear"));
    public static final StreamCodec<ByteBuf, WhipLowerGearPayload> CODEC = StreamCodec.unit(new WhipLowerGearPayload());

    public static void handle(WhipLowerGearPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.getVehicle() instanceof AbstractWagon wagon && wagon.tryLowerSpeed(player))
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TWSoundEvents.WHIP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
