package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EjectSeatPayload(int wagonId, int seat, boolean draft) implements CustomPacketPayload {

    public static final Type<EjectSeatPayload> TYPE = new Type<>(TrottingWagons.id("eject_seat"));
    public static final StreamCodec<ByteBuf, EjectSeatPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EjectSeatPayload::wagonId,
            ByteBufCodecs.VAR_INT, EjectSeatPayload::seat,
            ByteBufCodecs.BOOL, EjectSeatPayload::draft,
            EjectSeatPayload::new
    );

    public static void handle(EjectSeatPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(!(player.level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon) || player.getVehicle() != wagon)
                return;
            if(payload.draft())
                wagon.ejectDraft(player, payload.seat() == 0 ? AbstractWagon.Side.LEFT : AbstractWagon.Side.RIGHT);
            else
                wagon.ejectSeat(player, payload.seat());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
