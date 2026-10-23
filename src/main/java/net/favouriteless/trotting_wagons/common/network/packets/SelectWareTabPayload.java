package net.favouriteless.trotting_wagons.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.menus.WareMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectWareTabPayload(int tab) implements CustomPacketPayload {

    public static final Type<SelectWareTabPayload> TYPE = new Type<>(TrottingWagons.id("select_ware_tab"));
    public static final StreamCodec<ByteBuf, SelectWareTabPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SelectWareTabPayload::tab,
            SelectWareTabPayload::new
    );

    public static void handle(SelectWareTabPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if(player.containerMenu instanceof WareMenu menu)
                menu.setTab(payload.tab());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
