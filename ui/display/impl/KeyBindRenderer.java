package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.ui.display.ElementRenderer;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.client.KeyStorage;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.font.Fonts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class KeyBindRenderer implements ElementRenderer {

    final Dragging dragging;

    float width;
    float height;

    private final ResourceLocation on_function = new ResourceLocation("rolka/images/on_function.png");

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;

        ITextComponent name = new StringTextComponent("Hotkeys").mergeStyle(TextFormatting.WHITE);



        RenderUtility.drawShadow(posX, posY, width, height, 7, ColorUtils.rgba(9, 8, 23, 1));
        RenderUtility.drawRoundedRect(posX - 1.3f, posY - 1.3f, width + 2.8f, height + 2.8f, 5,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(posX - 0.5f, posY - 0.5f, width + 1f, height + 1f, 4,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        Fonts.icons2.drawText(eventDisplay.getMatrixStack(), "C", posX + 63, posY + 5.5f, Theme.Text(0), fontSize);


        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        Fonts.sfui.drawCenteredText(ms, name, posX + width / 4 + 1, posY + padding, fontSize + 0.45f);
        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfui.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;


        RenderUtility.drawRectHorizontalW(posX + 0.5f, posY, width - 1, 1.5f, 3, ColorUtils.rgba(46, 45, 58, (int) (255 * 1f)));
        posY += 4f;

        for (Module f : Rol.getInstance().getModuleManager().getModules()) {
            f.getAnimation().update();
            if (!(f.getAnimation().getValue() > 0) || f.getBind() == 0)
                continue;
            String nameText = f.getName();
            float nameWidth = Fonts.sfui.getWidth(nameText, fontSize);

            String bindText = "[" + KeyStorage.getKey(f.getBind()) + "]";
            float bindWidth = Fonts.sfui.getWidth(bindText, fontSize);

            float localWidth = nameWidth + bindWidth + padding * 3;

            Fonts.sfui.drawText(ms, nameText, posX + padding, posY + 0.5f,
                    ColorUtils.rgba(255, 255, 255, (int) (255 * f.getAnimation().getValue())), fontSize + 0.1f);
            int color = Theme.Text(0);
            Fonts.icons2.drawText(eventDisplay.getMatrixStack(), "J", posX + 63, posY + 0.5f, color, fontSize + 2);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (fontSize + padding) * f.getAnimation().getValue();
            localHeight += (fontSize + padding) * f.getAnimation().getValue();
        }
        Scissor.unset();
        Scissor.pop();

        width = Math.max(maxWidth, 80);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}
