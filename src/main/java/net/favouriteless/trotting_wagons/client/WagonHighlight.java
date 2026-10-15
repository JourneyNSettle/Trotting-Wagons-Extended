package net.favouriteless.trotting_wagons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonParts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = TrottingWagons.MOD_ID, value = Dist.CLIENT)
public class WagonHighlight {

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if(player == null || minecraft.level == null)
            return;
        AbstractWagon wagon = null;
        WagonParts.Part part = null;
        double reach = player.entityInteractionRange();
        double best = Double.MAX_VALUE;
        Vec3 eye = player.getEyePosition();
        for(AbstractWagon nearby : minecraft.level.getEntitiesOfClass(AbstractWagon.class, player.getBoundingBox().inflate(reach + 2))) {
            WagonParts.Part picked = WagonParts.pick(nearby, player);
            if(picked == null)
                continue;
            for(AABB local : picked.local()) {
                double distance = WagonParts.worldBox(nearby, local).distanceToSqr(eye);
                if(distance < best) {
                    best = distance;
                    wagon = nearby;
                    part = picked;
                }
            }
        }
        if(wagon == null || part == null)
            return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        pose.pushPose();
        for(AABB local : part.local()) {
            AABB world = WagonParts.worldBox(wagon, local).move(-camera.x, -camera.y, -camera.z);
            LevelRenderer.renderLineBox(pose, lines, world, 0.0F, 0.0F, 0.0F, 0.4F);
        }
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

}
