package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.menus.AnimalMenu;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AnimalScreen extends AbstractContainerScreen<AnimalMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/animal_wagon.png");

    private final TooltipDelay tooltips = new TooltipDelay();
    private List<SeatButton> seatButtons;

    public AnimalScreen(AnimalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = AnimalMenu.IMAGE_HEIGHT;
        this.titleLabelY = 6;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        seatButtons = placeSeats(wagon);
        for(SeatButton button : seatButtons)
            addRenderableWidget(button);
        if(minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
            addRenderableWidget(new WagonLockButton(leftPos + 20, topPos + 160, 26, wagon));
    }

    private List<SeatButton> placeSeats(AbstractWagon wagon) {
        // Front is on the left, next to the driver. Top row is the wagon's left, bottom row its right, and the two inset cells are the large seats on the centerline.
        int[] seats = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] xs = {81, 107, 107, 133, 159, 159, 185, 211, 211};
        int[] ys = {50, 37, 63, 50, 37, 63, 50, 37, 63};
        List<SeatButton> buttons = new ArrayList<>(seats.length + 2);
        for(int i = 0; i < seats.length; i++) {
            SeatButton button = SeatButton.placed(wagon, seats[i], leftPos + xs[i], topPos + ys[i], 26);
            button.refresh();
            buttons.add(button);
        }
        SeatButton left = SeatButton.placedDraft(wagon, AbstractWagon.Side.LEFT, leftPos + 8, topPos + 50, 26);
        left.refresh();
        buttons.add(left);
        SeatButton right = SeatButton.placedDraft(wagon, AbstractWagon.Side.RIGHT, leftPos + 34, topPos + 50, 26);
        right.refresh();
        buttons.add(right);
        SeatButton.bind(buttons);
        return buttons;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        Component distance = Component.translatable("gui.trotting_wagons.distance", String.format(java.util.Locale.ROOT, "%.1f", wagon.getDistance() / 1000.0F));
        int distanceX = imageWidth - 8 - font.width(distance);
        graphics.drawString(font, distance, distanceX, titleLabelY, 4210752, false);
        String name = wagon.getOwnerName();
        if(!name.isEmpty()) {
            Component owner = Component.translatable("gui.trotting_wagons.owner", name);
            int ownerX = (imageWidth - font.width(owner)) / 2;
            int limit = distanceX - 6;
            if(ownerX + font.width(owner) > limit)
                ownerX = limit - font.width(owner);
            graphics.drawString(font, owner, ownerX, titleLabelY, 4210752, false);
        }
        int wheelCenter = AnimalMenu.WHEEL_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.wheels"), wheelCenter, 78);
        drawCentered(graphics, wagon.wheelSet().label(), wheelCenter, 88);
        int slotsCenter = AnimalMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, 60);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, 70);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if(hoveredSlot != null && hoveredSlot.hasItem()) {
            tooltips.ready(null);
            super.renderTooltip(graphics, mouseX, mouseY);
            return;
        }
        if(!tooltips.ready(hoveredSlot) || hoveredSlot == null || !isWagonSlot(hoveredSlot))
            return;
        Component tip = emptySlotTip(hoveredSlot);
        if(tip != null)
            graphics.renderTooltip(font, tip, mouseX, mouseY);
    }

    private boolean isWagonSlot(Slot slot) {
        int index = menu.slots.indexOf(slot);
        return index >= 0 && (index < AbstractWagon.WHEEL_COUNT || index >= menu.slots.size() - AbstractWagon.REPAIR_SLOTS);
    }

    private Component emptySlotTip(Slot slot) {
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
            SeatButtons.drawHealth(graphics, font, menu.getWagon(), leftPos + AnimalMenu.HEALTH_X, topPos + AnimalMenu.HEALTH_Y, AnimalMenu.HEALTH_W);
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
