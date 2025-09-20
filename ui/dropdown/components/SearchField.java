package minecraft.rolest.ui.dropdown.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.api.ModuleManager;
import minecraft.rolest.modules.impl.render.HUD;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.render.GaussianBlur;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.font.Fonts;
import lombok.Setter;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.text.font.ClientFonts;

@ Setter
@ Getter
public class SearchField {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    public SearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.text = "";
        this.isFocused = false;
        this.typing = false;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        drawStyledRect(x, y, width, height, 4);
        Scissor.push();
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        Scissor.setFromComponentCoordinates(x, y, width - 20f, height);
        Fonts.sfMedium.drawText(matrixStack, textToDraw + cursor, x + 5, y + (height - 8) / 2 + 1, ColorUtils.rgb(145,145,145), 7);

        Scissor.unset();
        Scissor.pop();


        ClientFonts.icon[15].drawString(matrixStack, "a", x + 100, y + (height - 8) / 2 + 3.5f, ColorUtils.rgb(145,145,145));
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (isFocused) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE){
            typing = false;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!MathUtil.isHovered((float) mouseX, (float) mouseY, x, y, width, height)){
            isFocused = false;
        }
        isFocused = MathUtil.isHovered((float) mouseX, (float) mouseY, x, y, width, height);
        typing = isFocused;
        return isFocused;
    }
    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {
        ModuleManager moduleManager = Rol.getInstance().getModuleManager();
        HUD blurblat = moduleManager.getHud();
        if(blurblat.blur.get()) {
            GaussianBlur.startBlur();
            RenderUtility.drawRoundedRect(x, y, width, height  ,4,ColorUtils.rgba(5,5,5,200));
            GaussianBlur.endBlur(8,1);
        }
            RenderUtility.drawRoundedRect(x, y, width, height  ,4,ColorUtils.rgba(5,5,5,200));
            RenderUtility.drawRoundedRect(x, y, width, height  ,4,ColorUtils.setAlpha(Theme.RectColor(0),30));


    }
    public boolean isEmpty() {
        return text.isEmpty();
    }
    public void setFocused(boolean focused) { isFocused = focused; }
}