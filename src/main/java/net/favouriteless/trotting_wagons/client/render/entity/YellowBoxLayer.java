package net.favouriteless.trotting_wagons.client.render.entity;

import com.kltyton.bonehitboxlib.client.geometry.bounds.PartBounds;
import com.kltyton.bonehitboxlib.geometry.obb.ObbGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** World boxes of the hitbox bone, taken from the same cubes that draw the yellow outline. */
public class YellowBoxLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    public record Capture(List<ObbGeometry> boxes, long gameTime) {}

    private static final Map<Integer, Capture> CAPTURES = new ConcurrentHashMap<>();
    private static final ThreadLocal<List<ObbGeometry>> FILLING = new ThreadLocal<>();

    public YellowBoxLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    public static Capture capture(int entityId) {
        return CAPTURES.get(entityId);
    }

    public static Iterable<Map.Entry<Integer, Capture>> captures() {
        return CAPTURES.entrySet();
    }

    private static boolean isCollisionBone(String name) {
        return name.equals("hitbox") || name.startsWith("Barrel_");
    }

    @Override
    public void render(PoseStack pose, T animatable, BakedGeoModel model, RenderType type, MultiBufferSource buffer,
                       VertexConsumer consumer, float partialTick, int packedLight, int packedOverlay) {
        if(!(animatable instanceof Entity entity))
            return;
        List<ObbGeometry> boxes = new ArrayList<>();
        FILLING.set(boxes);
        CAPTURES.put(entity.getId(), new Capture(boxes, entity.level().getGameTime()));
    }

    @Override
    public void renderForBone(PoseStack pose, T animatable, GeoBone bone, RenderType type, MultiBufferSource buffer,
                              VertexConsumer consumer, float partialTick, int packedLight, int packedOverlay) {
        List<ObbGeometry> boxes = FILLING.get();
        if(boxes == null || bone.isHidden() || !isCollisionBone(bone.getName()))
            return;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        for(GeoCube cube : bone.getCubes()) {
            if(cube.quads() == null)
                continue;
            pose.pushPose();
            RenderUtil.translateToPivotPoint(pose, cube);
            RenderUtil.rotateMatrixAroundCube(pose, cube);
            RenderUtil.translateAwayFromPivotPoint(pose, cube);
            PartBounds.BoundsBuilder builder = PartBounds.builder(pose.last().pose());
            for(var quad : cube.quads()) {
                if(quad == null)
                    continue;
                for(var vertex : quad.vertices()) {
                    var position = vertex.position();
                    builder.include(position.x(), position.y(), position.z());
                }
            }
            PartBounds bounds = builder.build();
            pose.popPose();
            if(bounds != null)
                boxes.add(bounds.geometry(camera));
        }
    }

}
