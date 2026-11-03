package com.hallowedmold900.hudinfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Редактор HUD: перетаскивание мышкой + панель настроек выбранного элемента
 * (вкл/выкл, размер текста, прозрачность фона, цвет RGB).
 */
public class HudEditorScreen extends Screen {
    private static final int ROW = 24;
    private static final int COL_W = 150;
    private static final int GAP = 6;

    private static boolean open;

    public static boolean isOpen() {
        return open;
    }

    private ElementType selected;
    private ElementType dragging;
    private int dragDx;
    private int dragDy;
    /** true = показывать отладочную строку в редакторе */
    private static final boolean DEBUG = false;
    private boolean wasDown;
    private int clicks;
    private int evClick;
    private int evRelease;
    private int evKey;
    private int evButton = -1;
    private boolean rightDown;
    private boolean midDown;
    private int lastMouseX;
    private int lastMouseY;

    public HudEditorScreen() {
        super(Component.translatable("hudinfo.screen.title"));
    }

    // ------------------------------------------------------------------ виджеты

    @Override
    protected void init() {
        open = true;
        int cx = this.width / 2;
        int bottomY = this.height - 28;
        Component doneLabel = Component.translatable("hudinfo.ui.done");

        if (selected == null) {
            addRenderableWidget(Button.builder(doneLabel, b -> onClose())
                    .bounds(cx - 75, bottomY, 150, 20).build());
            return;
        }

        final ModConfig.Element c = ModConfig.get().of(selected);
        final ElementType sel = selected;
        int leftX = cx - COL_W - GAP / 2;
        int rightX = cx + GAP / 2;
        int y1 = bottomY - ROW * 3;
        int y2 = y1 + ROW;
        int y3 = y2 + ROW;

        // Левая колонка: вкл/выкл, размер, фон
        addRenderableWidget(Button.builder(toggleLabel(c), b -> {
            c.enabled = !c.enabled;
            b.setMessage(toggleLabel(c));
        }).bounds(leftX, y1, COL_W, 20).build());

        addRenderableWidget(new ValueSlider(leftX, y2, COL_W, 20, (c.scale - 0.5) / 2.5,
                v -> Component.translatable("hudinfo.ui.size", fmtScale(snapScale(v))),
                v -> c.scale = snapScale(v)));

        addRenderableWidget(new ValueSlider(leftX, y3, COL_W, 20, c.bgOpacity,
                v -> Component.translatable("hudinfo.ui.background", Math.round(v * 100)),
                v -> c.bgOpacity = (float) (double) v));

        // Правая колонка: цвет R, G, B
        addRenderableWidget(channelSlider(rightX, y1, "R", 16, c));
        addRenderableWidget(channelSlider(rightX, y2, "G", 8, c));
        addRenderableWidget(channelSlider(rightX, y3, "B", 0, c));

        // Нижний ряд: сброс и готово
        addRenderableWidget(Button.builder(Component.translatable("hudinfo.ui.reset"), b -> {
            ModConfig.get().elements.put(sel.id, ModConfig.defaultFor(sel));
            rebuildWidgets();
        }).bounds(leftX, bottomY, COL_W, 20).build());

        addRenderableWidget(Button.builder(doneLabel, b -> onClose())
                .bounds(rightX, bottomY, COL_W, 20).build());
    }

    private static ValueSlider channelSlider(int x, int y, String name, int shift, ModConfig.Element c) {
        return new ValueSlider(x, y, COL_W, 20, ((c.color >> shift) & 0xFF) / 255.0,
                v -> Component.literal(name + ": " + Math.round(v * 255)),
                v -> {
                    int ch = (int) Math.round(v * 255);
                    c.color = (c.color & ~(0xFF << shift)) | (ch << shift);
                });
    }

    private static Component toggleLabel(ModConfig.Element c) {
        return Component.translatable(c.enabled ? "hudinfo.ui.enabled" : "hudinfo.ui.disabled");
    }

    private static float snapScale(double v) {
        return Math.round((0.5 + v * 2.5) * 10.0) / 10.0f;
    }

    private static String fmtScale(float s) {
        return String.format(Locale.ROOT, "%.1f", s);
    }

    // ------------------------------------------------------------------ отрисовка

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        HudData.frame();
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        ModConfig cfg = ModConfig.get();

