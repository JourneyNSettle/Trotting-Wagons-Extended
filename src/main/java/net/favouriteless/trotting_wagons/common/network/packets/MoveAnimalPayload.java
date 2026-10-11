package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MoveAnimalPayload(int wagonId, int animalId, int toSeat) implements CustomPacketPayload {

    public static final Type<MoveAnimalPayload> TYPE = new Type<>(TrottingWagons.id("move_animal"));
    public static final StreamCodec<ByteBuf, MoveAnimalPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MoveAnimalPayload::wagonId,
            ByteBufCodecs.INT, MoveAnimalPayload::animalId,
            ByteBufCodecs.VAR_INT, MoveAnimalPayload::toSeat,
            MoveAnimalPayload::new
    );

    public static void handle(MoveAnimalPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.level().getEntity(payload.wagonId()) instanceof AbstractWagon wagon && player.getVehicle() == wagon)
                wagon.moveAnimal(player, payload.animalId(), payload.toSeat());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
