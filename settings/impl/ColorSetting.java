package minecraft.rolest.modules.settings.impl;


import java.util.function.Supplier;

import minecraft.rolest.modules.settings.Setting;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }
    @Override
    public ColorSetting setVisible(Supplier<Boolean> bool) {
        return (ColorSetting) super.setVisible(bool);
    }
    public int getAlpha() {
        // Извлекаем alpha-канал (старший байт) из значения цвета
        // getValue() возвращает Integer в формате ARGB
        return (get() >> 24) & 0xFF; // Сдвигаем на 24 бита вправо и маскируем, чтобы получить значение 0–255
    }

    public void setAlpha(int alpha) {
        // Убеждаемся, что alpha в диапазоне 0–255
        alpha = Math.max(0, Math.min(255, alpha));

        // Получаем текущее значение цвета
        int currentColor = getAlpha();

        // Обнуляем текущий alpha-канал (старший байт), сохраняя RGB
        int rgb = currentColor & 0x00FFFFFF; // Маскируем, чтобы оставить только RGB (обнуляем старший байт)

        // Устанавливаем новый alpha-канал
        int newColor = (alpha << 24) | rgb; // Сдвигаем alpha на 24 бита влево и объединяем с RGB

        // Устанавливаем новое значение цвета
        set(newColor);
    }
}
