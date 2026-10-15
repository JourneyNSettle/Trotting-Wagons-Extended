package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleWagonLockPayload(int wagonId) implements CustomPacketPayload {

    public static final Type<ToggleWagonLockPayload> TYPE = new Type<>(TrottingWagons.id("toggle_wagon_lock"));
    public static final StreamCodec<ByteBuf, ToggleWagonLockPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ToggleWagonLockPayload::wagonId,
            ToggleWagonLockPayload::new
    );

    public static void handle(ToggleWagonLockPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon)
                wagon.toggleLock(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
