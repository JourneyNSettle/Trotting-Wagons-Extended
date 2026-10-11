package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectSeatPayload(int wagonId, int seat) implements CustomPacketPayload {

    public static final Type<SelectSeatPayload> TYPE = new Type<>(TrottingWagons.id("select_seat"));
    public static final StreamCodec<ByteBuf, SelectSeatPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SelectSeatPayload::wagonId,
            ByteBufCodecs.VAR_INT, SelectSeatPayload::seat,
            SelectSeatPayload::new
    );

    public static void handle(SelectSeatPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon && player.getVehicle() == wagon)
                wagon.movePlayerToSeat(player, payload.seat());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
