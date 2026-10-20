package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.wagons.DebugWagon;
import net.favouriteless.trotting_wagons.common.network.packets.ToggleLadderPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;

public class LadderButton extends Button {

    private final DebugWagon wagon;

    public LadderButton(int x, int y, DebugWagon wagon) {
        super(x, y, 16, 16, Component.empty(), button -> PacketDistributor.sendToServer(new ToggleLadderPayload(wagon.getId())), DEFAULT_NARRATION);
        this.wagon = wagon;
        setTooltipDelay(Duration.ofMillis(TooltipDelay.MILLIS));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setTooltip(Tooltip.create(Component.translatable(wagon.isLadderOpen() ? "gui.trotting_wagons.ladder.raise" : "gui.trotting_wagons.ladder.lower")));
        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + 16, y + 16, isHovered() ? 0xFF9A9A9A : 0xFF8B8B8B);
        graphics.fill(x, y, x + 16, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + 16, 0xFFFFFFFF);
        graphics.fill(x, y + 15, x + 16, y + 16, 0xFF373737);
        graphics.fill(x + 15, y, x + 16, y + 16, 0xFF373737);
        int rail = wagon.isLadderOpen() ? 0xFF6B4A2A : 0xFF373737;
        graphics.fill(x + 4, y + 3, x + 6, y + 13, rail);
        graphics.fill(x + 10, y + 3, x + 12, y + 13, rail);
        graphics.fill(x + 4, y + 5, x + 12, y + 6, rail);
        graphics.fill(x + 4, y + 8, x + 12, y + 9, rail);
        graphics.fill(x + 4, y + 11, x + 12, y + 12, rail);
    }

}
