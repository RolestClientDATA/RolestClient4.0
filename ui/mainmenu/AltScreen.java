package minecraft.rolest.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import minecraft.rolest.Rol;
import minecraft.rolest.config.AltConfig;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.client.ClientUtil;
import minecraft.rolest.utils.client.IMinecraft;
import minecraft.rolest.utils.client.TimerUtility;
import minecraft.rolest.utils.client.Vec2i;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.player.MouseUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.shader.ShaderUtil;
import minecraft.rolest.utils.text.font.ClientFonts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Represents the Alt Manager screen for switching between Minecraft accounts
public class AltScreen extends Screen implements IMinecraft {
    // Constants for UI dimensions and styling
    private static final float WINDOW_WIDTH = 250f;
    private static final float WINDOW_HEIGHT = 240f;
    private static final float OFFSET = 6f;
    private static final float MINUS = 14f;
    private static final float ACCOUNT_ITEM_HEIGHT = 20f;
    private static final float LIST_HEIGHT = 177.5f;
    private static final int BACKGROUND_COLOR = ColorUtils.rgba(20, 20, 20, 200);
    private static final int LIST_BACKGROUND_COLOR = ColorUtils.rgba(15, 15, 15, 155);
    private static final int INPUT_FIELD_COLOR = ColorUtils.rgba(5, 5, 5, 80);
    private static final ResourceLocation BACKMENU = new ResourceLocation("rolka/images/backmenu.png");
    private static final ResourceLocation GLOW = new ResourceLocation("rolka/images/glow.png");

    // Instance variables
    private final TimerUtility timer = new TimerUtility();
    public final List<Alt> alts = new ArrayList<>();
    private float scroll = 0f;
    private float scrollAnimation = 0f;
    private String altName = "";
    private boolean typing = false;

    public AltScreen() {
        super(new StringTextComponent("Account Switcher"));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Smooth scroll animation
        scrollAnimation = MathUtil.lerp(scrollAnimation, scroll, 5);

        // Setup rendering
        Minecraft.getInstance().gameRenderer.setupOverlayRendering(2);

        // Calculate centered window position
        float x = getScaledWidth() / 2f - WINDOW_WIDTH / 2f;
        float y = getScaledHeight() / 2f - WINDOW_HEIGHT / 2f;

        // Render UI components
        renderBackground(matrixStack, x, y);
        renderTitle(matrixStack, x, y);
        renderInputField(matrixStack, x, y, mouseX, mouseY);
        renderAccountList(matrixStack, x, y, mouseX, mouseY);
    }

    // Render the background elements
    private void renderBackground(MatrixStack matrixStack, float x, float y) {
        // Draw shader and background images
        // RenderUtility.drawShader(timer); // Commented out as it seems unused
        RenderUtility.drawImage(BACKMENU, -5, 0, 1000, 1000, -1);
        RenderUtility.drawImage(GLOW, -300, 20, 1000, 1000, ColorUtils.setAlpha(Theme.MainColor(0), 50));
        RenderUtility.drawImage(GLOW, 300, -300, 1000, 1000, ColorUtils.setAlpha(Theme.MainColor(0), 50));

        // Apply shader uniforms
        ShaderUtil.MainMenuShader.attach();
        ShaderUtil.MainMenuShader.setUniform("time", MainScreen.o / 60f);
        ShaderUtil.MainMenuShader.setUniform("width", (float) getMainWindow().getWidth());
        ShaderUtil.MainMenuShader.setUniform("height", (float) getMainWindow().getHeight());

        // Draw main window background
        RenderUtility.drawRoundedRect(x - OFFSET, y - OFFSET, WINDOW_WIDTH + OFFSET * 2f, WINDOW_HEIGHT + OFFSET * 2f, 4, BACKGROUND_COLOR);
    }

    // Render the title text
    private void renderTitle(MatrixStack matrixStack, float x, float y) {
        Fonts.sfMedium.drawCenteredText(matrixStack, "Account Switcher", x + WINDOW_WIDTH / 2, y + OFFSET * 2, -1, 10);
    }