        // перетаскивание считаем по кадрам, по позиции мыши
        if (dragging != null) {
            ModConfig.Element dc = cfg.of(dragging);
            HudDrawer.Box db = HudDrawer.measure(this.font, dc, HudData.text(dragging), this.width, this.height);
            dc.x = Math.max(0, Math.min(mouseX - dragDx, this.width - db.w()));
            dc.y = Math.max(0, Math.min(mouseY - dragDy, this.height - db.h()));
        }
        for (ElementType t : ElementType.values()) {
            ModConfig.Element c = cfg.of(t);
            String text = HudData.text(t);
            HudDrawer.Box b = HudDrawer.measure(this.font, c, text, this.width, this.height);
            HudDrawer.draw(g, this.font, c, text, b, c.enabled ? 255 : 100);

            int outline = (t == selected) ? 0xFFFFFF55 : (b.contains(mouseX, mouseY) ? 0xFFFFFFFF : 0x80FFFFFF);
            outline(g, b, outline);
        }

        if (DEBUG) drawDebug(g);

        int bottomY = this.height - 28;
        if (selected != null) {
            ModConfig.Element c = cfg.of(selected);
            int titleY = bottomY - ROW * 3 - 14;
            int cx = this.width / 2;
            String title = selected.title().getString();
            g.text(this.font, title, cx - COL_W - GAP / 2, titleY, 0xFFFFFFFF, true);
            // образец цвета
            int sx = cx + GAP / 2;
            g.fill(sx, titleY - 1, sx + 20, titleY + 9, 0xFFFFFFFF);
            g.fill(sx + 1, titleY, sx + 19, titleY + 8, 0xFF000000 | (c.color & 0xFFFFFF));
        } else {
            String hint = Component.translatable("hudinfo.ui.hint").getString();
            g.text(this.font, hint, (this.width - this.font.width(hint)) / 2, bottomY - 16, 0xFFFFFFFF, true);
        }
    }

    private void drawDebug(GuiGraphicsExtractor g) {
        String l1 = "dbg mouse=" + lastMouseX + "," + lastMouseY + " L=" + wasDown + " R=" + rightDown + " M=" + midDown
                + " polledClicks=" + clicks + " sel=" + selected + " drag=" + dragging;
        String l2 = "dbg events: click=" + evClick + " (btn " + evButton + ") release=" + evRelease
                + " key=" + evKey + " widgets=" + this.children().size();
        g.text(this.font, l1, 4, this.height - 20, 0xFFFFFF55, true);
        g.text(this.font, l2, 4, this.height - 10, 0xFFFFFF55, true);
    }

    private static void outline(GuiGraphicsExtractor g, HudDrawer.Box b, int color) {
        g.fill(b.x(), b.y(), b.x() + b.w(), b.y() + 1, color);
        g.fill(b.x(), b.y() + b.h() - 1, b.x() + b.w(), b.y() + b.h(), color);
        g.fill(b.x(), b.y(), b.x() + 1, b.y() + b.h(), color);
        g.fill(b.x() + b.w() - 1, b.y(), b.x() + b.w(), b.y() + b.h(), color);
    }

    // ------------------------------------------------------------------ мышь

    private void onPress(int mx, int my) {
        clicks++;
        // клик по виджету панели — обрабатывают сами виджеты
        for (var child : this.children()) {
            if (child instanceof AbstractWidget w && w.isMouseOver(mx, my)) return;
        }
        ModConfig cfg = ModConfig.get();
        ElementType[] types = ElementType.values();
        for (int i = types.length - 1; i >= 0; i--) {
            ElementType t = types[i];
            HudDrawer.Box b = HudDrawer.measure(this.font, cfg.of(t), HudData.text(t), this.width, this.height);
            if (b.contains(mx, my)) {
                dragging = t;
                dragDx = mx - b.x();
                dragDy = my - b.y();
                if (selected != t) {
                    selected = t;
                    rebuildWidgets();
                }
                return;
            }
        }
        if (selected != null) {
            selected = null;
            dragging = null;
            rebuildWidgets();
        }
    }

    private void onRelease() {
        if (dragging != null) {
            dragging = null;
            ModConfig.save();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        evClick++;
        evButton = event.button();
        // Номер кнопки не проверяем: в 26.3 (SDL) левая = 1, в GLFW была 0.
        for (var child : this.children()) {
            if (child instanceof AbstractWidget w && w.isMouseOver(lastMouseX, lastMouseY)) {
                return super.mouseClicked(event, doubleClick);
            }
        }
        onPress(lastMouseX, lastMouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        evRelease++;
        if (dragging != null) {
            onRelease();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        evKey++;
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) return true;
        return super.mouseDragged(event, dx, dy);
    }

    // ------------------------------------------------------------------ закрытие

    @Override
    public void removed() {
        open = false;
        ModConfig.save();
        super.removed();
    }
}
