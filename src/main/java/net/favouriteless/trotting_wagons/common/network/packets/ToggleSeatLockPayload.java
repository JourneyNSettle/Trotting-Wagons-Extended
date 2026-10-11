package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleSeatLockPayload(int wagonId, int seat) implements CustomPacketPayload {

    public static final Type<ToggleSeatLockPayload> TYPE = new Type<>(TrottingWagons.id("toggle_seat_lock"));
    public static final StreamCodec<ByteBuf, ToggleSeatLockPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ToggleSeatLockPayload::wagonId,
            ByteBufCodecs.VAR_INT, ToggleSeatLockPayload::seat,
            ToggleSeatLockPayload::new
    );

    public static void handle(ToggleSeatLockPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon && player.getVehicle() == wagon)
                wagon.toggleSeatLock(player, payload.seat());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
