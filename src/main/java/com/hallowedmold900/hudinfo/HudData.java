package com.hallowedmold900.hudinfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;

import java.lang.reflect.Method;
import java.util.Locale;

/** Источники данных для текста каждого элемента. */
public final class HudData {
    private HudData() {}

    // ---------- свой счётчик FPS (не зависит от API Minecraft) ----------
    private static int frames;
    private static int fps;
    private static long lastNanos = System.nanoTime();

    /** Вызывать один раз за кадр. */
    public static void frame() {
        frames++;
        long now = System.nanoTime();
        long dt = now - lastNanos;
        if (dt >= 500_000_000L) {
            fps = (int) Math.round(frames * 1_000_000_000.0 / dt);
            frames = 0;
            lastNanos = now;
        }
    }

    // ---------- тексты ----------
    public static String text(ElementType type) {
        Minecraft mc = Minecraft.getInstance();
        return switch (type) {
            case FPS -> tr("hudinfo.text.fps", fps);
            case PING -> tr("hudinfo.text.ping", ping(mc));
            case BIOME -> tr("hudinfo.text.biome", biome(mc));
            case DAY -> tr("hudinfo.text.day", day(mc));
            case COORDS -> coords(mc);
        };
    }

    private static String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private static String ping(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return "—";
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? "—" : String.valueOf(info.getLatency());
    }

    private static String biome(Minecraft mc) {
        if (mc.level == null || mc.player == null) return "—";
        Holder<Biome> holder = mc.level.getBiome(mc.player.blockPosition());
        String name = holder.getRegisteredName(); // например "minecraft:plains"
        int i = name.indexOf(':');
        String ns = i < 0 ? "minecraft" : name.substring(0, i);
        String path = (i < 0 ? name : name.substring(i + 1)).replace('/', '.');
        return Component.translatable("biome." + ns + "." + path).getString();
    }

    private static String coords(Minecraft mc) {
        if (mc.player == null) return tr("hudinfo.text.coords", "—", "—", "—");
        return tr("hudinfo.text.coords",
                String.format(Locale.ROOT, "%.1f", mc.player.getX()),
                String.format(Locale.ROOT, "%.1f", mc.player.getY()),
                String.format(Locale.ROOT, "%.1f", mc.player.getZ()));
    }

    // ---------- день ----------
    // В 26.x время мира переехало на "часы" (clocks). Чтобы не зависеть от точного имени метода,
    // пробуем getDayTime() через рефлексию (имена в 26.x не обфусцированы), иначе берём gameTime.
    private static Method dayTimeMethod;
    private static boolean dayTimeLookedUp;

    private static String day(Minecraft mc) {
        if (mc.level == null) return "—";
        long t;
        try {
            if (!dayTimeLookedUp) {
                dayTimeLookedUp = true;
                try {
                    dayTimeMethod = mc.level.getClass().getMethod("getDayTime");
                } catch (NoSuchMethodException ignored) {
                    dayTimeMethod = null;
                }
            }
            t = dayTimeMethod != null ? ((Number) dayTimeMethod.invoke(mc.level)).longValue() : mc.level.getGameTime();
        } catch (ReflectiveOperationException e) {
            t = mc.level.getGameTime();
        }
        return String.valueOf(Math.floorDiv(t, 24000L) + 1);
    }
}
