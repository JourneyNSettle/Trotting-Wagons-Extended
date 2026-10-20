package net.favouriteless.trotting_wagons.compat;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class BlockRunnerSpeed {

    private static final String MOD_ID = "blockrunner";
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "block_speed");
    private static final Method SPEED_FACTOR = lookup();

    private BlockRunnerSpeed() {}

    public static double factor(Block block) {
        if(SPEED_FACTOR == null)
            return 1.0D;
        try {
            return (double)SPEED_FACTOR.invoke(null, block);
        } catch(ReflectiveOperationException exception) {
            return 1.0D;
        }
    }

    public static double movementSpeed(AttributeInstance attribute) {
        double value = attribute.getValue();
        if(SPEED_FACTOR == null)
            return value;
        AttributeModifier blockSpeed = attribute.getModifier(MODIFIER_ID);
        if(blockSpeed == null || blockSpeed.operation() != AttributeModifier.Operation.ADD_VALUE)
            return value;
        double base = attribute.getBaseValue();
        double added = -blockSpeed.amount();
        double multipliedBase = 0.0D;
        double multipliedTotal = 1.0D;
        for(AttributeModifier modifier : attribute.getModifiers()) {
            switch(modifier.operation()) {
                case ADD_VALUE -> added += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multipliedBase += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal *= 1.0D + modifier.amount();
            }
        }
        return attribute.getAttribute().value().sanitizeValue((base + added) * (1.0D + multipliedBase) * multipliedTotal);
    }

    private static Method lookup() {
        try {
            if(!ModList.get().isLoaded(MOD_ID))
                return null;
            return Class.forName("fuzs.blockrunner.world.level.block.data.BlockSpeed").getMethod("getSpeedFactor", Block.class);
        } catch(Throwable exception) {
            TrottingWagons.LOGGER.warn("Block Runner is present, but its block speed lookup could not be used", exception);
            return null;
        }
    }

}
