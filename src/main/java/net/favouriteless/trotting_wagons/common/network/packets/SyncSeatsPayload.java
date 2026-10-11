package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSeatsPayload(int wagonId, int[] seatIds, boolean[] locked) implements CustomPacketPayload {

    public static final Type<SyncSeatsPayload> TYPE = new Type<>(TrottingWagons.id("sync_seats"));
    public static final StreamCodec<ByteBuf, SyncSeatsPayload> CODEC = StreamCodec.of(SyncSeatsPayload::write, SyncSeatsPayload::read);

    private static void write(ByteBuf buf, SyncSeatsPayload payload) {
        ByteBufCodecs.INT.encode(buf, payload.wagonId());
        ByteBufCodecs.VAR_INT.encode(buf, payload.seatIds().length);
        for(int id : payload.seatIds())
            ByteBufCodecs.INT.encode(buf, id);
        ByteBufCodecs.VAR_INT.encode(buf, payload.locked().length);
        for(boolean locked : payload.locked())
            ByteBufCodecs.BOOL.encode(buf, locked);
    }

    private static SyncSeatsPayload read(ByteBuf buf) {
        int wagonId = ByteBufCodecs.INT.decode(buf);
        int count = ByteBufCodecs.VAR_INT.decode(buf);
        int[] seatIds = new int[count];
        for(int i = 0; i < count; i++)
            seatIds[i] = ByteBufCodecs.INT.decode(buf);
        int lockCount = ByteBufCodecs.VAR_INT.decode(buf);
        boolean[] locked = new boolean[lockCount];
        for(int i = 0; i < lockCount; i++)
            locked[i] = ByteBufCodecs.BOOL.decode(buf);
        return new SyncSeatsPayload(wagonId, seatIds, locked);
    }

    public static void handle(SyncSeatsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(ctx.player().level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon)
                wagon.applySeatState(payload.seatIds(), payload.locked());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
