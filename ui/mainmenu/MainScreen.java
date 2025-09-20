package minecraft.rolest.ui.mainmenu;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.ClientDataReader;
import minecraft.rolest.utils.animations.easing.CompactAnimation;
import minecraft.rolest.utils.animations.easing.Easing;
import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.utils.NameClient;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.rolest.Rol;
import minecraft.rolest.utils.client.ClientUtil;
import minecraft.rolest.utils.client.IMinecraft;
import minecraft.rolest.utils.client.Vec2i;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.text.BetterText;
import minecraft.rolest.utils.text.font.ClientFonts;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

import static minecraft.rolest.utils.client.UserPublic.getVers;

public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));

    }
    private final ResourceLocation logo = new ResourceLocation("rolka/images/hud/rolest.png");
    private final ResourceLocation backmenu = new ResourceLocation("rolka/images/backmenu.png");
    private final ResourceLocation glow = new ResourceLocation("rolka/images/glow.png");

    public final StopWatch timer = new StopWatch();
    public static float o = 0;

    private final List<Button> buttons = new ArrayList<>();
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_IN_BACK, 100);

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        MainWindow mainWindow = mc.getMainWindow();
        super.init(minecraft, width, height);
        float widthButton = 360 / 4f;
        float heightButton = 55 / 2f - 5;
        float fontSize = 5;
        String username = ClientDataReader.getUsername();
        float widthButtonExit = 70;
        float heightButtonExit = 20;


        float x = ClientUtil.calc(width) / 2f - widthButton / 2f;
        float y = Math.round(ClientUtil.calc(height) / 2f + 1) - 60;

        float x1 = ClientUtil.calc(width) / 2f - widthButton / 2f + 440;
        float y1 = Math.round(ClientUtil.calc(height) / 2f + 1) - 245;

        buttons.clear();
        buttons.add(new Button(x-15, y, widthButton+20, heightButton, "Одиночный", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += 50 / 2f;
        buttons.add(new Button(x-15, y, widthButton+20, heightButton, "Мультиплеер", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += 50 / 2f;
        buttons.add(new Button(x-15, y, widthButton+20, heightButton, "Аккаунты", () -> {
            mc.displayGuiScreen(Rol.getInstance().getAltScreen());
        }));
        y += 50 / 2f;
        buttons.add(new Button(x-15, y, widthButton+20, heightButton, "Настройки", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        y += 55 / 2f;
        buttons.add(new Button(x - 5, y, widthButton, heightButton, "Выход", mc::shutdownMinecraftApplet));

    }


    private final StopWatch stopWatch = new StopWatch();
    static boolean start = false;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        MainWindow mainWindow = mc.getMainWindow();
        RenderUtility.drawRoundedRect(0,0, mainWindow.getScaledWidth(), mainWindow.getScaledHeight(), 0, ColorUtils.rgb(15, 15, 15));

        float y = Math.round(ClientUtil.calc(height) / 2f - (ClientFonts.msBold[22].getFontHeight())) - 60;

        for (float i=0;i<1488;i++) {
            if(timer.isReached(10)){
                MainScreen.o++;
                i=0;
                timer.reset();
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        RenderUtility.drawQuads(0, 0, mainWindow.getScaledWidth(), mainWindow.getScaledHeight(), 7);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        mc.gameRenderer.setupOverlayRendering(2);

        float widthRect = 330 / 4f + 10;
        float fontSize = 50;
        RenderUtility.drawImage(backmenu, 0, 0, width + 200, height + 200, -1);
        RenderUtility.drawImage(glow, -300, 20, 1000, 1000, ColorUtils.setAlpha(Theme.MainColor(0),50));
        RenderUtility.drawImage(glow, 300, -300, 1000, 1000, ColorUtils.setAlpha(Theme.MainColor(0),50));

//text
        RenderUtility.drawImage(logo, mainWindow.getScaledWidth() / 2 - 27, y - 66,48, 47, ColorUtils.rgb(255,255,255));
        drawButtons(matrixStack, mouseX, mouseY, partialTicks);
        mc.gameRenderer.setupOverlayRendering();Fonts.sfuy.drawCenteredText(matrixStack, "Приятной игры 3>   ", mainWindow.getScaledWidth() / 2, y - 4, -1, 8f);
        Fonts.sfuy.drawCenteredText(matrixStack, " Добро пожаловать  " +(ClientDataReader.getUsername()) + "!", mainWindow.getScaledWidth() / 2, y - 15, -1, 8f);


    }

    private final BetterText gavno = new BetterText(List.of(
            "Development build " + getVers,"Development build" + getVers ,"Development build" +getVers
    ), 1500);
    private String setMessage() {
        gavno.update();
        String emoji = gavno.getOutput().toString();
        String userName =  "" + emoji;

        return userName + " ";
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }

    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        public float animation;



        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }


        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            int colorbutton = MathUtil.isHovered(mouseX, mouseY, x, y, width, height) ? Theme.MainColor(1) : ColorUtils.rgba(15, 15, 15, 100);
            RenderUtility.drawRoundedRect(x, y + 2, width, height, 4,colorbutton);
            RenderUtility.drawRoundedRect(x + 0.2f, y + 2, width - 0.5f, height, 4,ColorUtils.rgba(15, 15, 15,250));
            int color = MathUtil.isHovered(mouseX, mouseY, x, y, width, height) ? -1 : ColorUtils.rgba(200, 200, 200, (int) (255));
            Fonts.sfuy.drawCenteredText(stack, text, x + width / 2f, y + height / 2f - 2f, color, 8f, 0.05f);
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();

            }
        }
    }
}
