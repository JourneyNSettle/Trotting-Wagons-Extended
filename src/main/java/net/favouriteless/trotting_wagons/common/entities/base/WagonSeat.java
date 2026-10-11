package net.favouriteless.trotting_wagons.common.entities.base;

import net.minecraft.world.phys.Vec3;

public record WagonSeat(Vec3 position, Kind kind) {

    public enum Kind {
        DRIVER,
        PLAYER,
        ANIMAL,
        ANIMALBIG,
        UNIVERSAL,
        DRAFT
    }

    public boolean acceptsPlayers() {
        return kind == Kind.DRIVER || kind == Kind.PLAYER || kind == Kind.UNIVERSAL;
    }

    public boolean canLockAnimals() {
        return kind == Kind.ANIMAL || kind == Kind.ANIMALBIG || kind == Kind.UNIVERSAL;
    }

}
