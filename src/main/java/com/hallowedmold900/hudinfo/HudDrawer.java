package com.hallowedmold900.hudinfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Общая отрисовка элемента: используется и HUD-ом, и редактором. */
public final class HudDrawer {
    private HudDrawer() {}

    public static final int PAD = 3;

    public record Box(int x, int y, int w, int h) {
        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    /** Считает прямоугольник элемента и прижимает его к границам экрана. */
    public static Box measure(Font font, ModConfig.Element c, String text, int screenW, int screenH) {
        int w = Math.round((font.width(text) + PAD * 2) * c.scale);
        int h = Math.round((font.lineHeight + PAD * 2) * c.scale);
        int x = Math.max(0, Math.min(c.x, screenW - w));
        int y = Math.max(0, Math.min(c.y, screenH - h));
        return new Box(x, y, w, h);
    }

    public static void draw(GuiGraphicsExtractor g, Font font, ModConfig.Element c, String text, Box b, int textAlpha) {
        int bgAlpha = Math.round(c.bgOpacity * 255f);
        if (bgAlpha > 0) {
            g.fill(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), bgAlpha << 24);
        }
        var pose = g.pose(); // 2D-стек матриц (Matrix3x2fStack)
        pose.pushMatrix();
        pose.translate(b.x() + PAD * c.scale, b.y() + PAD * c.scale);
        pose.scale(c.scale, c.scale);
        g.text(font, text, 0, 0, (textAlpha << 24) | (c.color & 0xFFFFFF), true);
        pose.popMatrix();
    }
}
