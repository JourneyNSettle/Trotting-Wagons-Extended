package net.favouriteless.trotting_wagons.common.init;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TWCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrottingWagons.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTRY.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.trotting_wagons"))
            .icon(() -> new ItemStack(TWItems.WHEEL.get()))
            .displayItems((params, output) -> {
                output.accept(TWItems.HORSE_WHIP.get());
                output.accept(TWItems.TONGUE.get());
                output.accept(TWItems.SHAFTS.get());
                output.accept(TWItems.WHEEL.get());
                output.accept(TWItems.WIDE_WHEEL.get());
                output.accept(TWItems.REINFORCED_WHEEL.get());
                output.accept(TWItems.LONG_STICK.get());
                output.accept(TWItems.CHASSIS.get());
            })
            .build());

}
