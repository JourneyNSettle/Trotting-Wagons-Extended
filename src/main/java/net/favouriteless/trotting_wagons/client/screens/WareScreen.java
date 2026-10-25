package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.wagons.WareWagon;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.favouriteless.trotting_wagons.common.menus.WareMenu;
import net.favouriteless.trotting_wagons.common.network.packets.SelectWareTabPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WareScreen extends AbstractContainerScreen<WareMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/ware_wagon.png");

    private final List<WareTabButton> tabs = new ArrayList<>();
    private List<SeatButton> seatButtons;

    public WareScreen(WareMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WareMenu.IMAGE_WIDTH;
        this.imageHeight = WareMenu.IMAGE_HEIGHT;
        this.titleLabelY = 6;
        this.inventoryLabelX = WareMenu.INV_X;
        this.inventoryLabelY = WareMenu.INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        tabs.clear();
        for(int tab = 0; tab <= WareWagon.CHESTS; tab++) {
            WareTabButton button = new WareTabButton(
                    leftPos + WareMenu.TAB_X + tab * WareMenu.TAB_STEP, topPos + WareMenu.TAB_Y, tab, menu);
            tabs.add(button);
            addRenderableWidget(button);
        }
        WareWagon wagon = menu.getWagon();
        if(wagon != null) {
            seatButtons = new ArrayList<>();
            SeatButton driver = SeatButton.placed(wagon, 0, leftPos + WareMenu.SEAT_X, topPos + WareMenu.SEAT_Y, 24);
            driver.refresh();
            seatButtons.add(driver);
            SeatButton left = SeatButton.placedDraft(wagon, AbstractWagon.Side.LEFT,
                    leftPos + WareMenu.DRAFT_X, topPos + WareMenu.DRAFT_Y, 24);
            left.refresh();
            seatButtons.add(left);
            if(wagon.hitchSlots() > 1) {
                SeatButton right = SeatButton.placedDraft(wagon, AbstractWagon.Side.RIGHT,
                        leftPos + WareMenu.DRAFT_X + WareMenu.DRAFT_STEP, topPos + WareMenu.DRAFT_Y, 24);
                right.refresh();
                seatButtons.add(right);
            }
            SeatButton.bind(seatButtons);
            for(SeatButton button : seatButtons)
                addRenderableWidget(button);
            if(minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
                addRenderableWidget(new WagonLockButton(leftPos + WareMenu.LOCK_X, topPos + WareMenu.LOCK_Y, WareMenu.LOCK_SIZE, wagon));
        }
        refreshTabs(false);
        layoutSlots();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshTabs(true);
        layoutSlots();
        if(seatButtons != null && menu.getWagon() != null)
            SeatButtons.refresh(seatButtons);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int ownerLeft = titleLabelX + font.width(title) + 12;
        WagonGui.drawHeader(graphics, font, menu.getWagon(), titleLabelY, imageWidth, ownerLeft);
        WareWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        int slotsCenter = WareMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, WareMenu.REPAIR_Y - 22);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, WareMenu.REPAIR_Y - 12);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        layoutSlots();
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, WareMenu.IMAGE_WIDTH, WareMenu.IMAGE_HEIGHT);
        if(menu.getWagon() != null)
            SeatButtons.drawHealth(graphics, font, menu.getWagon(),
                    leftPos + WareMenu.HEALTH_X, topPos + WareMenu.HEALTH_Y, WareMenu.HEALTH_W);
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

    private void refreshTabs(boolean tellServer) {
        WareWagon wagon = menu.getWagon();
        if(tellServer && menu.tab() > 0 && (wagon == null || !wagon.hasChest(menu.tab() - 1))) {
            menu.setTab(0);
            PacketDistributor.sendToServer(new SelectWareTabPayload(0));
        }
        for(WareTabButton button : tabs) {
            button.active = button.tab() == 0 || (wagon != null && wagon.hasChest(button.tab() - 1));
            button.setSelected(menu.tab() == button.tab());
        }
    }

    private void layoutSlots() {
        int cargo = 0;
        for(int index = menu.cargoStart(); index < menu.contentStart(); index++) {
            Slot slot = menu.slots.get(index);
            if(menu.tab() == 0)
                moveSlot(slot, WareMenu.gridX(cargo % WareMenu.GRID_COLUMNS), WareMenu.gridY(cargo / WareMenu.GRID_COLUMNS));
            else
                moveSlot(slot, -2000, -2000);
            cargo++;
        }
        for(int index = menu.contentStart(); index < menu.playerStart(); index++) {
            int relative = index - menu.contentStart();
            int chest = relative / WareWagon.CONTENTS;
            int offset = relative % WareWagon.CONTENTS;
            Slot slot = menu.slots.get(index);
            if(menu.tab() == chest + 1)
                moveSlot(slot, WareMenu.gridX(offset % WareMenu.GRID_COLUMNS), WareMenu.gridY(offset / WareMenu.GRID_COLUMNS));
            else
                moveSlot(slot, -2000, -2000);
        }
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

}
