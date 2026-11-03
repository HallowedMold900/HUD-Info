package com.hallowedmold900.hudinfo;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class HudInfoMod implements ClientModInitializer {
    public static final String MOD_ID = "hudinfo";

    /**
     * Клавиша по умолчанию — K. Код берём из InputConstants.KEY_K, потому что в 26.3
     * (SDL) коды клавиш не совпадают с GLFW. Запасной вариант — GLFW-код 75.
     */
    private static int defaultKey() {
        try {
            return InputConstants.class.getField("KEY_K").getInt(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 75;
        }
    }

    private static KeyMapping openEditorKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openEditorKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.hudinfo.open", defaultKey(), category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.consumeClick()) {
                client.gui.setScreen(new HudEditorScreen());
            }
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "info"),
                HudInfoMod::renderHud);
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        HudData.frame();
        // Пока открыт редактор, элементы рисует он сам (иначе фон удвоится)
        if (HudEditorScreen.isOpen()) return;

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        ModConfig cfg = ModConfig.get();

        for (ElementType type : ElementType.values()) {
            ModConfig.Element c = cfg.of(type);
            if (!c.enabled) continue;
            String text = HudData.text(type);
            HudDrawer.Box box = HudDrawer.measure(mc.font, c, text, sw, sh);
            HudDrawer.draw(graphics, mc.font, c, text, box, 255);
        }
    }
}
