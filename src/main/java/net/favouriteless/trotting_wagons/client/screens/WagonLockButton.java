package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.network.packets.ToggleWagonLockPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WagonLockButton extends Button {

    private final AbstractWagon wagon;

    private final boolean painted;

    public WagonLockButton(int x, int y, AbstractWagon wagon) {
        this(x, y, 16, wagon, false);
    }

    public WagonLockButton(int x, int y, int size, AbstractWagon wagon) {
        this(x, y, size, wagon, true);
    }

    private WagonLockButton(int x, int y, int size, AbstractWagon wagon, boolean painted) {
        super(x, y, size, size, Component.empty(), button -> PacketDistributor.sendToServer(new ToggleWagonLockPayload(wagon.getId())), DEFAULT_NARRATION);
        this.wagon = wagon;
        this.painted = painted;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setTooltip(Tooltip.create(Component.translatable(wagon.isLocked() ? "gui.trotting_wagons.unlock" : "gui.trotting_wagons.lock")));
        if(painted)
            return;
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + 16, y + 16, isHovered() ? 0xFF9A9A9A : 0xFF8B8B8B);
        graphics.fill(x, y, x + 16, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + 16, 0xFFFFFFFF);
        graphics.fill(x, y + 15, x + 16, y + 16, 0xFF373737);
        graphics.fill(x + 15, y, x + 16, y + 16, 0xFF373737);
        drawLock(graphics, x, y, wagon.isLocked());
    }

    private static void drawLock(GuiGraphics graphics, int x, int y, boolean locked) {
        int shackle = locked ? 3 : 2;
        graphics.fill(x + 5, y + shackle, x + 11, y + shackle + 2, 0xFF373737);
        graphics.fill(x + 5, y + shackle, x + 7, y + 8, 0xFF373737);
        graphics.fill(x + 9, y + shackle, x + 11, y + 8, 0xFF373737);
        graphics.fill(x + 4, y + 7, x + 12, y + 14, 0xFF555555);
        graphics.fill(x + 7, y + 9, x + 9, y + 12, 0xFFC6C6C6);
    }

}
