package net.favouriteless.trotting_wagons.client.screens;

import net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon;
import net.favouriteless.trotting_wagons.common.entities.base.WagonSeat;
import net.favouriteless.trotting_wagons.common.network.packets.EjectSeatPayload;
import net.favouriteless.trotting_wagons.common.network.packets.MoveAnimalPayload;
import net.favouriteless.trotting_wagons.common.network.packets.SelectSeatPayload;
import net.favouriteless.trotting_wagons.common.network.packets.ToggleSeatLockPayload;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class SeatButton extends AbstractWidget {

    public static final int SIZE = 24;
    private static final long TOOLTIP_DELAY_MS = 1000L;

    private static final List<SeatButton> OPEN = new ArrayList<>();
    private static SeatButton dragSource;
    private static boolean dragLeftSource;

    private final AbstractWagon wagon;
    private final int seat;
    private long hoverStart = -1L;
    private Component tooltip = Component.empty();

    public static void bind(List<SeatButton> buttons) {
        OPEN.clear();
        OPEN.addAll(buttons);
        dragSource = null;
        dragLeftSource = false;
    }

    public static boolean press(double mouseX, double mouseY, int button) {
        if(button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            return false;
        SeatButton hit = at(mouseX, mouseY);
        if(hit == null)
            return false;
        WagonSeat.Kind seatKind = hit.wagon.getSeat(hit.seat).kind();
        boolean lockable = hit.wagon.getSeat(hit.seat).canLockAnimals();
        if(Screen.hasShiftDown() && lockable) {
            PacketDistributor.sendToServer(new ToggleSeatLockPayload(hit.wagon.getId(), hit.seat));
            return true;
        }
        if(hit.occupant().kind() == Kind.ANIMAL) {
            dragSource = hit;
            dragLeftSource = false;
            return true;
        }
        return false;
    }

    public static void markDrag(double mouseX, double mouseY) {
        if(dragSource != null && !dragSource.contains(mouseX, mouseY))
            dragLeftSource = true;
    }

    public static boolean finishDrag(double mouseX, double mouseY) {
        SeatButton source = dragSource;
        if(source == null)
            return false;
        SeatButton target = at(mouseX, mouseY);
        boolean move = target != null && target != source && target.accepts(source);
        boolean eject = !move && !dragLeftSource && source.contains(mouseX, mouseY);
        int animalId = source.wagon.getSeatEntityId(source.seat);
        dragSource = null;
        dragLeftSource = false;
        if(move && animalId >= 0)
            PacketDistributor.sendToServer(new MoveAnimalPayload(source.wagon.getId(), animalId, target.seat));
        else if(eject)
            PacketDistributor.sendToServer(new EjectSeatPayload(source.wagon.getId(), source.seat));
        return true;
    }

    public static void renderCarried(GuiGraphics graphics, int mouseX, int mouseY) {
        if(dragSource == null)
            return;
        int id = dragSource.wagon.getSeatEntityId(dragSource.seat);
        if(!(dragSource.wagon.level().getEntity(id) instanceof LivingEntity living) || living instanceof Player)
            return;
        int size = 32;
        int x1 = mouseX - size / 2;
        int y1 = mouseY - size / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, x1, y1, x1 + size, y1 + size, 14, 0.15F, mouseX, mouseY, living
        );
    }

    private static SeatButton at(double mouseX, double mouseY) {
        for(SeatButton button : OPEN) {
            if(button.contains(mouseX, mouseY))
                return button;
        }
        return null;
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
    }

    public SeatButton(AbstractWagon wagon, int seat, int x, int y) {
        super(x, y, SIZE, SIZE, Component.literal(Integer.toString(seat + 1)));
        this.wagon = wagon;
        this.seat = seat;
    }

    public void refresh() {
        Occupant occupant = occupant();
        active = switch(occupant.kind()) {
            case EMPTY_PLAYER, ANIMAL, LOCKABLE -> true;
            case PLAYER, SELF, UNKNOWN -> false;
        };
        tooltip = occupant.tooltip();
    }

    public Component delayedTooltip() {
        if(!isHovered() || hoverStart < 0 || Util.getMillis() - hoverStart < TOOLTIP_DELAY_MS)
            return null;
        return tooltip;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if(isHovered()) {
            if(hoverStart < 0)
                hoverStart = Util.getMillis();
        } else {
            hoverStart = -1L;
        }

        boolean locked = wagon.isSeatLocked(seat);
        int color = locked ? 0xFF6E3A3A : active ? 0xFF8B8B8B : 0xFF5A5A5A;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF373737);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, color);
        if(dragSource != null && dragSource != this && accepts(dragSource))
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0x8800C853);

        Occupant occupant = occupant();
        boolean carried = dragSource == this;
        if(occupant.entity() instanceof Player player)
            drawPlayer(graphics, player);
        else if(!carried && occupant.entity() instanceof LivingEntity living)
            drawAnimal(graphics, living);
        else if(occupant.entity() == null && occupant.kind() == Kind.UNKNOWN)
            drawFitted(graphics, Component.literal("?"), 18);
        else if(carried || occupant.kind() == Kind.EMPTY_PLAYER || occupant.kind() == Kind.LOCKABLE)
            drawFitted(graphics, typeLabel(wagon.getSeat(seat)), 16);

        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + 4, 0xFFFFFF);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!active || !visible || !isMouseOver(mouseX, mouseY) || !isValidClickButton(button))
            return false;

        boolean lockable = wagon.getSeat(seat).canLockAnimals();
        if(Screen.hasShiftDown() && lockable) {
            PacketDistributor.sendToServer(new ToggleSeatLockPayload(wagon.getId(), seat));
            return true;
        }

        Kind kind = occupant().kind();
        if(kind == Kind.ANIMAL)
            return false;
        else if(kind == Kind.EMPTY_PLAYER)
            PacketDistributor.sendToServer(new SelectSeatPayload(wagon.getId(), seat));
        else
            return false;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private Occupant occupant() {
        int id = wagon.getSeatEntityId(seat);
        WagonSeat definition = wagon.getSeat(seat);
        if(id < 0) {
            if(definition.kind() == WagonSeat.Kind.UNIVERSAL)
                return new Occupant(Kind.EMPTY_PLAYER, null, Component.translatable("gui.trotting_wagons.tooltip.move").append("\n").append(lockTooltip()));
            if(definition.kind() == WagonSeat.Kind.ANIMAL || definition.kind() == WagonSeat.Kind.ANIMALBIG)
                return new Occupant(Kind.LOCKABLE, null, lockTooltip());
            return new Occupant(Kind.EMPTY_PLAYER, null, Component.translatable("gui.trotting_wagons.tooltip.move"));
        }

        if(!(wagon.level().getEntity(id) instanceof LivingEntity living))
            return new Occupant(Kind.UNKNOWN, null, Component.translatable("gui.trotting_wagons.tooltip.unknown"));

        if(living instanceof Player player) {
            if(player == Minecraft.getInstance().player)
                return new Occupant(Kind.SELF, player, Component.translatable("gui.trotting_wagons.tooltip.here"));
            return new Occupant(Kind.PLAYER, player, Component.translatable("gui.trotting_wagons.tooltip.occupied", player.getName()));
        }

        Component tip = Component.translatable("gui.trotting_wagons.tooltip.eject");
        if(definition.canLockAnimals())
            tip = tip.copy().append("\n").append(lockTooltip());
        return new Occupant(Kind.ANIMAL, living, tip);
    }

    private boolean accepts(SeatButton source) {
        if(source == null || source.wagon != wagon)
            return false;
        int id = source.wagon.getSeatEntityId(source.seat);
        return wagon.level().getEntity(id) instanceof LivingEntity animal && wagon.canPlaceAnimal(seat, animal);
    }

    private Component lockTooltip() {
        String key = wagon.isSeatLocked(seat) ? "gui.trotting_wagons.tooltip.unlock" : "gui.trotting_wagons.tooltip.lock";
        return Component.translatable(key);
    }

    private static Component typeLabel(WagonSeat seat) {
        return switch(seat.kind()) {
            case DRIVER -> Component.translatable("gui.trotting_wagons.seat.driver");
            case PLAYER -> Component.translatable("gui.trotting_wagons.seat.player");
            case UNIVERSAL -> Component.translatable("gui.trotting_wagons.seat.universal");
            case ANIMAL -> Component.translatable("gui.trotting_wagons.seat.animal_small");
            case ANIMALBIG -> Component.translatable("gui.trotting_wagons.seat.animal_large");
        };
    }

    private void drawPlayer(GuiGraphics graphics, Player player) {
        if(player instanceof AbstractClientPlayer clientPlayer) {
            PlayerFaceRenderer.draw(graphics, clientPlayer.getSkin().texture(), getX() + 5, getY() + 8, 14);
            return;
        }
        drawFitted(graphics, player.getName(), 12);
    }

    private void drawAnimal(GuiGraphics graphics, LivingEntity living) {
        graphics.enableScissor(getX() + 2, getY() + 10, getX() + width - 2, getY() + height - 2);
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                getX() + 3, getY() + 9, getX() + width - 3, getY() + height - 2,
                10, 0.35F,
                getX() + width / 2F, getY() + height / 2F,
                living
        );
        graphics.disableScissor();
    }

    private void drawFitted(GuiGraphics graphics, Component text, int y) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(getX() + width / 2F, getY() + y, 0);
        pose.scale(0.5F, 0.5F, 1F);
        graphics.drawCenteredString(Minecraft.getInstance().font, text, 0, 0, 0xFFFFFF);
        pose.popPose();
    }

    private enum Kind {
        EMPTY_PLAYER,
        LOCKABLE,
        ANIMAL,
        PLAYER,
        SELF,
        UNKNOWN
    }

    private record Occupant(Kind kind, LivingEntity entity, Component tooltip) {}

}
