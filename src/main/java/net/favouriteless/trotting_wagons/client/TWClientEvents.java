package net.favouriteless.trotting_wagons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.items.HorseWhipItem;
import net.favouriteless.trotting_wagons.common.network.packets.HitchAnimalPayload;
import net.favouriteless.trotting_wagons.common.network.packets.OpenWagonMenuPayload;
import net.favouriteless.trotting_wagons.common.network.packets.WhipLowerGearPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
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
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(!event.isAttack() || minecraft.player == null)
            return;
        if(!(minecraft.player.getMainHandItem().getItem() instanceof HorseWhipItem))
            return;
        if(!(minecraft.player.getVehicle() instanceof AbstractWagon wagon) || wagon.getControllingPassenger() != minecraft.player)
            return;
        event.setCanceled(true);
        event.setSwingHand(true);
        if(minecraft.player.getAttackStrengthScale(0.0F) < 0.9F)
            return;
        minecraft.player.resetAttackStrengthTicker();
        PacketDistributor.sendToServer(new WhipLowerGearPayload());
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == null || !(minecraft.player.getVehicle() instanceof AbstractWagon wagon))
            return;
        if(wagon.getControllingPassenger() != minecraft.player)
            return;
        GuiGraphics graphics = event.getGuiGraphics();
        int width = 182;
        int filled = (int)(wagon.speedFraction() * 183.0F);
        int x = (graphics.guiWidth() - width) / 2;
        int y = graphics.guiHeight() - 32 + 3;
        RenderSystem.enableBlend();
        graphics.blitSprite(ResourceLocation.withDefaultNamespace("hud/jump_bar_background"), x, y, width, 5);
        if(filled > 0)
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("hud/jump_bar_progress"), width, 5, 0, 0, x, y, filled, 5);
        RenderSystem.disableBlend();
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
