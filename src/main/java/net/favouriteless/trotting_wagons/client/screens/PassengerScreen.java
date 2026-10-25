package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.PassengerMenu;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class PassengerScreen extends AbstractContainerScreen<PassengerMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/passenger_wagon.png");

    private List<SeatButton> seatButtons;

    public PassengerScreen(PassengerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PassengerMenu.IMAGE_WIDTH;
        this.imageHeight = PassengerMenu.IMAGE_HEIGHT;
        this.titleLabelY = 6;
        this.inventoryLabelX = PassengerMenu.INV_X;
        this.inventoryLabelY = PassengerMenu.INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        int[] seats = new int[wagon.getSeatCount()];
        for(int i = 0; i < seats.length; i++)
            seats[i] = leftPos + PassengerMenu.SEAT_X + i * PassengerMenu.SEAT_STEP;
        seatButtons = SeatButtons.place(wagon, seats, topPos + PassengerMenu.SEAT_Y,
                leftPos + PassengerMenu.DRAFT_X, topPos + PassengerMenu.SEAT_Y, 24, PassengerMenu.DRAFT_STRIDE);
        for(SeatButton button : seatButtons)
            addRenderableWidget(button);
        if(minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
            addRenderableWidget(new WagonLockButton(leftPos + PassengerMenu.LOCK_X, topPos + PassengerMenu.LOCK_Y, PassengerMenu.LOCK_SIZE, wagon));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int ownerLeft = titleLabelX + font.width(title) + 12;
        WagonGui.drawHeader(graphics, font, menu.getWagon(), titleLabelY, imageWidth, ownerLeft);
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        int slotsCenter = PassengerMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, PassengerMenu.REPAIR_Y - 22);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, PassengerMenu.REPAIR_Y - 12);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items"), slotsCenter, PassengerMenu.CUSTOM_Y);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items.line2"), slotsCenter, PassengerMenu.CUSTOM_Y + 10);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(seatButtons != null && menu.getWagon() != null)
            SeatButtons.refresh(seatButtons);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, PassengerMenu.IMAGE_WIDTH, PassengerMenu.IMAGE_HEIGHT);
        AbstractWagon wagon = menu.getWagon();
        if(wagon != null)
            SeatButtons.drawHealth(graphics, font, wagon, leftPos + PassengerMenu.HEALTH_X, topPos + PassengerMenu.HEALTH_Y, PassengerMenu.HEALTH_W);
        for(Slot slot : menu.slots) {
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
