package net.favouriteless.trotting_wagons.client.render.entity;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.ConestogaWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
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
        if(wagon instanceof DebugWagon)
            return TrottingWagons.id(texturePathPrefix + "none.png");
        DyeColor color = wagon.getColor();
        return TrottingWagons.id(texturePathPrefix + (color != null ? color.getName() : "none") + ".png");
    }

    @Override
    public void setCustomAnimations(T wagon, long instanceId, AnimationState<T> state) {
        super.setCustomAnimations(wagon, instanceId, state);

        AnimationProcessor<T> processor = getAnimationProcessor();
        float partialTick = (float)state.animationTick - (int)state.animationTick;
        float rot = wagon.getWheelRot(partialTick);

        spinAxle(processor, "FrontWheels", -rot);
        spinAxle(processor, "RearWheels", -rot);
        spinAxle(processor, "FrontWheels_Wide", -rot);
        spinAxle(processor, "RearWheels_Wide", -rot);
        spinAxle(processor, "FrontWheels_Iron", -rot);
        spinAxle(processor, "RearWheels_Iron", -rot);
        if(wagon instanceof DebugWagon)
            hideDebugWheels(processor, wagon);
        else
            hideMissingWheels(processor, wagon);

        AbstractWagon.WheelSag sag = wagon.wheelSag();
        float bodyPitch = wagon.getXRot(partialTick) + sag.pitch();
        processor.getBone("Tongue").setRotX(wagon.tongueAngle(bodyPitch, sag.roll(), sag.drop()));
        if(wagon instanceof DebugWagon debugWagon) {
            GeoBone ladder = processor.getBone("Ladder");
            if(ladder != null)
                ladder.setRotZ(debugWagon.ladderRender(partialTick) * DebugWagon.LADDER_DROP);
            hidePumpkins(processor, debugWagon.getPumpkinCount());
            boolean bare = debugWagon.getBannerStack().isEmpty();
            GeoBone banner = processor.getBone("banner");
            GeoBone stand = processor.getBone("stand");
            GeoBone lamp = processor.getBone("Lamp");
            if(banner != null)
                banner.setHidden(bare);
            if(stand != null)
                stand.setHidden(bare);
            if(lamp != null)
                lamp.setHidden(!debugWagon.hasLantern());
        }
    }

    private static void spinAxle(AnimationProcessor<?> processor, String name, float rot) {
        GeoBone bone = processor.getBone(name);
        if(bone != null)
            bone.setRotX(rot);
    }

    private static void hideDebugWheels(AnimationProcessor<?> processor, AbstractWagon wagon) {
        for(int i = 0; i < DEBUG_WHEEL_VARIANTS.length; i++) {
            int show = wagon.getWheelKind(i).ordinal() - 1;
            String[] names = DEBUG_WHEEL_VARIANTS[i];
            for(int variant = 0; variant < names.length; variant++) {
                GeoBone bone = processor.getBone(names[variant]);
                if(bone != null)
                    bone.setHidden(variant != show);
            }
        }
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
    private static final String[][] DEBUG_WHEEL_VARIANTS = {
            {"LeftFrontWheel", "LeftFrontWheel2", "LeftFrontWheel3"},
            {"RightFrontWheel", "RightFrontWheel2", "RightFrontWheel3"},
            {"LeftRearWheel", "LeftRearWheel2", "LeftRearWheel3"},
            {"RightRearWheel", "RightRearWheel2", "RightRearWheel3"}
    };

    private static void hidePumpkins(AnimationProcessor<?> processor, int count) {
        for(int i = 0; i < DebugWagon.PUMPKIN_SLOTS; i++) {
            GeoBone bone = processor.getBone("pumpkin" + (i + 1));
            if(bone != null)
                bone.setHidden(i >= count);
        }
    }

}
