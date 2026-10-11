package net.favouriteless.trotting_wagons.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

final class WagonGui {

    private WagonGui() {}

    static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        graphics.fill(x, y, x + 1, y + height, 0xFF000000);
        graphics.fill(x + 1, y, x + 3, y + height, 0xFFFFFFFF);
        graphics.fill(x + width - 3, y, x + width - 1, y + height, 0xFF555555);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF000000);
        graphics.fill(x, y, x + width, y + 1, 0xFF000000);
        graphics.fill(x + 3, y + 1, x + width - 3, y + 3, 0xFFFFFFFF);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF000000);
        graphics.fill(x + 3, y + height - 3, x + width - 3, y + height - 1, 0xFF555555);
    }

    static void drawSlot(GuiGraphics graphics, int left, int top, Slot slot) {
        int x = left + slot.x - 1;
        int y = top + slot.y - 1;
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
    }

}
