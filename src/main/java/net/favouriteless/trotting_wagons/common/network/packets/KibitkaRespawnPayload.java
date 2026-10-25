package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KibitkaRespawnPayload(int wagonId) implements CustomPacketPayload {

    public static final Type<KibitkaRespawnPayload> TYPE = new Type<>(TrottingWagons.id("kibitka_respawn"));
    public static final StreamCodec<ByteBuf, KibitkaRespawnPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, KibitkaRespawnPayload::wagonId,
            KibitkaRespawnPayload::new
    );

    public static void handle(KibitkaRespawnPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof KibitkaWagon wagon)
                wagon.trySetRespawn(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
