package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.ArmoredMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ArmoredScreen extends AbstractContainerScreen<ArmoredMenu> {

    private List<SeatButton> seatButtons;

    public ArmoredScreen(ArmoredMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 161;
        this.titleLabelY = 4;
        this.inventoryLabelX = 80;
        this.inventoryLabelY = 65;
    }

    @Override
    protected void init() {
        super.init();
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        seatButtons = SeatButtons.create(wagon, leftPos, topPos + 28, imageWidth, topPos + 28);
        for(SeatButton button : seatButtons)
            addRenderableWidget(button);
        if(minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
            addRenderableWidget(new WagonLockButton(leftPos + 6, topPos + imageHeight - 22, wagon));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        WagonGui.drawStatus(graphics, font, menu.getWagon(), titleLabelX, titleLabelY, imageWidth);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(seatButtons != null && menu.getWagon() != null)
            SeatButtons.refresh(seatButtons);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WagonGui.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight);
        if(menu.getWagon() != null)
            SeatButtons.drawHealth(graphics, font, menu.getWagon(), leftPos + 16, topPos + 14, imageWidth - 32);
        for(var slot : menu.slots)
            WagonGui.drawSlot(graphics, leftPos, topPos, slot);
        WagonGui.drawWheelCaption(graphics, font, menu, menu.getWagon(), leftPos, topPos);
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
