package net.favouriteless.trotting_wagons.client.render;

import net.favouriteless.trotting_wagons.common.items.PartItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PartItemRenderer extends GeoItemRenderer<PartItem> {

    public PartItemRenderer() {
        super(new PartItemModel());
        useAlternateGuiLighting();
    }

}
