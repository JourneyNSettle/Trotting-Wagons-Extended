package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KibitkaSleepPayload(int wagonId) implements CustomPacketPayload {

    public static final Type<KibitkaSleepPayload> TYPE = new Type<>(TrottingWagons.id("kibitka_sleep"));
    public static final StreamCodec<ByteBuf, KibitkaSleepPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, KibitkaSleepPayload::wagonId,
            KibitkaSleepPayload::new
    );

    public static void handle(KibitkaSleepPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof KibitkaWagon wagon)
                wagon.trySleep(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
