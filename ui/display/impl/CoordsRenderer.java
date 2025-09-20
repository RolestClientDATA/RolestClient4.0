package  minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import  minecraft.rolest.Rol;
import  minecraft.rolest.ui.display.ElementRenderer;
import  minecraft.rolest.events.EventDisplay;

import minecraft.rolest.modules.impl.render.Theme;
import  minecraft.rolest.utils.render.color.ColorUtils;
import  minecraft.rolest.utils.render.rect.RenderUtility;
import  minecraft.rolest.utils.render.font.Fonts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoordsRenderer implements ElementRenderer {

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();


        float offset = 3;
        float fontSize = 7;
        float padding = 4;
        float radius = 3;

        String coordsText = String.format("XYZ: %d, %d, %d",
                (int)mc.player.getPosX(),
                (int)mc.player.getPosY(),
                (int)mc.player.getPosZ());

        float iconWidth = Fonts.icons2.getWidth("F", fontSize);
        float textWidth = Fonts.sfui.getWidth(coordsText, fontSize);
        float totalWidth = iconWidth + padding + textWidth + padding * 2;
        float height = Fonts.sfui.getHeight(fontSize) + padding * 2;

        float posX = offset;
        float posY = mc.getMainWindow().getScaledHeight() - offset - height;

        drawStyledRect(posX, posY, totalWidth, height, radius);

        float textPosY = posY + (height - Fonts.sfui.getHeight(fontSize)) / 2;
        Fonts.icons2.drawText(ms, "F", posX + padding, textPosY - -0.4f, Theme.Text(0), fontSize);
        Fonts.sfui.drawText(ms, coordsText, posX + padding + iconWidth + 2, textPosY, -1, fontSize);
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        RenderUtility.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 90));
        RenderUtility.drawShadow(x + 3, y + 3, width, height, 3, ColorUtils.rgba(10, 15, 13, 15));
    }
}