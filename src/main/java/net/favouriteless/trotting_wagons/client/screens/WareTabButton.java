package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.menus.WareMenu;
import net.favouriteless.trotting_wagons.common.network.packets.SelectWareTabPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public class WareTabButton extends Button {

    private static final ItemStack CHEST = new ItemStack(Items.CHEST);

    private final int tab;
    private boolean selected;

    public WareTabButton(int x, int y, int tab, WareMenu menu) {
        super(x, y, 20, 20, Component.empty(), button -> {
            menu.setTab(tab);
            PacketDistributor.sendToServer(new SelectWareTabPayload(menu.tab()));
        }, DEFAULT_NARRATION);
        this.tab = tab;
        setTooltip(Tooltip.create(tab == 0
                ? Component.translatable("gui.trotting_wagons.ware.cargo")
                : Component.translatable("gui.trotting_wagons.ware.chest", tab)));
    }

    public int tab() {
        return tab;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if(tab == 0) {
            int x = getX() + 6;
            int y = getY() + 6;
            graphics.fill(x, y, x + 8, y + 8, 0xFF8B5A2B);
            graphics.fill(x, y, x + 8, y + 2, 0xFFC4A574);
        } else {
            graphics.renderItem(CHEST, getX() + 2, getY() + 2);
        }
        if(!selected)
            return;
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFFFFFFA0);
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFFFFFFA0);
        graphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFFFFFFA0);
        graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFFFFFFA0);
    }

}
