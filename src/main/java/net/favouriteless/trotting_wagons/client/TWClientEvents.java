package net.favouriteless.trotting_wagons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.network.packets.HitchAnimalPayload;
import net.favouriteless.trotting_wagons.common.network.packets.OpenWagonMenuPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TrottingWagons.MOD_ID, value = Dist.CLIENT)
public class TWClientEvents {

    public static final KeyMapping HITCH_KEY = new KeyMapping(
            "key.trotting_wagons.hitch",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.trotting_wagons"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == null || minecraft.screen != null)
            return;

        while(HITCH_KEY.consumeClick()) {
            if(minecraft.player.getVehicle() instanceof Mob)
                PacketDistributor.sendToServer(new HitchAnimalPayload());
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(!(event.getScreen() instanceof InventoryScreen) || minecraft.player == null)
            return;
        if(minecraft.player.getVehicle() instanceof AbstractWagon wagon && wagon.hasFixedSeats()) {
            event.setCanceled(true);
            PacketDistributor.sendToServer(new OpenWagonMenuPayload());
        }
    }

}
