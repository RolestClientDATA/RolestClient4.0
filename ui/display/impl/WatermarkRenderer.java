package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.ui.display.ElementRenderer;
import minecraft.rolest.utils.ClientDataReader;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.rolest.utils.render.rect.RenderUtility;
import net.minecraft.util.ResourceLocation;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();


        // Настройки размеров
        float posX = 80;
        float posXL = 5;
        float posY = 5;
        float fontSize = 7.0f;
        float spacing = 2.5f;
        float padding = 5.0f;
        float elementHeight = 16;

        // В username пишите что хотите, название своего чита и тп
        String username = ClientDataReader.getUsername();
        String CLIENT = "Rolest Client";
        String SERVER = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "Singleplayer";
        String FPS = mc.debugFPS + "fps";
        String PING = mc.player != null ? mc.player.connection.getPlayerInfo(mc.player.getGameProfile().getId()).getResponseTime() + " ms" : "N/A";

        float usernameWidth = Fonts.sfMedium.getWidth(username, fontSize);
        float serverWidth = Fonts.sfMedium.getWidth(SERVER, fontSize);
        float fpsWidth = Fonts.sfMedium.getWidth(FPS, fontSize);
        float pingWidth = Fonts.sfMedium.getWidth(PING, fontSize);

        float iconSpacing = 2.5f;
        float usernameIconWidth = Fonts.icons2.getWidth("U", fontSize);
        float serverIconWidth = Fonts.icons2.getWidth("F", fontSize);
        float fpsIconWidth = Fonts.icons2.getWidth("S", fontSize);
        float pingIconWidth = Fonts.icons2.getWidth("D", fontSize);

        final ResourceLocation logo = new ResourceLocation("rolka/images/hud/rolest.png");
        float logoSize = elementHeight;
        float logoPadding = 2.0f;

        float contentWidth = usernameIconWidth + iconSpacing + usernameWidth + spacing +
                serverIconWidth + iconSpacing + serverWidth + spacing +
                fpsIconWidth + iconSpacing + fpsWidth + spacing +
                pingIconWidth + iconSpacing + pingWidth;

        float width = contentWidth + padding * 2;
        float radius = 2.5f;

        // Сам рисунок Логотипа
        drawStyledRect(posXL - 1, posY, logoSize +1, elementHeight, radius);
        RenderUtility.drawImage(logo,
                posXL + (logoSize - 8)/2 - 4.7f,
                posY + (elementHeight - 8)/2 - 3,
                16, 15,
                Theme.Text(0));

        drawStyledRect(posX + logoSize + logoPadding, posY, width, elementHeight, radius);

        float textPosX = posX + logoSize + logoPadding + padding;
        float textPosY = posY + (elementHeight - fontSize) / 2 + 1;

        Fonts.icons2.drawText(ms, "U", textPosX, textPosY + 0.2f,Theme.Text(0), fontSize);
        textPosX += usernameIconWidth + iconSpacing;
        Fonts.sfMedium.drawText(ms, username, textPosX, textPosY + -0.75f, ColorUtils.rgb(255, 255, 255), fontSize);
        textPosX += usernameWidth + spacing;

        Fonts.icons2.drawText(ms, "F", textPosX, textPosY + 0.2f, Theme.Text(0), fontSize);
        textPosX += serverIconWidth + iconSpacing;
        Fonts.sfMedium.drawText(ms, SERVER, textPosX, textPosY + -0.75f, ColorUtils.rgb(255, 255, 255), fontSize);
        textPosX += serverWidth + spacing;

        Fonts.icons2.drawText(ms, "S", textPosX, textPosY + 0.2f, Theme.Text(0), fontSize);
        textPosX += fpsIconWidth + iconSpacing;
        Fonts.sfMedium.drawText(ms, FPS, textPosX, textPosY + -0.75f, ColorUtils.rgb(255, 255, 255), fontSize);
        textPosX += fpsWidth + spacing;

        Fonts.icons2.drawText(ms, "Q", textPosX, textPosY + 0.1f,Theme.Text(0), fontSize);
        textPosX += pingIconWidth + iconSpacing;
        Fonts.sfMedium.drawText(ms, PING, textPosX, textPosY + -0.75f, ColorUtils.rgb(255, 255, 255), fontSize);


        float width1 = 73;
        float WaterX = 5 + 16 + 2.0f;

      drawStyledRect(5 + 16 + 2.0f, posY, width1, elementHeight, radius);


        Fonts.sfMedium.drawText(ms,CLIENT , WaterX + 17, textPosY + -0.75f, ColorUtils.rgb(255,255,255), fontSize);

    }





    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        RenderUtility.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(x, y, width, height, radius,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
     //   RenderUtility.drawShadow(x + 3, y + 3, width, height, 3, ColorUtils.rgba(10, 15, 13, 15));
    }
}