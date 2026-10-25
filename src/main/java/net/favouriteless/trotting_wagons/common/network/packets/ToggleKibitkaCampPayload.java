package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleKibitkaCampPayload(int wagonId) implements CustomPacketPayload {

    public static final Type<ToggleKibitkaCampPayload> TYPE = new Type<>(TrottingWagons.id("toggle_kibitka_camp"));
    public static final StreamCodec<ByteBuf, ToggleKibitkaCampPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ToggleKibitkaCampPayload::wagonId,
            ToggleKibitkaCampPayload::new
    );

    public static void handle(ToggleKibitkaCampPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof KibitkaWagon wagon)
                wagon.requestCampToggle(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
