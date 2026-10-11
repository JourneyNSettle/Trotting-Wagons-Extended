package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class SeatButtons {

    private SeatButtons() {}

    public static List<SeatButton> create(AbstractWagon wagon, int left, int top, int panelWidth) {
        int count = wagon.getSeatCount();
        int stride = SeatButton.SIZE + 2;
        int perRow = Math.max(1, (panelWidth - 8) / stride);
        List<SeatButton> buttons = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            int row = i / perRow;
            int col = i % perRow;
            int rowCount = Math.min(perRow, count - row * perRow);
            int rowWidth = rowCount * SeatButton.SIZE + (rowCount - 1) * 2;
            int x = left + Math.max(4, (panelWidth - rowWidth) / 2) + col * stride;
            SeatButton button = new SeatButton(wagon, i, x, top + row * stride);
            button.refresh();
            buttons.add(button);
        }
        SeatButton.bind(buttons);
        return buttons;
    }

    public static void refresh(List<SeatButton> buttons) {
        for(SeatButton button : buttons)
            button.refresh();
    }

    public static void drawHealth(GuiGraphics graphics, Font font, AbstractWagon wagon, int x, int y, int width) {
        int max = Math.max(1, wagon.getMaxHealth());
        int current = Math.round(wagon.getHealth());
        int fill = (int)(width * Mth.clamp(wagon.getHealth() / max, 0F, 1F));
        graphics.fill(x, y, x + width, y + 10, 0xFF2B2B2B);
        graphics.fill(x, y, x + fill, y + 10, 0xFF8B2E2E);
        String text = current + " / " + wagon.getMaxHealth();
        graphics.drawString(font, text, x + width / 2 - font.width(text) / 2, y + 1, 0xFFFFFF, true);
    }

}
