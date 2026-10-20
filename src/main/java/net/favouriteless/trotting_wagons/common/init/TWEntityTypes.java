package net.favouriteless.trotting_wagons.common.init;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.wagons.AnimalWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.ArmoredWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.ConestogaWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.RoyalWagon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TWEntityTypes {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TrottingWagons.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ConestogaWagon>> CONESTOGA_WAGON = REGISTRY.register("conestoga_wagon", () ->
            EntityType.Builder.of(ConestogaWagon::new, MobCategory.MISC).sized(3.0F, 2.9F).build("conestoga_wagon"));

    public static final DeferredHolder<EntityType<?>, EntityType<RoyalWagon>> ROYAL_WAGON = REGISTRY.register("royal_wagon", () ->
            EntityType.Builder.of(RoyalWagon::new, MobCategory.MISC).sized(3.0F, 2.9F).build("royal_wagon"));

    public static final DeferredHolder<EntityType<?>, EntityType<ArmoredWagon>> ARMORED_WAGON = REGISTRY.register("armored_wagon", () ->
            EntityType.Builder.of(ArmoredWagon::new, MobCategory.MISC).sized(3.0F, 2.9F).build("armored_wagon"));

    public static final DeferredHolder<EntityType<?>, EntityType<DebugWagon>> DEBUG_WAGON = REGISTRY.register("debug_wagon", () ->
            EntityType.Builder.of(DebugWagon::new, MobCategory.MISC).sized(3.5F, 4.0F).build("debug_wagon"));

    public static final DeferredHolder<EntityType<?>, EntityType<AnimalWagon>> ANIMAL_WAGON = REGISTRY.register("animal_wagon", () ->
            EntityType.Builder.of(AnimalWagon::new, MobCategory.MISC).sized(3.5F, 2.75F).build("animal_wagon"));

}
