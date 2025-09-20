package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.ui.display.ElementRenderer;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PotionRenderer implements ElementRenderer {

    final Dragging dragging;

    float width;
    float height;

    Map<String, Animation> effectAnimations = new HashMap<>();

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;

        ITextComponent name = new StringTextComponent("Potions").mergeStyle(TextFormatting.WHITE);


        RenderUtility.drawShadow(posX, posY, width, height, 7, ColorUtils.rgba(9, 8, 23, 1));
        RenderUtility.drawRoundedRect(posX - 1.3f, posY - 1.3f, width + 2.8f, height + 2.8f, 5, ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(posX - 0.5f, posY - 0.5f, width + 1f, height + 1f, 4,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        Fonts.icons2.drawText(eventDisplay.getMatrixStack(), "E", posX + 64, posY + 5.5f, Theme.Text(0), 9);

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        Fonts.sfui.drawCenteredText(ms, name, posX + width / 4, posY + padding - 0.5f, fontSize + 0.45f);
        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfui.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        RenderUtility.drawRectHorizontalW(posX + 0.5f, posY, width - 1, 1.5f, 3,
                ColorUtils.rgba(46, 45, 58, 255));
        posY += 4f;

        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            String key = ef.getEffectName() + "_" + ef.getAmplifier();
            Animation anim = effectAnimations.get(key);
            if (anim == null) {
                anim = new Animation();
                effectAnimations.put(key, anim);
            }
            anim.update();

            if (anim.getValue() <= 0)
                continue;

            int amp = ef.getAmplifier();
            String ampStr = "";
            if (amp >= 1 && amp <= 9) {
                ampStr = " " + I18n.format("enchantment.level." + (amp + 1));
            }
            String nameText = I18n.format(ef.getEffectName()) + ampStr;
            float nameWidth = Fonts.sfui.getWidth(nameText, fontSize);

            String durationText = EffectUtils.getPotionDurationString(ef, 1);
            float durationWidth = Fonts.sfui.getWidth(durationText, fontSize);

            float localWidth = nameWidth + durationWidth + padding * 3;

            Fonts.sfui.drawText(ms, nameText, posX + padding, posY + 0.5f,
                    ColorUtils.rgba(255, 255, 255, (int) (255 * anim.getValue())), fontSize + 0.1f);
            Fonts.sfui.drawText(ms, durationText, posX + width - padding - durationWidth, posY + 0.5f,
                    ColorUtils.rgba(255, 255, 255, (int) (255 * anim.getValue())), fontSize + 0.1f);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (fontSize + padding) * anim.getValue();
            localHeight += (fontSize + padding) * anim.getValue();
        }
        Scissor.unset();
        Scissor.pop();

        width = Math.max(maxWidth, 80);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);

        cleanUpAnimations();
    }

    private void cleanUpAnimations() {
        Map<String, Boolean> activeKeys = new HashMap<>();
        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            String key = ef.getEffectName() + "_" + ef.getAmplifier();
            activeKeys.put(key, true);
        }
        Iterator<String> iterator = effectAnimations.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!activeKeys.containsKey(key)) {
                iterator.remove();
            }
        }
    }

    public static class Animation {
        @Getter
        private float value = 0.0f;
        private final float speed = 0.05f;

        public void update() {
            if (value < 1.0f) {
                value += speed;
                if (value > 1.0f) {
                    value = 1.0f;
                }
            }
        }

    }
}
