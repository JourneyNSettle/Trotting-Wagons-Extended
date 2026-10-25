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
        super(x, y, 26, 26, Component.empty(), button -> {
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
        if(tab != 0)
            graphics.renderItem(CHEST, getX() + 5, getY() + 5);
        if(!active)
            graphics.fill(getX() + 2, getY() + 2, getX() + 24, getY() + 23, 0x88000000);
        if(!selected)
            return;
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + width, y + 1, 0xFFFFFFA0);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFA0);
        graphics.fill(x, y, x + 1, y + height, 0xFFFFFFA0);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFA0);
    }

}
