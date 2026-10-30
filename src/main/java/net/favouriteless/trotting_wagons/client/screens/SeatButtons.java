package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.WagonLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class SeatButtons {

    private SeatButtons() {}

    public static List<SeatButton> create(AbstractWagon wagon, int left, int top, int panelWidth, int draftY) {
        int count = wagon.getSeatCount();
        int stride = SeatButton.SIZE + 2;
        int perRow = Math.max(1, (panelWidth - 8) / stride);
        List<SeatButton> buttons = new ArrayList<>(count + 2);
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
        if(wagon.hitchSlots() > 0)
            buttons.add(draftButton(wagon, AbstractWagon.Side.LEFT, left, draftY));
        if(wagon.hitchSlots() > 1)
            buttons.add(draftButton(wagon, AbstractWagon.Side.RIGHT, left, draftY));
        SeatButton.bind(buttons);
        return buttons;
    }

    public static List<SeatButton> place(AbstractWagon wagon, int[] seatX, int seatY, int draftX, int draftY, int size) {
        return place(wagon, seatX, seatY, draftX, draftY, size, size);
    }

    public static List<SeatButton> place(AbstractWagon wagon, int[] seatX, int seatY, int draftX, int draftY, int size, int draftStride) {
        List<SeatButton> buttons = new ArrayList<>(seatX.length + 2);
        for(int i = 0; i < wagon.getSeatCount() && i < seatX.length; i++) {
            SeatButton button = SeatButton.placed(wagon, i, seatX[i], seatY, size);
            button.refresh();
            buttons.add(button);
        }
        if(wagon.hitchSlots() > 0) {
            SeatButton left = SeatButton.placedDraft(wagon, AbstractWagon.Side.LEFT, draftX, draftY, size);
            left.refresh();
            buttons.add(left);
        }
        if(wagon.hitchSlots() > 1) {
            SeatButton right = SeatButton.placedDraft(wagon, AbstractWagon.Side.RIGHT, draftX + draftStride, draftY, size);
            right.refresh();
            buttons.add(right);
        }
        SeatButton.bind(buttons);
        return buttons;
    }

    private static SeatButton draftButton(AbstractWagon wagon, AbstractWagon.Side side, int left, int y) {
        SeatButton button = SeatButton.draft(wagon, side, WagonLayout.draftX(left, side.ordinal()), y);
        button.refresh();
        return button;
    }

    public static void refresh(List<SeatButton> buttons) {
        for(SeatButton button : buttons)
            button.refresh();
    }

    public static void drawHealth(GuiGraphics graphics, Font font, AbstractWagon wagon, int x, int y, int width) {
        int max = Math.max(1, wagon.getMaxHealth());
        int current = Mth.clamp(Math.round(wagon.getHealth()), 0, max);
        int fill = current >= max ? width : (int)(width * (current / (float)max));
        graphics.fill(x, y, x + width, y + 10, 0xFF2B2B2B);
        graphics.fill(x, y, x + fill, y + 10, 0xFF8B2E2E);
        String text = current + " / " + wagon.getMaxHealth();
        graphics.drawString(font, text, x + width / 2 - font.width(text) / 2, y + 1, 0xFFFFFF, true);
    }

}
