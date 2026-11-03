package com.hallowedmold900.hudinfo;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/** Простой слайдер 0..1 с подписью и колбэком. */
public final class ValueSlider extends AbstractSliderButton {
    private final Function<Double, Component> label;
    private final Consumer<Double> onChange;

    public ValueSlider(int x, int y, int w, int h, double value,
                       Function<Double, Component> label, Consumer<Double> onChange) {
        super(x, y, w, h, Component.empty(), Math.max(0.0, Math.min(1.0, value)));
        this.label = label;
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (label != null) setMessage(label.apply(this.value));
    }

    @Override
    protected void applyValue() {
        if (onChange != null) onChange.accept(this.value);
    }
}
