package net.favouriteless.trotting_wagons.common.items;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** An inventory item drawn from a GeckoLib model instead of a flat sprite. */
public class PartItem extends Item implements GeoItem {

    private final String model;
    private final String texture;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PartItem(Properties properties, String model) {
        this(properties, model, "textures/item/misc.png");
    }

    public PartItem(Properties properties, String model, String texture) {
        super(properties);
        this.model = model;
        this.texture = texture;
    }

    public String model() {
        return model;
    }

    public String texture() {
        return texture;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
