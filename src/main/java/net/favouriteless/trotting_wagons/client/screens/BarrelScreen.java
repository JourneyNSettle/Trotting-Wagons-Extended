package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.BarrelWagon;
import net.favouriteless.trotting_wagons.common.menus.BarrelContentSlot;
import net.favouriteless.trotting_wagons.common.menus.BarrelMenu;
import net.favouriteless.trotting_wagons.common.menus.BarrelSlot;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Locale;

public class BarrelScreen extends AbstractContainerScreen<BarrelMenu> {

    private final TooltipDelay tooltips = new TooltipDelay();
    private WagonLockButton lockButton;
    private int healthY = 74;
    private int labelY = 88;

    public BarrelScreen(BarrelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BarrelMenu.IMAGE_WIDTH;
        this.imageHeight = 210;
        this.titleLabelY = 6;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        BarrelWagon wagon = menu.getWagon();
        if(wagon != null && minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player)) {
            lockButton = new WagonLockButton(leftPos + 6, topPos + imageHeight - 20, wagon);
            addRenderableWidget(lockButton);
        }
        layout();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        layout();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void layout() {
        BarrelWagon wagon = menu.getWagon();
        int rows = BarrelWagon.BARRELS / 2;
        healthY = BarrelMenu.BARREL_Y + rows * 18 + 6;
        labelY = healthY + 14;
        int wheelY = labelY + 22;
        imageHeight = wheelY + 76 + 24;
        topPos = (height - imageHeight) / 2;
        int wheel = 0;
        int player = 0;
        int repair = 0;
        int content = 0;
        for(int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if(slot instanceof BarrelSlot) {
                int index = slot.getSlotIndex();
                moveSlot(slot, BarrelMenu.barrelX(index), BarrelMenu.barrelY(index));
            } else if(slot instanceof BarrelContentSlot) {
                placeContent(wagon, slot, content++);
            } else if(i >= menu.wheelStart() && i < menu.playerStart()) {
                moveSlot(slot, 17 + (wheel % 2) * 18, wheelY + (wheel / 2) * 18);
                wheel++;
            } else if(i >= menu.playerStart() && i < menu.repairStart()) {
                if(player < 9)
                    moveSlot(slot, 80 + player * 18, wheelY + 58);
                else {
                    int n = player - 9;
                    moveSlot(slot, 80 + (n % 9) * 18, wheelY + (n / 9) * 18);
                }
                player++;
            } else if(i >= menu.repairStart()) {
                moveSlot(slot, 270 + (repair % 2) * 18, wheelY + (repair / 2) * 18);
                repair++;
            }
        }
        if(lockButton != null) {
            lockButton.setX(leftPos + 6);
            lockButton.setY(topPos + imageHeight - 20);
        }
    }

    private void placeContent(BarrelWagon wagon, Slot slot, int shown) {
        int barrel = shown / BarrelWagon.CONTENTS;
        int offset = shown % BarrelWagon.CONTENTS;
        if(wagon == null || !wagon.barrelFilled(barrel)) {
            moveSlot(slot, -2000, -2000);
            return;
        }
        moveSlot(slot, BarrelMenu.contentX(barrel, offset), BarrelMenu.barrelY(barrel));
    }

    private static void moveSlot(Slot slot, int x, int y) {
        SlotMove.UNSAFE.putInt(slot, SlotMove.X, x);
        SlotMove.UNSAFE.putInt(slot, SlotMove.Y, y);
    }

    private static final class SlotMove {
        private static final Unsafe UNSAFE;
        private static final long X;
        private static final long Y;

        static {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                UNSAFE = (Unsafe)field.get(null);
                X = UNSAFE.objectFieldOffset(Slot.class.getDeclaredField("x"));
                Y = UNSAFE.objectFieldOffset(Slot.class.getDeclaredField("y"));
            } catch(ReflectiveOperationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        AbstractWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        Component distance = Component.translatable("gui.trotting_wagons.distance", String.format(Locale.ROOT, "%.1f", wagon.getDistance() / 1000.0F));
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
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.wheels"), 17 + 17, labelY);
        drawCentered(graphics, wagon.wheelSet().label(), 17 + 17, labelY + 10);
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), 270 + 17, labelY);
        drawCentered(graphics, wagon.conditionName(), 270 + 17, labelY + 10);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WagonGui.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight);
        AbstractWagon wagon = menu.getWagon();
        if(wagon != null)
            SeatButtons.drawHealth(graphics, font, wagon, leftPos + BarrelMenu.HEALTH_X, topPos + healthY, BarrelMenu.HEALTH_W);
        for(Slot slot : menu.slots) {
            if(slot.x < 0 || slot.y < 0)
                continue;
            if(slot instanceof RepairSlot && !slot.isActive())
                continue;
            if(slot instanceof BarrelContentSlot && !slot.isActive())
                continue;
            WagonGui.drawSlot(graphics, leftPos, topPos, slot);
        }
        WagonGui.drawEmptySlotIcons(graphics, leftPos, topPos, menu);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if(hoveredSlot != null && hoveredSlot.hasItem()) {
            tooltips.ready(null);
            super.renderTooltip(graphics, mouseX, mouseY);
            return;
        }
        if(!tooltips.ready(hoveredSlot) || hoveredSlot == null || hoveredSlot.x < 0)
            return;
        Component tip = emptySlotTip(hoveredSlot);
        if(tip != null)
            graphics.renderTooltip(font, tip, mouseX, mouseY);
    }

    private Component emptySlotTip(Slot slot) {
        if(slot instanceof RepairSlot repair && menu.getWagon() != null)
            return menu.getWagon().repairTooltip(repair.getSlotIndex());
        ItemStack icon = WagonGui.ghostIcon(menu, slot);
        return icon.isEmpty() ? null : icon.getHoverName();
    }

}
