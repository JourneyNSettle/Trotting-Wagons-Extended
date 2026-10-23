package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.wagons.WareWagon;
import net.favouriteless.trotting_wagons.common.menus.RepairSlot;
import net.favouriteless.trotting_wagons.common.menus.WareMenu;
import net.favouriteless.trotting_wagons.common.network.packets.SelectWareTabPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WareScreen extends AbstractContainerScreen<WareMenu> {

    private final List<WareTabButton> tabs = new ArrayList<>();

    public WareScreen(WareMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WareMenu.IMAGE_WIDTH;
        this.imageHeight = WareMenu.IMAGE_HEIGHT;
        this.titleLabelY = 4;
        this.inventoryLabelX = WareMenu.INV_X;
        this.inventoryLabelY = WareMenu.INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        tabs.clear();
        for(int tab = 0; tab <= WareWagon.CHESTS; tab++) {
            WareTabButton button = new WareTabButton(leftPos + WareMenu.TAB_X + tab * 24, topPos + WareMenu.TAB_Y, tab, menu);
            tabs.add(button);
            addRenderableWidget(button);
        }
        WareWagon wagon = menu.getWagon();
        if(wagon != null && minecraft != null && minecraft.player != null && wagon.isOwnedBy(minecraft.player))
            addRenderableWidget(new WagonLockButton(leftPos + 6, topPos + imageHeight - 22, wagon));
        refreshTabs(false);
        layoutSlots();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshTabs(true);
        layoutSlots();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        WagonGui.drawHeader(graphics, font, menu.getWagon(), titleLabelY, imageWidth);
        WareWagon wagon = menu.getWagon();
        if(wagon == null)
            return;
        int slotsCenter = WareMenu.REPAIR_X + 17;
        drawCentered(graphics, Component.translatable("gui.trotting_wagons.durability"), slotsCenter, WareMenu.REPAIR_Y - 23);
        drawCentered(graphics, wagon.conditionName(), slotsCenter, WareMenu.REPAIR_Y - 13);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        layoutSlots();
        WagonGui.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight);
        if(menu.getWagon() != null)
            SeatButtons.drawHealth(graphics, font, menu.getWagon(), leftPos + 16, topPos + 14, imageWidth - 32);
        for(Slot slot : menu.slots) {
            if(!slot.isActive())
                continue;
            WagonGui.drawSlot(graphics, leftPos, topPos, slot);
        }
        WagonGui.drawWheelCaption(graphics, font, menu, menu.getWagon(), leftPos, topPos);
        WagonGui.drawEmptySlotIcons(graphics, leftPos, topPos, menu);
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
                moveSlot(slot, WareMenu.GRID_X + (cargo % 9) * 18, WareMenu.GRID_Y + (cargo / 9) * 18);
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
                moveSlot(slot, WareMenu.GRID_X + (offset % 9) * 18, WareMenu.GRID_Y + (offset / 9) * 18);
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
