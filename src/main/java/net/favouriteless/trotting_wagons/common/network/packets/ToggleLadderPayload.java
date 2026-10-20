package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleLadderPayload(int wagonId) implements CustomPacketPayload {

    public static final Type<ToggleLadderPayload> TYPE = new Type<>(TrottingWagons.id("toggle_ladder"));
    public static final StreamCodec<ByteBuf, ToggleLadderPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ToggleLadderPayload::wagonId,
            ToggleLadderPayload::new
    );

    public static void handle(ToggleLadderPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof DebugWagon wagon)
                wagon.toggleLadder(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