    // Render the input field and random button
    private void renderInputField(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY) {
        float inputX = x + OFFSET - 1;
        float inputY = y + OFFSET + 64 - MINUS * 2.5f + 177 - OFFSET * 2;
        float inputWidth = WINDOW_WIDTH - 120 - OFFSET * 2f;

        // Draw input field
        RenderUtility.drawRoundedRect(inputX, inputY, inputWidth, ACCOUNT_ITEM_HEIGHT, 2f, INPUT_FIELD_COLOR);
        Scissor.push();
        Scissor.setFromComponentCoordinates(inputX, inputY, inputWidth, ACCOUNT_ITEM_HEIGHT);
        String inputText = typing ? (altName + "_") : "Укажите свой ник!";
        Fonts.sfuy.drawText(matrixStack, inputText, inputX + 5f, inputY + 6.5f, ColorUtils.rgb(152, 152, 152), 8);
        Scissor.unset();
        Scissor.pop();

        // Draw random button
        float buttonX = x + WINDOW_WIDTH - 2 - OFFSET - ClientFonts.msSemiBold[22].getWidth("Random") - OFFSET * 2;
        float buttonY = inputY - 0.1f;
        float buttonWidth = ClientFonts.msSemiBold[22].getWidth("Random") + 13f;
        int buttonColor = MathUtil.isHovered(mouseX, mouseY, buttonX, buttonY, buttonWidth, ACCOUNT_ITEM_HEIGHT)
                ? Theme.MainColor(1)
                : ColorUtils.rgba(15, 15, 15, 180);

        RenderUtility.drawRoundedRect(buttonX, buttonY, buttonWidth, ACCOUNT_ITEM_HEIGHT, 4, buttonColor);
        Fonts.sfMedium.drawCenteredText(matrixStack, "Random", buttonX + buttonWidth / 2, buttonY + 1.1f + ClientFonts.msSemiBold[22].getFontHeight() / 2, -1, 8);
    }

