package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.ui.display.ElementRenderer;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.font.Fonts;
import net.minecraft.util.math.MathHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BpsRenderer implements ElementRenderer {
    final Dragging dragging;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();


        float fontSize = 7;
        float padding = 4;

        double deltaX = mc.player.getPosX() - mc.player.prevPosX;
        double deltaZ = mc.player.getPosZ() - mc.player.prevPosZ;
        double bps = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20;
        String bpsText = String.format("BPS: %.1f", bps);

        float iconWidth = Fonts.icons2.getWidth("G", fontSize);
        float textWidth = Fonts.sfui.getWidth(bpsText, fontSize);
        float totalWidth = iconWidth + padding + textWidth + padding * 2;
        float height = Fonts.sfui.getHeight(fontSize) + padding * 2;

        float posX = dragging.getX();
        float posY = dragging.getY();

        drawStyledRect(posX, posY, totalWidth, height, 3);

        float textPosY = posY + (height - Fonts.sfui.getHeight(fontSize)) / 2;
        Fonts.icons2.drawText(ms, "G", posX + padding, textPosY - -0.6f,Theme.Text(0), fontSize);
        Fonts.sfui.drawText(ms, bpsText, posX + padding + iconWidth + 2, textPosY, -1, fontSize);

        dragging.setWidth(totalWidth);
        dragging.setHeight(height);
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        RenderUtility.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 90));
        RenderUtility.drawShadow(x + 3, y + 3, width, height, 3, ColorUtils.rgba(10, 15, 13, 15));
    }
}