package com.arpatsche.coloniexaerocompat.minimap;

import com.arpatsche.coloniexaerocompat.ColonyXaeroCompat;

import java.lang.reflect.Field;

/**
 * The minimap moves each element to its rotated position, but does not rotate or scale what we draw.
 * To draw a chunk square that turns with the minimap we need its rotation and zoom, which the handler
 * keeps in private fields (ps = sine, pc = cosine of the rotation, zoom = pixels per block).
 * If they cannot be read (for example after a Xaero update), only the claim areas on the minimap
 * are skipped; the rest of the game is not affected.
 */
final class MinimapTransform {

    private static volatile Object handler;
    private static Field psField;
    private static Field pcField;
    private static Field zoomField;
    private static boolean failed = false;

    private MinimapTransform() {
    }

    static void setHandler(Object mapHandler) {
        handler = mapHandler;
        psField = null;
        pcField = null;
        zoomField = null;
        failed = false;
    }

    /** Reads the current rotation and zoom into the context. Returns false if that is not possible. */
    static boolean read(MinimapContext context) {
        Object target = handler;
        if (failed || target == null) {
            return false;
        }
        try {
            if (psField == null) {
                psField = find(target.getClass(), "ps");
                pcField = find(target.getClass(), "pc");
                zoomField = find(target.getClass(), "zoom");
            }
            double ps = psField.getDouble(target);
            double pc = pcField.getDouble(target);
            // handler: x' = ps * x - pc * z; y' = pc * x + ps * z -> a rotation with cos = ps, sin = pc
            context.angle = Math.atan2(pc, ps);
            context.zoom = zoomField.getDouble(target);
            return context.zoom > 0;
        } catch (Exception e) {
            failed = true;
            ColonyXaeroCompat.LOGGER.warn("Could not read the minimap rotation; claim areas will not be shown on the minimap", e);
            return false;
        }
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // try the parent class
            }
        }
        throw new NoSuchFieldException(name);
    }
}