    // Render the account list
    private void renderAccountList(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY) {
        float listX = x + OFFSET;
        float listY = y + OFFSET + 60f - MINUS * 2;
        float listWidth = WINDOW_WIDTH - OFFSET * 2f;

        RenderUtility.drawRoundedRect(listX - 1, listY, listWidth + 2, LIST_HEIGHT - MINUS * 2, 4, LIST_BACKGROUND_COLOR);

        if (alts.isEmpty()) {
            Fonts.sfuy.drawCenteredText(matrixStack, "пусто", x + WINDOW_WIDTH / 2f, listY + (LIST_HEIGHT - MINUS) / 2, -1, 8);
            return;
        }

        Scissor.push();
        Scissor.setFromComponentCoordinates(listX, listY, listWidth, LIST_HEIGHT - MINUS * 2);
        float iter = scrollAnimation;
        for (Alt alt : alts) {
            float scrollY = y + iter * 22f;
            int color = Minecraft.getInstance().session.getUsername().equals(alt.name)
                    ? Theme.MainColor(1)
                    : ColorUtils.rgba(20, 20, 20, 145);

            // Draw account item
            RenderUtility.drawRoundedRect(listX + 2f, scrollY + OFFSET + 62 - MINUS * 2, listWidth - 4f, ACCOUNT_ITEM_HEIGHT, 2f, color);
            RenderUtility.drawRoundedRect(listX + 2.3f, scrollY + OFFSET + 62 - MINUS * 2, listWidth - 4.5f, ACCOUNT_ITEM_HEIGHT, 2f, ColorUtils.rgba(20, 20, 20, 210));

            // Draw account name and skin
            Fonts.sfuy.drawText(matrixStack, alt.name, listX + 24f, scrollY + OFFSET + 69 - MINUS * 2, -1, 8);
            Minecraft.getInstance().getTextureManager().bindTexture(alt.skin);
            AbstractGui.drawScaledCustomSizeModalRect(listX + 4.5f, scrollY + OFFSET + 63.5f - MINUS * 2, 8f, 8f, 8f, 8f, 16, 16, 64, 64);

            iter++;
        }
        scroll = MathHelper.clamp(scroll, alts.size() > 8 ? -alts.size() + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !altName.isEmpty()) {
                altName = altName.substring(0, altName.length() - 1);
            } else if (keyCode == GLFW.GLFW_KEY_ENTER && !altName.isEmpty() && altName.length() >= 3) {
                alts.add(new Alt(altName));
                AltConfig.updateFile();
                typing = false;
                altName = "";
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                typing = false;
                altName = "";
            } else if (ClientUtil.ctrlIsDown()) {
                if (keyCode == GLFW.GLFW_KEY_V) {
                    try {
                        altName += ClientUtil.pasteString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    altName = "";
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing && altName.length() <= 20) {
            altName += codePoint;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = getScaledWidth() / 2f - WINDOW_WIDTH / 2f;
        float y = getScaledHeight() / 2f - WINDOW_HEIGHT / 2f;

        // Handle random button click
        float buttonX = x + WINDOW_WIDTH - 2 - OFFSET - ClientFonts.msSemiBold[22].getWidth("Random") - OFFSET * 2;
        float buttonY = y + OFFSET + 63.9f - MINUS * 2.5f + 177 - OFFSET * 2;
        if (button == 0 && RenderUtility.isInRegion(mouseX, mouseY, buttonX, buttonY, ClientFonts.msSemiBold[22].getWidth("Random") + 13f, ACCOUNT_ITEM_HEIGHT)) {
            alts.add(new Alt(Rol.getInstance().randomNickname()));
            AltConfig.updateFile();
        }

        // Handle input field click
        if (button == 0 && RenderUtility.isInRegion(mouseX, mouseY, x + OFFSET - 1, buttonY, WINDOW_WIDTH - 120 - OFFSET * 2f, ACCOUNT_ITEM_HEIGHT)
                && !RenderUtility.isInRegion(mouseX, mouseY, buttonX, buttonY, ClientFonts.msSemiBold[22].getWidth("Random") + 12f, ACCOUNT_ITEM_HEIGHT)) {
            typing = !typing;
        }

        // Handle account list clicks
        float iter = scrollAnimation;
        Iterator<Alt> iterator = alts.iterator();
        while (iterator.hasNext()) {
            Alt account = iterator.next();
            float scrollY = y + iter * 22f;
            if (RenderUtility.isInRegion(mouseX, mouseY, x + OFFSET + 2f, scrollY + OFFSET + 62 - MINUS * 2, WINDOW_WIDTH - OFFSET * 2f - 4f, ACCOUNT_ITEM_HEIGHT)) {
                if (button == 0) {
                    Minecraft.getInstance().session = new Session(account.name, "", "", "mojang");
                } else if (button == 1) {
                    iterator.remove();
                    AltConfig.updateFile();
                }
            }
            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = getScaledWidth() / 2f - WINDOW_WIDTH / 2f;
        float y = getScaledHeight() / 2f - WINDOW_HEIGHT / 2f;

        if (MouseUtil.isHovered(mouseX, mouseY, x + OFFSET, y + OFFSET + 60f - MINUS * 2, WINDOW_WIDTH - OFFSET * 2f, LIST_HEIGHT - MINUS * 2)) {
            scroll += delta * 1;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }

    // Helper methods for cleaner access to Minecraft properties
    private float getScaledWidth() {
        return Minecraft.getInstance().getMainWindow().getScaledWidth();
    }

    private float getScaledHeight() {
        return Minecraft.getInstance().getMainWindow().getScaledHeight();
    }

    private net.minecraft.client.MainWindow getMainWindow() {
        return Minecraft.getInstance().getMainWindow();
    }
}