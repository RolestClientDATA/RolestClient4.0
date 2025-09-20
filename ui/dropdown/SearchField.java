package minecraft.rolest.ui.dropdown;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.Rol;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class SearchField {
    private int x;
    private int y;
    private int width;
    private int height;
    private String searchQuery;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    private Animation slideAnimation = new Animation();
    private Animation focusAnimation = new Animation();
    private final int startY;

    public SearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.searchQuery = "";
        this.isFocused = false;
        this.typing = false;
        this.startY = y;

        this.slideAnimation = new Animation().animate(1, 0.5f, Easings.CUBIC_OUT);
        this.focusAnimation = new Animation().animate(0, 0.3f, Easings.QUAD_OUT);
    }



    public void show() {
        slideAnimation.animate(1, 0.5f, Easings.CUBIC_OUT);
    }

    public void hide() {
        slideAnimation.animate(0, 0.4f, Easings.CUBIC_IN);
    }

    public void reset() {
        slideAnimation.setValue(0);
        searchQuery = "";
        isFocused = false;
        typing = false;
        focusAnimation.setValue(0);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        slideAnimation.update();
        focusAnimation.update();

        float slideValue = (float) slideAnimation.getValue();
        float focusValue = (float) focusAnimation.getValue();
        float animatedY = startY - (1 - slideValue) * 20;

        int bgAlpha = (int) (220 * slideValue);
        int borderAlpha = (int) (255 * slideValue);
        RenderUtility.drawRoundedRect(x - 1, animatedY - 1, width - 148, height + 2, 6,
                ColorUtils.rgba(15 + (int)(20 * focusValue),
                        15 + (int)(20 * focusValue),
                        15 + (int)(20 * focusValue),
                        (int)(borderAlpha * 0.3f)));
        RenderUtility.drawRoundedRect(x, animatedY, width - 150, height, 5,
                ColorUtils.rgba(15, 15, 15, 200));

        final ResourceLocation icon = new ResourceLocation("rolka/images/gui2/poisk.png");
        float iconSize = 14;
        float iconX = x + width - iconSize - 155;
        float iconY = animatedY + (height - iconSize) / 2;
        RenderUtility.drawImage(icon, iconX, iconY, iconSize, iconSize,
                ColorUtils.rgba(180 + (int)(75 * focusValue),
                        180 + (int)(75 * focusValue),
                        180 + (int)(75 * focusValue),
                        (int)(255 * slideValue)));

        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "│" : "";
        int textColor = ColorUtils.rgba(200 + (int)(55 * focusValue),
                200 + (int)(55 * focusValue),
                200 + (int)(55 * focusValue),
                (int)(255 * slideValue));
        int placeholderColor = ColorUtils.rgba(120 + (int)(40 * focusValue),
                120 + (int)(40 * focusValue),
                120 + (int)(40 * focusValue),
                (int)(255 * slideValue));

        if (searchQuery.isEmpty() && !typing) {
            ClientFonts.msMedium[15].drawString(matrixStack,
                    placeholder,
                    x + 8,
                    animatedY + (height - 8) / 2 + 3f,
                    placeholderColor);
        } else {
            ClientFonts.msMedium[15].drawString(matrixStack,
                    searchQuery + cursor,
                    x + 8,
                    animatedY + (height - 8) / 2 + 3f,
                    textColor);
        }
    }

    public String getSearchQuery() {
        return convertKeyboardLayout(searchQuery);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (isFocused) {
            searchQuery += codePoint;
            Rol.getInstance().getDropDown().updateRowHeights();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            isFocused = true;
            typing = true;
            focusAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
            show();
            return true;
        }

        if (isFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                Rol.getInstance().getDropDown().updateRowHeights();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                typing = false;
                isFocused = false;
                focusAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
                Rol.getInstance().getDropDown().updateRowHeights();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                reset();
                Rol.getInstance().getDropDown().updateRowHeights();
                return true;
            }
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float slideValue = (float) slideAnimation.getValue();
        float animatedY = startY - (1 - slideValue) * 20;

        if (MathUtil.isHovered((float) mouseX, (float) mouseY, x, animatedY, width, height)) {
            isFocused = true;
            typing = true;
            focusAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
            show();
            return true;
        } else {
            isFocused = false;
            typing = false;
            focusAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
            return false;
        }
    }

    public boolean isEmpty() {
        return searchQuery.isEmpty();
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
        focusAnimation.animate(focused ? 1 : 0, 0.3f, Easings.QUAD_OUT);
    }

    private static final Map<Character, Character> keyboardLayoutMap = new HashMap<>();

    static {
        String rus = "йцукенгшщзхъфывапролджэячсмитьбю";
        String eng = "qwertyuiop[]asdfghjkl;'zxcvbnm,.";

        for (int i = 0; i < rus.length(); i++) {
            keyboardLayoutMap.put(rus.charAt(i), eng.charAt(i));
            keyboardLayoutMap.put(Character.toUpperCase(rus.charAt(i)), Character.toUpperCase(eng.charAt(i)));
        }
    }

    private String convertKeyboardLayout(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(keyboardLayoutMap.getOrDefault(c, c));
        }
        return result.toString();
    }
}