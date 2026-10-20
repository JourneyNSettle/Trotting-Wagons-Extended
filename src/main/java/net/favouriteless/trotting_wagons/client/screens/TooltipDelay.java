package net.favouriteless.trotting_wagons.client.screens;

import net.minecraft.Util;
import net.minecraft.world.inventory.Slot;

final class TooltipDelay {

    static final long MILLIS = 1000L;

    private Slot slot;
    private long start = -1L;

    boolean ready(Slot hovered) {
        if(hovered == null) {
            slot = null;
            start = -1L;
            return false;
        }
        if(hovered != slot) {
            slot = hovered;
            start = Util.getMillis();
            return false;
        }
        return Util.getMillis() - start >= MILLIS;
    }

}
