package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenWagonMenuPayload() implements CustomPacketPayload {

    public static final Type<OpenWagonMenuPayload> TYPE = new Type<>(TrottingWagons.id("open_wagon_menu"));
    public static final StreamCodec<ByteBuf, OpenWagonMenuPayload> CODEC = StreamCodec.unit(new OpenWagonMenuPayload());

    public static void handle(OpenWagonMenuPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.getVehicle() instanceof AbstractWagon wagon && wagon.hasFixedSeats() && wagon instanceof net.minecraft.world.MenuProvider provider)
                player.openMenu(provider, buf -> buf.writeInt(wagon.getId()));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
