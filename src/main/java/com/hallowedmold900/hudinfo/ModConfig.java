package com.hallowedmold900.hudinfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Настройки мода: один JSON-файл config/hudinfo.json. */
public final class ModConfig {

    /** Настройки одного элемента. */
    public static final class Element {
        public boolean enabled = true;
        public int x;
        public int y;
        public float scale = 1.0f;      // размер текста 0.5 .. 3.0
        public int color = 0xFFFFFF;    // RGB
        public float bgOpacity = 0.4f;  // 0.0 .. 1.0
    }

    public Map<String, Element> elements = new LinkedHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("hudinfo.json");
    }

    public static ModConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        ModConfig loaded = null;
        try {
            Path p = path();
            if (Files.exists(p)) {
                loaded = GSON.fromJson(Files.readString(p), ModConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[hudinfo] Не удалось прочитать конфиг, используются значения по умолчанию: " + e);
        }
        instance = loaded != null ? loaded : new ModConfig();
        if (instance.elements == null) instance.elements = new LinkedHashMap<>();
        for (ElementType t : ElementType.values()) {
            Element e = instance.elements.computeIfAbsent(t.id, k -> defaultFor(t));
            e.scale = Math.max(0.5f, Math.min(3.0f, e.scale));
            e.bgOpacity = Math.max(0f, Math.min(1f, e.bgOpacity));
        }
    }

    public static void save() {
        if (instance == null) return;
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[hudinfo] Не удалось сохранить конфиг: " + e);
        }
    }

    public static Element defaultFor(ElementType t) {
        Element e = new Element();
        e.x = t.defX;
        e.y = t.defY;
        return e;
    }

    public Element of(ElementType t) {
        return elements.computeIfAbsent(t.id, k -> defaultFor(t));
    }
}
