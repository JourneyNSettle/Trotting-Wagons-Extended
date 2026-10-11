package net.favouriteless.trotting_wagons.client.render.entity;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.ConestogaWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.RoyalWagon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class WagonModel<T extends AbstractWagon> extends DefaultedEntityGeoModel<T> {

    private final String texturePathPrefix;

    public WagonModel(ResourceLocation assetSubpath) {
        super(assetSubpath, false);
        this.texturePathPrefix = "textures/entity/" + assetSubpath.getPath() + "/";
    }

    @Override
    public ResourceLocation getTextureResource(T wagon) {
        DyeColor color = wagon.getColor();
        return TrottingWagons.id(texturePathPrefix + (color != null ? color.getName() : "none") + ".png");
    }

    @Override
    public void setCustomAnimations(T wagon, long instanceId, AnimationState<T> state) {
        super.setCustomAnimations(wagon, instanceId, state);

        AnimationProcessor<T> processor = getAnimationProcessor();
        float partialTick = (float)state.animationTick - (int)state.animationTick;
        float rot = wagon.getWheelRot(partialTick);

        processor.getBone("FrontWheels").setRotX(-rot);
        processor.getBone("RearWheels").setRotX(-rot);
        hideMissingWheels(processor, wagon);

        AbstractWagon.WheelSag sag = wagon.wheelSag();
        float bodyPitch = wagon.getXRot(partialTick) + sag.pitch();
        processor.getBone("Tongue").setRotX(wagon.tongueAngle(bodyPitch, sag.roll(), sag.drop()));
    }

    private static void hideMissingWheels(AnimationProcessor<?> processor, AbstractWagon wagon) {
        String[] bones = wagon instanceof ConestogaWagon ? CONESTOGA_WHEELS
                : wagon instanceof RoyalWagon ? ROYAL_WHEELS
                : ARMORED_WHEELS;
        int mask = wagon.getWheelMask();
        for(int i = 0; i < bones.length; i++) {
            GeoBone bone = processor.getBone(bones[i]);
            if(bone != null)
                bone.setHidden((mask & (1 << i)) == 0);
        }
    }

    private static final String[] CONESTOGA_WHEELS = {"wheelleft", "wheelright", "bone", "bone2"};
    private static final String[] ROYAL_WHEELS = {"octagon2", "octagon", "octagon4", "octagon3"};
    private static final String[] ARMORED_WHEELS = {"bone3", "bone4", "bone", "bone2"};

}
