
package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleManager;
import minecraft.rolest.ui.display.ElementRenderer;
import minecraft.rolest.ui.styles.Style;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.GradientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;


public class NotificationsRenderer
        implements ElementRenderer {
    private final ModuleManager functionRegistry;
    private float width;
    private float height;

    public NotificationsRenderer() {
        this.functionRegistry = Rol.getInstance().getModuleManager();
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float screenWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
        float screenHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
        float posX = screenWidth - this.width - 5.0f;
        float posY = screenHeight - this.height - 5.0f;
        float fontSize = 6.5f;
        float padding = 5.0f;
        StringTextComponent title = (StringTextComponent) GradientUtil.white("Функции");
        Style style = Rol.getInstance().getStyleManager().getCurrentStyle();
        RenderUtility.drawShadow(posX, posY, this.width, this.height, 10, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
        this.drawStyledRect(posX, posY, this.width, this.height, 4.0f);
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, this.width, this.height);
        Fonts.sfui.drawCenteredText(ms, title, posX + this.width / 2.0f, posY + padding + 0.5f, fontSize);
        posY += fontSize + padding * 2.0f;
        float maxWidth = Fonts.sfMedium.getWidth(title, fontSize) + padding * 2.0f;
        float localHeight = fontSize + padding * 2.0f;
        for (Module function : this.functionRegistry.getModules()) {
            String functionName = function.getName() + " " + (function.isState() ? "включено" : "выключено");
            float nameWidth = Fonts.sfMedium.getWidth(functionName, fontSize);
            Fonts.sfMedium.drawText(ms, functionName, posX + padding, posY, ColorUtils.rgba(210, 210, 210, 255), fontSize);
            if (nameWidth + padding * 2.0f > maxWidth) {
                maxWidth = nameWidth + padding * 2.0f;
            }
            posY += fontSize + padding;
            localHeight += fontSize + padding;
        }
        Scissor.unset();
        Scissor.pop();
        this.width = Math.max(maxWidth, 80.0f);
        this.height = localHeight + 2.5f;
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        RenderUtility.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(15, 15, 45, 145));
    }

    public NotificationsRenderer(ModuleManager functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}