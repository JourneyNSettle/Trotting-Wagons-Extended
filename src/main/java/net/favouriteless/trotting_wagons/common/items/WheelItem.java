package net.favouriteless.trotting_wagons.common.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WheelItem extends PartItem {

    public WheelItem(Properties properties, String model) {
        super(properties, model);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int max = stack.getMaxDamage();
        if(max <= 0)
            max = 100;
        int remaining = max - stack.getDamageValue();
        tooltip.add(Component.translatable("item.trotting_wagons.wheel.durability", remaining, max));
    }

}
