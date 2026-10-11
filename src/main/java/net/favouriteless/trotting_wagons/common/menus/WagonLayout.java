package net.favouriteless.trotting_wagons.common.menus;

public final class WagonLayout {

    public static final int SCREEN_WIDTH = 320;
    public static final int DRAFT_STRIDE = 26;

    private WagonLayout() {}

    public static int wheelX(int index) {
        return SCREEN_WIDTH - 16 - 36 + (index % 2) * 18;
    }

    public static int wheelY(int top, int index) {
        return top + (index / 2) * 18;
    }

    public static int draftX(int panelLeft, int index) {
        return panelLeft + 16 + index * DRAFT_STRIDE;
    }

}
