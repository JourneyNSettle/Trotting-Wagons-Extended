package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.WheelSlot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

    static void drawWheelCaption(GuiGraphics graphics, Font font, AbstractContainerMenu menu, AbstractWagon wagon, int left, int top) {
        int minX = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for(Slot slot : menu.slots) {
            if(!(slot instanceof WheelSlot))
                continue;
            minX = Math.min(minX, slot.x);
            maxY = Math.max(maxY, slot.y);
        }
        if(minX == Integer.MAX_VALUE || wagon == null)
            return;
        int center = left + minX + 18;
        int y = top + maxY + 19;
        Component title = Component.translatable("gui.trotting_wagons.wheels");
        Component state = wagon.wheelSet().label();
        graphics.drawString(font, title, center - font.width(title) / 2, y, 4210752, false);
        graphics.drawString(font, state, center - font.width(state) / 2, y + 10, 4210752, false);
    }

}
