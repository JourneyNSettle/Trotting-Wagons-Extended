package net.favouriteless.trotting_wagons.client.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.init.TWItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Finished wagons are built on the base. They stay out of the ingredient list. */
@JeiPlugin
public class WagonJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return TrottingWagons.id("jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(
                new ItemStack(TWItems.ARMORED_WAGON.get()),
                new ItemStack(TWItems.DEBUG_WAGON.get()),
                new ItemStack(TWItems.ANIMAL_WAGON.get()),
                new ItemStack(TWItems.LOG_WAGON.get()),
                new ItemStack(TWItems.BARREL_WAGON.get()),
                new ItemStack(TWItems.PASSENGER_WAGON.get()),
                new ItemStack(TWItems.WARE_WAGON.get()),
                new ItemStack(TWItems.KIBITKA_WAGON.get()),
                new ItemStack(TWItems.CONESTOGA_WAGON.get()),
                new ItemStack(TWItems.ROYAL_WAGON.get()),
                new ItemStack(TWItems.BIG_CART_WAGON.get())
        ));
    }

}
