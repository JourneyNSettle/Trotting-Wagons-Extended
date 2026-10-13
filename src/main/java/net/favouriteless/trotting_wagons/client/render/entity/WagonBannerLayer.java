package net.favouriteless.trotting_wagons.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class WagonBannerLayer<T extends AbstractWagon> extends GeoRenderLayer<T> {

    private static final ModelPart FLAG = BannerRenderer.createBodyLayer().bakeRoot().getChild("flag");

    public WagonBannerLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, T wagon, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                              VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(!bone.getName().equals("banner") || !(wagon instanceof DebugWagon debugWagon))
            return;
        ItemStack stack = debugWagon.getBannerStack();
        if(!(stack.getItem() instanceof BannerItem banner))
            return;
        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        poseStack.pushPose();
        poseStack.translate(bone.getPivotX() / 16F, bone.getPivotY() / 16F, (bone.getPivotZ() - 2F) / 16F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        FLAG.y = 0;
        BannerRenderer.renderPatterns(poseStack, bufferSource, packedLight, packedOverlay, FLAG, ModelBakery.BANNER_BASE, true, banner.getColor(), patterns);
        poseStack.popPose();
    }

}
