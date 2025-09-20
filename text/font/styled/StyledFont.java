package minecraft.rolest.utils.text.font.styled;

import java.awt.*;
import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL30;
import minecraft.rolest.utils.text.font.common.AbstractFont;
import minecraft.rolest.utils.text.font.common.Lang;
import minecraft.rolest.utils.math.MathUtil;

public final class StyledFont {

    private final GlyphPage regular;

    public StyledFont(String fileName, int size, float stretching, float spacing, float lifting, boolean antialiasing, Lang lang) {
        int[] codes = lang.getCharCodes();
        char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];

        int c = 0;
        for (int d = 0; d <= 2; d += 2) {
            for (int i = codes[d]; i <= codes[d + 1] - 1; i++) {
                chars[c] = (char) i;
                c++;
            }
        }

        this.regular = new GlyphPage(AbstractFont.getFont(fileName, Font.PLAIN, size), chars, stretching, spacing, lifting, antialiasing);
    }

    public StyledFont(String fileName, int size, float stretching, float spacing, float lifting, boolean antialiasing, Lang lang, boolean wind) {
        int[] codes = lang.getCharCodes();
        char[] chars = new char[(codes[1] - codes[0] + codes[3] - codes[2])];

        int c = 0;
        for (int d = 0; d <= 2; d += 2) {
            for (int i = codes[d]; i <= codes[d + 1] - 1; i++) {
                chars[c] = (char) i;
                c++;
            }
        }

        this.regular = new GlyphPage(AbstractFont.getFontWindows(fileName, Font.PLAIN, size), chars, stretching, spacing, lifting, antialiasing);
    }

    public static String replaceSymbols(String string) {

        return string
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ꜰ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ꞯ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("ꜱ", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("ʏ", "y")
                .replaceAll("ᴢ", "z")
                .replaceAll("Ａ", "A")
                .replaceAll("Ｂ", "B")
                .replaceAll("Ｃ", "C")
                .replaceAll("Ｄ", "D")
                .replaceAll("Ｅ", "E")
                .replaceAll("Ｆ", "F")
                .replaceAll("Ｇ", "G")
                .replaceAll("Ｈ", "H")
                .replaceAll("Ｉ", "I")
                .replaceAll("Ｊ", "J")
                .replaceAll("Ｋ", "K")
                .replaceAll("Ｌ", "L")
                .replaceAll("Ｍ", "M")
                .replaceAll("Ｎ", "N")
                .replaceAll("Ｏ", "O")
                .replaceAll("Ｐ", "P")
                .replaceAll("Ｑ", "Q")
                .replaceAll("Ｒ", "R")
                .replaceAll("Ｓ", "S")
                .replaceAll("Ｔ", "T")
                .replaceAll("Ｕ", "U")
                .replaceAll("Ｖ", "V")
                .replaceAll("Ｗ", "W")
                .replaceAll("Ｘ", "X")
                .replaceAll("Ｙ", "Y")
                .replaceAll("Ｚ", "Z")
                .replaceAll("ａ", "a")
                .replaceAll("ｂ", "b")
                .replaceAll("ｃ", "c")
                .replaceAll("ｄ", "d")
                .replaceAll("ｅ", "e")
                .replaceAll("ｆ", "f")
                .replaceAll("ｇ", "g")
                .replaceAll("ｈ", "h")
                .replaceAll("ｉ", "i")
                .replaceAll("ｊ", "j")
                .replaceAll("ｋ", "k")
                .replaceAll("ｌ", "l")
                .replaceAll("ｍ", "m")
                .replaceAll("ｎ", "n")
                .replaceAll("ｏ", "o")
                .replaceAll("ｐ", "p")
                .replaceAll("ｑ", "q")
                .replaceAll("ｒ", "r")
                .replaceAll("ｓ", "s")
                .replaceAll("ｔ", "t")
                .replaceAll("ｕ", "u")
                .replaceAll("ｖ", "v")
                .replaceAll("ｗ", "w")
                .replaceAll("ｘ", "x")
                .replaceAll("ｙ", "y")
                .replaceAll("ｚ", "z")
                .replaceAll("[áàâãäå]", "a")
                .replaceAll("[ÁÀÂÃÄÅ]", "A")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[ÉÈÊË]", "E")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[ÍÌÎÏ]", "I")
                .replaceAll("[óòôõö]", "o")
                .replaceAll("[ÓÒÔÕÖ]", "O")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ÚÙÛÜ]", "U")
                .replaceAll("ñ", "n")
                .replaceAll("Ñ", "N")
                .replaceAll("ç", "c")
                .replaceAll("Ç", "C")
                .replaceAll("æ", "ae")
                .replaceAll("Æ", "AE")
                .replaceAll("œ", "oe")
                .replaceAll("Œ", "OE")
                .replaceAll("ß", "ss");
    }
    public float renderGlyph(Matrix4f matrix, char c, float x, float y, boolean bold, boolean italic, float red, float green, float blue, float alpha) {
        return getGlyphPage().renderGlyph(matrix, c, x, y, red, green, blue, alpha);
    }
    public void drawFormattedString(MatrixStack poseStack, IReorderingProcessor sequence, double x, double y, int defaultColor) {
        y -= 3;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        poseStack.push();
        poseStack.scale(0.5f, 0.5f, 1f);
        Matrix4f matrix = poseStack.getLast().getMatrix();
        float startPos = (float) x * 2.0f;
        float posY = (float) y * 2.0f;
        final int[] charCount = {0};
        sequence.accept((i, s, c) -> { charCount[0]++; return true; });
        final float totalChars = charCount[0];
        final float[] posX = {startPos};
        final float[] red = {(float) (defaultColor >> 16 & 255) / 255.0f};
        final float[] green = {(float) (defaultColor >> 8 & 255) / 255.0f};
        final float[] blue = {(float) (defaultColor & 255) / 255.0f};
        float alpha = 1.0f;
        final int[] currentColor = {defaultColor};
        final int[] charIndex = {0};
        sequence.accept((index, style, character) -> {
            String replacedString = replaceSymbols(Character.toString(character));
            char replacedChar = replacedString.charAt(0);
            net.minecraft.util.text.Color styleColor = style.getColor();
            if (styleColor != null && currentColor[0] != styleColor.getColor()) {
                currentColor[0] = styleColor.getColor();
                red[0] = (float) (currentColor[0] >> 16 & 255) / 255.0f;
                green[0] = (float) (currentColor[0] >> 8 & 255) / 255.0f;
                blue[0] = (float) (currentColor[0] & 255) / 255.0f;
            } else if (styleColor == null && currentColor[0] != defaultColor) {
                currentColor[0] = defaultColor;
                red[0] = (float) (defaultColor >> 16 & 255) / 255.0f;
                green[0] = (float) (defaultColor >> 8 & 255) / 255.0f;
                blue[0] = (float) (defaultColor >> 8 & 255) / 255.0f;
            }
            posX[0] += renderGlyph(matrix, replacedChar, posX[0], posY, false, false, red[0], green[0], blue[0], alpha);
            charIndex[0]++;
            return true;
        });
        poseStack.pop();
        GlStateManager.disableBlend();
    }
    public void drawStringWithShadow(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawShadowedString(matrixStack, this, text, x, y, color);
    }

    public void drawString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawStringTest(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.renderStringGradient(matrixStack, this, text, x, y, false, color);
    }


    public void drawString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithShadow(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawShadowedString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, String text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredXString(matrixStack, this, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, ITextComponent text, double x, double y, int color) {
        StyledFontRenderer.drawCenteredString(matrixStack, this, text, x, y, color);
    }

    public void drawStringWithOutline(MatrixStack stack, String text, double x, double y, int color) {
        Color c = new Color(0, 0, 0, 128);
        x = MathUtil.round(x, 0.5F);
        y = MathUtil.round(y, 0.5F);
        StyledFontRenderer.drawString(stack, this, text, x - 0.5, y, c.getRGB());
        StyledFontRenderer.drawString(stack, this, text, x + 0.5, y, c.getRGB());
        StyledFontRenderer.drawString(stack, this, text, x, y - 0.5f, c.getRGB());
        StyledFontRenderer.drawString(stack, this, text, x, y + 0.5f, c.getRGB());

        drawString(stack, text, x, y, color);
    }

    public void drawCenteredStringWithOutline(MatrixStack stack, String text, double x, double y, int color) {
        drawStringWithOutline(stack, text, x - getWidth(text) / 2F, y, color);
    }

    public float getWidth(String text) {
        float width = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length() &&
                    StyledFontRenderer.STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1)) != -1) {
                i++;
            } else {
                width += getGlyphPage().getWidth(c0) + regular.getSpacing();
            }
        }

        return (width - regular.getSpacing()) / 2.0f;
    }

    private GlyphPage getGlyphPage() {
        return regular;
    }

    public float getFontHeight() {
        return regular.getFontHeight();
    }

    public float getLifting() {
        return regular.getLifting();
    }
}
