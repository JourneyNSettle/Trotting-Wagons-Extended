package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.TrottingWagons;
import net.favouriteless.trotting_wagons.common.menus.RoyalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RoyalScreen extends AbstractWagonScreen<RoyalMenu> {

    private static final ResourceLocation TEXTURE = TrottingWagons.id("textures/gui/royal_wagon.png");

    public RoyalScreen(RoyalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, 256, 256);
        this.imageWidth = 320;
        this.imageHeight = 275;
        this.inventoryLabelX = 80;
        this.inventoryLabelY = 180;
    }

    @Override
    protected int seatBarY() {
        return 133;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WagonGui.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 17, 7, 17, 162, 108, 256, 256);
        drawSeatStrip(graphics, leftPos, imageWidth);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 192, 7, 139, 162, 76, 256, 256);
        WagonGui.drawWheelCaption(graphics, font, menu, menu.getWagon(), leftPos, topPos);
    }

}
