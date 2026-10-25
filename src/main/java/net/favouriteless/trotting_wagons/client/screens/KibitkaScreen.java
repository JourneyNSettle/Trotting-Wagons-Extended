package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.KibitkaWagon;
import net.favouriteless.trotting_wagons.common.menus.KibitkaMenu;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.favouriteless.trotting_wagons.common.network.packets.KibitkaRespawnPayload;
import net.favouriteless.trotting_wagons.common.network.packets.KibitkaSleepPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

import java.util.List;

public class KibitkaScreen extends AbstractContainerScreen<KibitkaMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/kibitka_wagon.png");

    private List<SeatButton> seatButtons;

    public KibitkaScreen(KibitkaMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = KibitkaMenu.IMAGE_WIDTH;
        this.imageHeight = KibitkaMenu.IMAGE_HEIGHT;
        this.titleLabelY = 6;
        this.inventoryLabelX = KibitkaMenu.INV_X;
        this.inventoryLabelY = KibitkaMenu.INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        seatButtons = SeatButtons.place(wagon, new int[] {leftPos + 70, leftPos + 96}, topPos + 99, leftPos + 22, topPos + 99, 24);
        for(SeatButton button : seatButtons)
            addRenderableWidget(button);
        if(wagon instanceof KibitkaWagon kibitka) {
            addRenderableWidget(new KibitkaCampButton(leftPos + 161, topPos + 98, kibitka));
            addRenderableWidget(new KibitkaActionButton(leftPos + 191, topPos + 98, 26, kibitka, Items.RED_BED,
                    "gui.trotting_wagons.kibitka.bed", "gui.trotting_wagons.kibitka.sleep", new KibitkaSleepPayload(kibitka.getId())));
            addRenderableWidget(new KibitkaActionButton(leftPos + 221, topPos + 98, 26, kibitka, Items.COMPASS,
                    "gui.trotting_wagons.kibitka.respawn", "gui.trotting_wagons.kibitka.respawn", new KibitkaRespawnPayload(kibitka.getId())));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        WagonGui.drawHeader(graphics, font, menu.getWagon(), titleLabelY, imageWidth);
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        int slotsCenter = KibitkaMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, KibitkaMenu.REPAIR_Y - 22);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, KibitkaMenu.REPAIR_Y - 12);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items"), slotsCenter, KibitkaMenu.CUSTOM_Y);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.custom_items.line2"), slotsCenter, KibitkaMenu.CUSTOM_Y + 10);
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, KibitkaMenu.IMAGE_WIDTH, KibitkaMenu.IMAGE_HEIGHT);
        AbstractWagon wagon = menu.getWagon();
        if(wagon != null)
            SeatButtons.drawHealth(graphics, font, wagon, leftPos + KibitkaMenu.HEALTH_X, topPos + KibitkaMenu.HEALTH_Y, KibitkaMenu.HEALTH_W);
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
