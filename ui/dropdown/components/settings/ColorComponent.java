package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.settings.impl.ColorSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.animations.Direction;
import minecraft.rolest.utils.animations.impl.EaseBackIn;
import minecraft.rolest.utils.projections.SoundPlayer;
import minecraft.rolest.utils.math.Vector4i;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorComponent extends Component {

    final ColorSetting setting;
    private Color initialColor;
    private Color currentColor;

    private enum ThemePreset {
        DARK, LIGHT, NEON, PASTEL, CUSTOM
    }

    private ThemePreset currentTheme = ThemePreset.DARK;
    private Map<ThemePreset, Color> themeColors;
    private float themeTransitionProgress = 1.0f;
    private EaseBackIn themeAnimation = new EaseBackIn(500, 1, 1.5f);

    float colorRectX, colorRectY, colorRectWidth, colorRectHeight;
    float pickerX, pickerY, pickerWidth, pickerHeight;
    float sliderX, sliderY, sliderWidth, sliderHeight;
    float themeButtonX, themeButtonY, themeButtonWidth;

    final float padding = 5.5f;
    float textX, textY;
    private float[] hsb = new float[3];

    boolean panelOpened;
    boolean draggingHue, draggingPicker, draggingAlpha;
    boolean showThemeSelector;

    final EaseBackIn animation = new EaseBackIn(300, 1, 1);

    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        this.initialColor = new Color(setting.get());
        this.currentColor = initialColor;
        hsb = Color.RGBtoHSB(ColorUtils.red(setting.get()),
                ColorUtils.green(setting.get()),
                ColorUtils.blue(setting.get()),
                null);
        hsb = new float[]{hsb[0], hsb[1], hsb[2]};
        setHeight(14);
        initializeThemes();
        applyTheme(currentTheme);
    }

    private void initializeThemes() {
        themeColors = new HashMap<>();
        themeColors.put(ThemePreset.DARK, new Color(30, 30, 30));
        themeColors.put(ThemePreset.LIGHT, new Color(240, 240, 240));
        themeColors.put(ThemePreset.NEON, new Color(0, 255, 255));
        themeColors.put(ThemePreset.PASTEL, new Color(180, 200, 255));
        themeColors.put(ThemePreset.CUSTOM, initialColor);
    }

    private void applyTheme(ThemePreset theme) {
        currentTheme = theme;
        themeTransitionProgress = 0.0f;
        themeAnimation.reset();
        themeAnimation.setDirection(Direction.FORWARDS);

        if (theme != ThemePreset.CUSTOM) {
            Color themeColor = themeColors.get(theme);
            hsb = Color.RGBtoHSB(themeColor.getRed(),
                    themeColor.getGreen(),
                    themeColor.getBlue(),
                    null);
            hsb = new float[]{hsb[0], hsb[1], hsb[2]};
            currentColor = new Color(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB());
            setting.set(ColorUtils.setAlpha(currentColor.getRGB(), setting.getAlpha()));
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        updateThemeTransition();
        renderTextAndColorRect(stack);

        animation.setDirection(!panelOpened ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDuration(!panelOpened ? 400 : 300);

        GlStateManager.pushMatrix();
        RenderUtility.sizeAnimation(getX() + (getWidth() / 2),
                (getY() + getHeight() / 2),
                animation.getOutput());

        if (animation.getOutput() > 0.01) {
            int updatedColorRGB = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB();
            setting.set(ColorUtils.setAlpha(updatedColorRGB, setting.getAlpha()));
            currentColor = new Color(setting.get());

            renderSlider(stack, mouseX, mouseY);
            renderPickerPanel(stack, mouseX, mouseY);
            renderAlphaSlider(stack, mouseX, mouseY);
            renderThemeSelector(stack, mouseX, mouseY);

            // Учитываем высоту всех элементов, включая слайдер прозрачности и селектор тем
            float alphaSliderHeight = 2.5f; // Новая высота слайдера
            float targetHeight = 10 + pickerHeight + padding * 3 + sliderHeight + alphaSliderHeight;
            if (showThemeSelector) {
                targetHeight += 10 + padding;
            }
            setHeight(14 + (targetHeight - 14) * (float)animation.getOutput());
            setHeight(14 + (targetHeight - 14) * (float)animation.getOutput());
        } else {
            setHeight(14);
        }

        GlStateManager.popMatrix();
        super.render(stack, mouseX, mouseY);
    }

    private void updateThemeTransition() {
        if (themeTransitionProgress < 1.0f) {
            themeTransitionProgress = (float) themeAnimation.getOutput();
        }
    }

    private void renderTextAndColorRect(MatrixStack stack) {
        String settingName = setting.getName();
        int colorValue = ColorUtils.setAlpha(setting.get(), setting.getAlpha());

        this.textX = this.getX() + padding;
        this.textY = this.getY() + 3;

        this.colorRectWidth = padding * 3f;
        this.colorRectHeight = padding * 1.5f;
        this.colorRectX = this.getX() + getWidth() - colorRectWidth - padding;
        this.colorRectY = this.getY() + 2;

        this.pickerX = this.getX() + padding;
        this.pickerY = this.getY() + padding + 8;
        this.pickerWidth = this.getWidth() - (padding * 2);
        this.pickerHeight = 30;

        this.sliderX = pickerX;
        this.sliderY = pickerY + pickerHeight + padding;
        this.sliderWidth = pickerWidth;
        this.sliderHeight = 3;

        this.themeButtonX = colorRectX - 40f;
        this.themeButtonY = colorRectY;

        int textColor = ColorUtils.interpolateColor(Theme.Text(1),
                themeColors.get(currentTheme).getRGB(),
                themeTransitionProgress);

        if (containsCyrillic(settingName)) {
            ClientFonts.msMedium[16].drawString(stack, settingName, textX, textY, textColor);
        } else {
            ClientFonts.msMedium[16].drawString(stack, settingName, textX, textY, textColor);
        }

        RenderUtility.drawRoundedRect(colorRectX, colorRectY, colorRectWidth, colorRectHeight, 2f, colorValue);
        RenderUtility.drawShadow(colorRectX, colorRectY, colorRectWidth, colorRectHeight, 2, colorValue);

        RenderUtility.drawRoundedRect(themeButtonX, themeButtonY, 15, colorRectHeight, 2f,
                ColorUtils.setAlpha(Color.GRAY.getRGB(), 180));
        ClientFonts.msMedium[12].drawString(stack, "T", themeButtonX + 5, themeButtonY + 3, -1);

        float resetButtonX = colorRectX - 20f;
        RenderUtility.drawRoundedRect(resetButtonX, colorRectY, 15, colorRectHeight, 2f,
                ColorUtils.setAlpha(Color.GRAY.getRGB(), 180));
        ClientFonts.msMedium[12].drawString(stack, "R", resetButtonX + 5, colorRectY + 3, -1);
    }

    private boolean containsCyrillic(String text) {
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CYRILLIC) {
                return true;
            }
        }
        return false;
    }

    private void renderPickerPanel(MatrixStack stack, float mouseX, float mouseY) {
        Vector4i vector4i = new Vector4i(
                ColorUtils.setAlpha(Color.WHITE.getRGB(), 255),
                ColorUtils.setAlpha(Color.BLACK.getRGB(), 255),
                ColorUtils.setAlpha(Color.getHSBColor(hsb[0], 1, 1).getRGB(), 255),
                ColorUtils.setAlpha(Color.BLACK.getRGB(), 255)
        );

        float offset = 2;
        float xRange = pickerWidth - offset * 2;
        float yRange = pickerHeight - offset * 2;

        if (draggingPicker) {
            float saturation = MathHelper.clamp((mouseX - pickerX - offset), 0, xRange) / xRange;
            float brightness = MathHelper.clamp((mouseY - pickerY - offset), 0, yRange) / yRange;
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;

            int updatedColorRGB = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB();
            currentColor = new Color(ColorUtils.setAlpha(updatedColorRGB, setting.getAlpha()));
            setting.set(currentColor.getRGB());
        }

        RenderUtility.drawRoundedRect(pickerX, pickerY, pickerWidth, pickerHeight,
                new Vector4f(2, 2, 2, 2), vector4i);

        float circleX = pickerX + offset + hsb[1] * xRange;
        float circleY = pickerY + offset + (1 - hsb[2]) * yRange;

        RenderUtility.drawCircle(circleX, circleY, 6, ColorUtils.setAlpha(Color.BLACK.getRGB(), 255));
        RenderUtility.drawCircle(circleX, circleY, 4, ColorUtils.setAlpha(Color.WHITE.getRGB(), 255));
    }

    private void renderSlider(MatrixStack stack, float mouseX, float mouseY) {
        float slH = 3;
        for (int i = 0; i < sliderWidth; i++) {
            float hue = i / sliderWidth;
            RenderUtility.drawRoundedRect(sliderX + i, sliderY - slH / 2f, slH, sliderHeight, 1,
                    ColorUtils.setAlpha(Color.HSBtoRGB(hue, 1, 1), 255));
        }

        RenderUtility.drawCircle(sliderX + (hsb[0] * sliderWidth), sliderY, 6,
                ColorUtils.rgba(0, 0, 0, 255));
        RenderUtility.drawCircle(sliderX + (hsb[0] * sliderWidth), sliderY, 4,
                ColorUtils.setAlpha(-1, 255));

        if (draggingHue) {
            hsb[0] = MathHelper.clamp((mouseX - sliderX) / sliderWidth, 0, 1);

            int updatedColorRGB = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB();
            currentColor = new Color(ColorUtils.setAlpha(updatedColorRGB, setting.getAlpha()));
            setting.set(currentColor.getRGB());
        }
    }

    private void renderAlphaSlider(MatrixStack stack, float mouseX, float mouseY) {
        // Позиция слайдера
        float alphaY = sliderY + sliderHeight + padding;
        int currentColorRGB = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB();

        // Уменьшаем высоту слайдера для менее "жирного" вида
        float alphaSliderHeight = 2.5f; // Уменьшили с 5 до 2.5

        // Градиент от черного (слева) к белому (справа) через текущий цвет
        Vector4i gradient = new Vector4i(
                ColorUtils.setAlpha(Color.BLACK.getRGB(), 255), // Начало - черный
                ColorUtils.setAlpha(currentColorRGB, 255),      // Середина - текущий цвет
                ColorUtils.setAlpha(Color.WHITE.getRGB(), 255), // Конец - белый
                ColorUtils.setAlpha(Color.WHITE.getRGB(), 255)  // Для симметрии
        );

        // Отрисовка полоски с градиентом
        RenderUtility.drawRoundedRect(sliderX, alphaY, sliderWidth, alphaSliderHeight, 2, currentColorRGB);

        // Позиция кружка на основе инвертированной яркости (1 - hsb[2])
        float brightnessPos = sliderX + (1 - hsb[2]) * sliderWidth; // Инвертируем яркость
        RenderUtility.drawCircle(brightnessPos, alphaY + alphaSliderHeight / 2, 6,
                ColorUtils.rgba(0, 0, 0, 255));
        RenderUtility.drawCircle(brightnessPos, alphaY + alphaSliderHeight / 2, 4,
                ColorUtils.setAlpha(-1, 255));

        // Обработка перетаскивания
        if (draggingAlpha) {
            float brightness = MathHelper.clamp((mouseX - sliderX) / sliderWidth, 0, 1);
            hsb[2] = 1 - brightness; // Инвертируем значение яркости: слева (0) -> 1, справа (1) -> 0

            int updatedColorRGB = Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB();
            currentColor = new Color(ColorUtils.setAlpha(updatedColorRGB, setting.getAlpha()));
            setting.set(currentColor.getRGB());
        }
    }

    private void renderThemeSelector(MatrixStack stack, float mouseX, float mouseY) {
        if (!showThemeSelector) return;

        // Пересчитываем позицию селектора тем, чтобы он был виден
        float alphaSliderHeight = 5; // Учитываем высоту слайдера прозрачности
        float themeY = sliderY + sliderHeight + padding + alphaSliderHeight + padding;
        float themeWidth = pickerWidth / 5;
        float themeHeight = 10; // Высота каждого элемента селектора тем
        int index = 0;

        for (ThemePreset preset : ThemePreset.values()) {
            float xPos = pickerX + (themeWidth * index);
            Color themeColor = themeColors.get(preset);
            RenderUtility.drawRoundedRect(xPos, themeY, themeWidth - 2, themeHeight, 2f,
                    ColorUtils.setAlpha(themeColor.getRGB(), 255));
            index++;
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (RenderUtility.isInRegion(mouseX, mouseY, colorRectX, colorRectY, colorRectWidth, colorRectHeight)
                && mouse == 1) {
            panelOpened = !panelOpened;
            SoundPlayer.playSound(panelOpened ? "guicoloropen.wav" : "guicolorclose.wav", 0.25);
        }

        float resetButtonX = colorRectX - 20f;
        if (RenderUtility.isInRegion(mouseX, mouseY, resetButtonX, colorRectY, 15, colorRectHeight)
                && mouse == 0) {
            setting.set(initialColor.getRGB());
            hsb = Color.RGBtoHSB(initialColor.getRed(), initialColor.getGreen(),
                    initialColor.getBlue(), null);
            hsb = new float[]{hsb[0], hsb[1], hsb[2]};
            currentColor = new Color(setting.get());
            SoundPlayer.playSound("guicolorreset.wav", 0.25);
        }

        if (RenderUtility.isInRegion(mouseX, mouseY, themeButtonX, themeButtonY, 15, colorRectHeight)
                && mouse == 0) {
            showThemeSelector = !showThemeSelector;
            SoundPlayer.playSound("guiclick.wav", 0.25);
        }

        if (panelOpened && mouse == 0) {
            float alphaY = sliderY + sliderHeight + padding;
            float alphaSliderHeight = 2.5f; // Согласуем с renderAlphaSlider

            if (RenderUtility.isInRegion(mouseX, mouseY, sliderX, sliderY - 1.5f, sliderWidth, sliderHeight)) {
                draggingHue = true;
            } else if (RenderUtility.isInRegion(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight)
                    && animation.isDone()) {
                draggingPicker = true;
            } else if (RenderUtility.isInRegion(mouseX, mouseY, sliderX, alphaY, sliderWidth, alphaSliderHeight)) {
                draggingAlpha = true;
                System.out.println("Alpha slider clicked"); // Для отладки
            } else if (showThemeSelector) {
                float themeY = alphaY + alphaSliderHeight + padding;
                float themeWidth = pickerWidth / 5;
                int index = 0;

                for (ThemePreset preset : ThemePreset.values()) {
                    float xPos = pickerX + (themeWidth * index);
                    if (RenderUtility.isInRegion(mouseX, mouseY, xPos, themeY, themeWidth - 2, 10)) {
                        applyTheme(preset);
                        SoundPlayer.playSound("guiclick.wav", 0.25);
                        break;
                    }
                    index++;
                }
            }
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        if (mouse == 0) {
            if (draggingHue) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingHue = false;
            }
            if (draggingPicker) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingPicker = false;
            }
            if (draggingAlpha) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingAlpha = false;
            }
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}