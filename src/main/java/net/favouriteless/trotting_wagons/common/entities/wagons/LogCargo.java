package net.favouriteless.trotting_wagons.common.entities.wagons;

/** Cargo limit for the log wagon. The TFC compat mod lowers it at startup. */
public final class LogCargo {

    /** Without the compat mod. That mod sets a quarter of this. */
    public static final int CAPACITY = 1760 * 4;
    private static int limit = CAPACITY;

    private LogCargo() {}

    public static int limit() {
        return limit;
    }

    public static void setLimit(int capacity) {
        limit = Math.max(1, capacity);
    }

}
