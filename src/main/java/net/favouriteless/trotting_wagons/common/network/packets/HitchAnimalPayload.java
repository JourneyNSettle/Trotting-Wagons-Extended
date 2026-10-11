package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HitchAnimalPayload() implements CustomPacketPayload {

    public static final Type<HitchAnimalPayload> TYPE = new Type<>(TrottingWagons.id("hitch_animal"));
    public static final StreamCodec<ByteBuf, HitchAnimalPayload> CODEC = StreamCodec.unit(new HitchAnimalPayload());

    public static void handle(HitchAnimalPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(!(ctx.player().getVehicle() instanceof Mob animal))
                return;
            AbstractWagon wagon = AbstractWagon.findHitchTarget(animal);
            if(wagon != null && wagon.hitchRiddenAnimal(animal) && ctx.player() instanceof ServerPlayer player)
                wagon.awardSinner(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
