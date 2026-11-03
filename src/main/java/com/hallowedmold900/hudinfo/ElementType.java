package com.hallowedmold900.hudinfo;

import net.minecraft.network.chat.Component;

/** Все элементы HUD. Порядок = порядок отрисовки и стартовое положение. */
public enum ElementType {
    FPS("fps", 6, 6),
    PING("ping", 6, 22),
    BIOME("biome", 6, 38),
    DAY("day", 6, 54),
    COORDS("coords", 6, 70);

    public final String id;
    public final int defX;
    public final int defY;

    ElementType(String id, int defX, int defY) {
        this.id = id;
        this.defX = defX;
        this.defY = defY;
    }

    public Component title() {
        return Component.translatable("hudinfo.element." + id);
    }
}
