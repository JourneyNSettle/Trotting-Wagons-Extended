package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.DebugMenu;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DebugScreen extends AbstractContainerScreen<DebugMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/debug_wagon.png");
    private static final long TOOLTIP_DELAY_MS = 1000L;
    private List<SeatButton> seatButtons;
    private Slot tooltipSlot;
    private long tooltipStart = -1L;

    public DebugScreen(DebugMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 196;
        this.titleLabelY = 6;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        seatButtons = SeatButtons.place(wagon, new int[] {leftPos + 108, leftPos + 134, leftPos + 160, leftPos + 186}, topPos + 60, leftPos + 21, topPos + 60, 26);
        for(SeatButton button : seatButtons)
            addRenderableWidget(button);
        if(minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
            addRenderableWidget(new WagonLockButton(leftPos + 20, topPos + 160, 26, wagon));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        String name = wagon.getOwnerName();
        if(!name.isEmpty())
            graphics.drawString(font, Component.translatable("gui.trotting_wagons.owner", name), 108, 6, 4210752, false);
        Component distance = Component.translatable("gui.trotting_wagons.distance", String.format(java.util.Locale.ROOT, "%.1f", wagon.getDistance() / 1000.0F));
        graphics.drawString(font, distance, imageWidth - 8 - font.width(distance), 6, 4210752, false);
        graphics.drawString(font, Component.translatable("gui.trotting_wagons.wheels"), 16, 92, 4210752, false);
        graphics.drawString(font, wagon.wheelSet().label(), 16, 102, 4210752, false);
        int slotsCenter = DebugMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, 60);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, 70);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items"), slotsCenter, 126);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items.line2"), slotsCenter, 136);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if(hoveredSlot != null && isWagonSlot(hoveredSlot)) {
            Component tip = delayedSlotTip(hoveredSlot);
            if(tip != null)
                graphics.renderTooltip(font, tip, mouseX, mouseY);
            return;
        }
        tooltipSlot = null;
        tooltipStart = -1L;
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean isWagonSlot(Slot slot) {
        int index = menu.slots.indexOf(slot);
        return index >= 0 && (index < 8 || index >= menu.slots.size() - 6);
    }

    private Component delayedSlotTip(Slot slot) {
        if(slot != tooltipSlot) {
            tooltipSlot = slot;
            tooltipStart = Util.getMillis();
            return null;
        }
        if(Util.getMillis() - tooltipStart < TOOLTIP_DELAY_MS)
            return null;
        if(slot.hasItem())
            return slot.getItem().getHoverName();
        if(slot instanceof RepairSlot repair && menu.getWagon() != null)
            return menu.getWagon().repairTooltip(repair.getSlotIndex());
        ItemStack icon = WagonGui.ghostIcon(menu, slot);
        return icon.isEmpty() ? null : icon.getHoverName();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(seatButtons != null && menu.getWagon() != null)
            SeatButtons.refresh(seatButtons);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 320, 196);
        if(menu.getWagon() != null)
            SeatButtons.drawHealth(graphics, font, menu.getWagon(), leftPos + DebugMenu.HEALTH_X, topPos + DebugMenu.HEALTH_Y, DebugMenu.HEALTH_W);
        for(var slot : menu.slots) {
            if(slot instanceof RepairSlot && slot.isActive())
                WagonGui.drawSlot(graphics, leftPos, topPos, slot);
        }
        WagonGui.drawEmptySlotIcons(graphics, leftPos, topPos, menu);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(SeatButton.press(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        SeatButton.markDrag(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(SeatButton.finishDrag(mouseX, mouseY))
            return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        SeatButton.renderCarried(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
        if(seatButtons == null)
            return;
        for(SeatButton button : seatButtons) {
            var tip = button.delayedTooltip();
            if(tip != null)
                graphics.renderTooltip(font, tip, mouseX, mouseY);
        }
    }

}
