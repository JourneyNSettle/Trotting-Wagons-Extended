package net.favouriteless.trotting_wagons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.BaseWagon;
import net.favouriteless.trotting_wagons.common.entities.Chassis;
import net.favouriteless.trotting_wagons.common.entities.WagonBuild;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.items.HorseWhipItem;
import net.favouriteless.trotting_wagons.common.items.WheelItem;
import net.favouriteless.trotting_wagons.common.network.packets.HitchAnimalPayload;
import net.favouriteless.trotting_wagons.common.network.packets.OpenWagonMenuPayload;
import net.favouriteless.trotting_wagons.common.network.packets.WhipLowerGearPayload;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
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

    /** Small Ships scrolls the helm camera from 1 to 20. A wagon stops at half of that. */
    private static final double DRIVER_ZOOM_MIN = 1.0D;
    private static final double DRIVER_ZOOM_MAX = 10.0D;
    private static double driverZoom = 6.0D;
    private static boolean wasDriver;

    @SubscribeEvent
    public static void onRenderHarnessed(RenderLivingEvent.Pre<?, ?> event) {
        if(!showHarnessed(event.getEntity()) || !(event.getEntity() instanceof Camel camel))
            return;
        camel.idleAnimationState.stop();
        camel.sitAnimationState.stop();
        camel.sitPoseAnimationState.stop();
        camel.sitUpAnimationState.stop();
        camel.dashAnimationState.stop();
    }

    private static boolean showHarnessed(LivingEntity entity) {
        return entity instanceof Mob mob && (mob instanceof AbstractHorse || mob instanceof Camel)
                && mob.isNoAi() && AbstractWagon.isHarnessed(mob);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == null)
            return;

        boolean driver = driving(minecraft);
        if(driver && !wasDriver)
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        else if(!driver && wasDriver)
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        wasDriver = driver;

        if(minecraft.screen != null)
            return;

        while(HITCH_KEY.consumeClick()) {
            if(minecraft.player.getVehicle() instanceof Mob)
                PacketDistributor.sendToServer(new HitchAnimalPayload());
        }
    }

    /** Third-person distance while the local player is the driver. Walls still cut it short. */
    @SubscribeEvent
    public static void onDriverCamera(CalculateDetachedCameraDistanceEvent event) {
        if(driving(Minecraft.getInstance()))
            event.setDistance((float)driverZoom);
    }

    /** Scroll up moves closer. The closest step is first person. Scroll down backs out, and the hotbar stays put. */
    @SubscribeEvent
    public static void onDriverScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if(!driving(minecraft))
            return;
        double delta = event.getScrollDeltaY();
        if(delta == 0.0D)
            return;
        if(minecraft.options.getCameraType().isFirstPerson()) {
            if(delta < 0.0D)
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            event.setCanceled(true);
            return;
        }
        double next = driverZoom - delta;
        if(next < DRIVER_ZOOM_MIN) {
            driverZoom = DRIVER_ZOOM_MIN;
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        } else
            driverZoom = Math.min(DRIVER_ZOOM_MAX, next);
        event.setCanceled(true);
    }

    private static boolean driving(Minecraft minecraft) {
        return minecraft.player != null
                && minecraft.player.getVehicle() instanceof AbstractWagon wagon
                && wagon.getControllingPassenger() == minecraft.player;
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
        if(minecraft.player != null && minecraft.screen == null && minecraft.crosshairPickEntity instanceof Chassis chassis)
            drawBuildHint(event.getGuiGraphics(), minecraft, chassis);
        if(minecraft.player != null && minecraft.screen == null && minecraft.crosshairPickEntity instanceof BaseWagon base && base.showsPlan())
            drawBuildPlan(event.getGuiGraphics(), minecraft, base);
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

    private static void drawBuildHint(GuiGraphics graphics, Minecraft minecraft, Chassis chassis) {
        WagonBuild.Need[] bill = WagonBuild.chassisBill();
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 16;
        for(int i = 0; i < bill.length; i++) {
            Component line = Component.translatable("gui.trotting_wagons.build." + bill[i].name(), chassis.syncedCount(i), bill[i].required());
            graphics.drawString(minecraft.font, line, x - minecraft.font.width(line) / 2, y + i * 10, 0xFFFFFF, true);
        }
    }

    private static void drawBuildPlan(GuiGraphics graphics, Minecraft minecraft, BaseWagon wagon) {
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 16;
        for(int i = 0; i < wagon.planSize(); i++) {
            Component line = Component.translatable("gui.trotting_wagons.build." + wagon.planName(i), wagon.planCount(i), wagon.planRequired(i));
            graphics.drawString(minecraft.font, line, x - minecraft.font.width(line) / 2, y + i * 10, 0xFFFFFF, true);
        }
    }

    /** Advanced tooltips add item.durability beside the wheel's own line. */
    @SubscribeEvent
    public static void onWheelTooltip(ItemTooltipEvent event) {
        if(!(event.getItemStack().getItem() instanceof WheelItem))
            return;
        event.getToolTip().removeIf(line -> line.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("item.durability"));
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
