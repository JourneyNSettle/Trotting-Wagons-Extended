package net.favouriteless.trotting_wagons.client.render;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.items.PartItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class PartItemModel extends GeoModel<PartItem> {

    @Override
    public ResourceLocation getModelResource(PartItem item) {
        return TrottingWagons.id("geo/item/" + item.model() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PartItem item) {
        return TrottingWagons.id(item.texture());
    }

    /** The chassis file also carries the base posts and the collision bone. The item is only the frame. */
    @Override
    public void setCustomAnimations(PartItem item, long instanceId, AnimationState<PartItem> state) {
        if(!"chassis".equals(item.model()))
            return;
        hide("hitbox");
        hide("cart");
    }

    private void hide(String name) {
        GeoBone bone = getAnimationProcessor().getBone(name);
        if(bone != null)
            bone.setHidden(true);
    }

    @Override
    public ResourceLocation getAnimationResource(PartItem item) {
        return TrottingWagons.id("animations/item/part.animation.json");
    }

}
