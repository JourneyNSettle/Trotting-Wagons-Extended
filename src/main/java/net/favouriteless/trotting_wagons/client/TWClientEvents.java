package net.favouriteless.trotting_wagons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.network.packets.HitchAnimalPayload;
import net.favouriteless.trotting_wagons.common.network.packets.OpenWagonMenuPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
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
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == null || !(minecraft.player.getVehicle() instanceof AbstractWagon wagon))
            return;
        if(wagon.getControllingPassenger() != minecraft.player)
            return;
        GuiGraphics graphics = event.getGuiGraphics();
        int gears = AbstractWagon.SPEED_LEVELS;
        int width = gears * 20 - 4;
        int x = graphics.guiWidth() / 2 - width / 2;
        int y = graphics.guiHeight() - 48;
        int level = wagon.getSpeedLevel();
        for(int i = 0; i < gears; i++) {
            int left = x + i * 20;
            graphics.fill(left, y, left + 16, y + 6, i <= level ? 0xFFE0E0E0 : 0xFF3A3A3A);
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
