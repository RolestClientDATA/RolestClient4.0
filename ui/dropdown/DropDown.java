package minecraft.rolest.ui.dropdown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.ModuleManager;
import minecraft.rolest.modules.impl.render.HUD;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.Vector2i;
import minecraft.rolest.utils.render.GaussianBlur;
import minecraft.rolest.modules.impl.render.ClickGui;
import minecraft.rolest.utils.client.ClientUtility;
import minecraft.rolest.utils.client.IMinecraft;
import minecraft.rolest.utils.projections.SoundPlayer;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.Cursors;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.text.font.ClientFonts;

public class DropDown extends Screen implements IMinecraft {

    private final List<Panel> panels = new ArrayList<>();
    @Getter
    private static Animation animation = new Animation();
    @Getter
    private static Animation searchAnimation = new Animation();
    private Panel selectedPanel;

    private float containerX;
    private float containerY;
    private final float containerWidth = 396;
    private final float containerHeight = 264f;
    private final float leftPanelWidth = 99;

    private Animation tabHoverAnimation = new Animation();
    private Animation clickAnimation = new Animation();
    private Map<String, Animation> rectAnimations = new HashMap<>();
    private boolean isOpening = false;
    private Animation closeAnimation = new Animation();
    private boolean isClosing = false;
    private float closingScale = 1.0f;
    private float closingAlpha = 1.0f;
    private final ResourceLocation logo = new ResourceLocation("rolka/images/hud/rolest.png");
    private SearchField searchField;
    private String lastSearchQuery = "";
    private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDir, "config/rolka_dropdown.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public DropDown(ITextComponent titleIn) {
        super(titleIn);
        for (Category category : Category.values()) {
            panels.add(new Panel(category, this));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static float quadInOut(float t) {
        t = MathHelper.clamp(t, 0.0f, 1.0f);
        if (t < 0.5f) {
            return 2.0f * t * t;
        }
        return 1.0f - 2.0f * (t - 1.0f) * (t - 1.0f);
    }

    @Override
    protected void init() {
        isOpening = true;
        isClosing = false;
        closingScale = 1.0f;
        closingAlpha = 1.0f;

        animation = new Animation().animate(0, 0.4f, Easings.QUAD_OUT);
        searchAnimation = new Animation().animate(1, 0.9f, Easings.QUAD_OUT);
        tabHoverAnimation = new Animation().animate(0, 0.3f, Easings.QUAD_OUT);
        clickAnimation = new Animation().animate(0, 0.2f, t -> DropDown.quadInOut((float) t));

        animation.animate(1, 0.4f, Easings.QUAD_OUT);

        super.init();

        int windowWidth = ClientUtility.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtility.calc(mc.getMainWindow().getScaledHeight());

        containerX = (windowWidth - containerWidth) / 2;
        containerY = (windowHeight - containerHeight) / 2;

        float searchX = containerX + leftPanelWidth + 10;
        float searchY = containerY + 10;
        searchField = new SearchField((int) searchX, (int) searchY, 280, 20, "Что хотите поискать?");
        searchField.setSearchQuery("");
        searchField.setTyping(false);
        searchField.setFocused(false);

        // Load the last selected category
        selectedPanel = loadSelectedCategory();

        rectAnimations.clear();
        rectAnimations.put("background", new Animation().animate(0, 0.6f, Easings.CUBIC_OUT));
        rectAnimations.put("container", new Animation().animate(0, 0.5f, Easings.BACK_OUT));
        rectAnimations.put("leftPanel", new Animation().animate(0, 0.4f, Easings.QUAD_OUT));
        rectAnimations.put("tabs", new Animation().animate(0, 0.3f, Easings.QUAD_OUT));
        rectAnimations.put("text", new Animation().animate(0, 0.4f, Easings.QUAD_OUT));

        rectAnimations.get("background").animate(1, 0.6f, Easings.CUBIC_OUT);
        rectAnimations.get("container").animate(1, 0.5f, Easings.BACK_OUT);
        rectAnimations.get("leftPanel").animate(1, 0.4f, Easings.QUAD_OUT);
        rectAnimations.get("tabs").animate(1, 0.3f, Easings.QUAD_OUT);
        rectAnimations.get("text").animate(1, 0.4f, Easings.QUAD_OUT);

        for (Panel panel : panels) {
            panel.resetModuleAnimations();
        }
    }

    public static float scale = 1.00f;

    @Override
    public void closeScreen() {
        if (!isClosing) {
            isClosing = true;
            isOpening = false;

            closeAnimation = new Animation().animate(0, 0.5f, Easings.CUBIC_IN);
            animation.animate(0, 0.4f, Easings.EXPO_IN);
            searchAnimation.animate(0, 0.3f, Easings.EXPO_IN);

            rectAnimations.get("background").animate(0, 0.6f, Easings.CUBIC_IN);
            rectAnimations.get("container").animate(0, 0.5f, Easings.BACK_IN);
            rectAnimations.get("leftPanel").animate(0, 0.4f, Easings.QUAD_IN);
            rectAnimations.get("tabs").animate(0, 0.3f, Easings.QUAD_IN);
            rectAnimations.get("text").animate(0, 0.4f, Easings.QUAD_IN);

            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isClosing) return false;

        SoundPlayer.playSound("guiscroll.wav");
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        if (selectedPanel != null) {
            float visibleHeight = selectedPanel.getHeight() - 37;
            if (selectedPanel.getMax() > visibleHeight) {
                selectedPanel.scrollBy((float) (delta * 20));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (isClosing) return false;

        for (Panel panel : panels) {
            panel.charTyped(codePoint, modifiers);
            if (searchField.charTyped(codePoint, modifiers)) {
                updateRowHeights();
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    public void updateRowHeights() {
        for (Panel panel : panels) {
            panel.updateHeights();
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        rectAnimations.values().forEach(Animation::update);
        animation.update();
        searchAnimation.update();
        tabHoverAnimation.update();
        clickAnimation.update();
        closeAnimation.update();

        if (isClosing) {
            closingScale = (float) closeAnimation.getValue();
            closingAlpha = (float) closeAnimation.getValue();
            if (closingScale <= 0.01f) {
                super.closeScreen();
                isClosing = false;
                return;
            }
        }

        if (!isOpening && !isClosing && animation.getValue() < 0.1) {
            super.closeScreen();
            return;
        }

        if (isOpening && animation.getValue() > 0.99) {
            isOpening = false;
        }

        int windowWidth = ClientUtility.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtility.calc(mc.getMainWindow().getScaledHeight());

        float bgAlpha = (float) rectAnimations.get("background").getValue() * 200 * closingAlpha;
        RenderUtility.drawRect(0, 0, windowWidth, windowHeight, ColorUtils.rgba(0, 0, 0, (int)bgAlpha));

        float containerScale = (float) rectAnimations.get("container").getValue() * closingScale;

        matrixStack.push();
        matrixStack.translate(containerX + containerWidth/2, containerY + containerHeight/2, 0);
        matrixStack.scale(containerScale, containerScale, 1);
        if (isClosing) {
            float rotation = (1.0f - closingScale) * 10.0f;
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(rotation));
        }
        matrixStack.translate(-(containerX + containerWidth/2), -(containerY + containerHeight/2), 0);

        ModuleManager moduleManager = Rol.getInstance().getModuleManager();
        HUD blurblat = moduleManager.getHud();
        ClickGui clickGui = Rol.getInstance().getModuleManager().getClickGui();

        if (clickGui.blurgui.get()) {
            GaussianBlur.startBlur();
            RenderUtility.drawRoundedRect(containerX, containerY, containerWidth, containerHeight, 8,
                    ColorUtils.rgba(10, 10, 10, (int)(230 * containerScale * closingAlpha)));
            GaussianBlur.endBlur(30,10); //10
        }

        RenderUtility.drawRoundedRect(containerX, containerY, containerWidth, containerHeight, 8,
                ColorUtils.rgba(10, 10, 10, (int)(230 * containerScale * closingAlpha)));

        Vector2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        String currentSearchQuery = searchField.getSearchQuery();
        if (!currentSearchQuery.equals(lastSearchQuery)) {
            updateRowHeights();
            lastSearchQuery = currentSearchQuery;
        }

        float leftPanelAlpha = (float) rectAnimations.get("leftPanel").getValue() * closingAlpha;

        float textAlpha = (float) rectAnimations.get("text").getValue() * closingAlpha;



        float tabWidth = 90;
        float tabHeight = 24;
        float tabX = containerX + (leftPanelWidth - tabWidth) / 2;
        float tabYStart = containerY + 40;
        float tabY = tabYStart;
        float tabsAlpha = (float) rectAnimations.get("tabs").getValue() * closingAlpha;


        for (Panel panel : panels) {
            boolean isSelected = panel == selectedPanel;
            boolean isHovered = MathUtil.isHovered((float)mouseX, (float)mouseY, tabX, tabY, tabWidth, tabHeight);

            if (isHovered && !isClosing) {
                tabHoverAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
            } else if (!isHovered && tabHoverAnimation.getValue() == 1) {
                tabHoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
            }

            float hoverScale = (float) (1 + tabHoverAnimation.getValue() * 0.05) * closingScale;
            int tabColor = isSelected ?
                    ColorUtils.setAlpha(Theme.RectColor(1), (int) (200 * tabsAlpha)) :
                    ColorUtils.rgba(20, 20, 20, (int)(0 + tabHoverAnimation.getValue() * 25 * tabsAlpha));
//INFO user


            //RenderUtility.drawRoundedRect(containerX + 26, containerY + 6, 33,24,4, tabColor);
            RenderUtility.drawImage(logo,
                    containerX + 34,
                    containerY + 6,
                    33, 32,
                    Theme.Text(0));

            matrixStack.push();
            matrixStack.translate(tabX + tabWidth/2, tabY + tabHeight/2, 0);
            matrixStack.scale(hoverScale, hoverScale, 1);
            matrixStack.translate(-(tabX + tabWidth/2), -(tabY + tabHeight/2), 0);

            RenderUtility.drawRoundedRect(tabX, tabY, tabWidth, tabHeight, 5, tabColor);
            RenderUtility.drawRoundedRect(tabX + 1, tabY + 1, tabWidth - 2, tabHeight - 2, 4, ColorUtils.rgba(15, 15, 15, 230));

            ClientFonts.rolka[20].drawString(
                    matrixStack,
                    panel.getCategory().getIcon(),
                    tabX + 8,
                    tabY + (tabHeight - ClientFonts.rolka[20].getFontHeight()) / 1.3f,
                    ColorUtils.rgba(
                            (int)(150 + tabHoverAnimation.getValue() * 105),
                            (int)(150 + tabHoverAnimation.getValue() * 105),
                            (int)(150 + tabHoverAnimation.getValue() * 105),
                            (int)(255 * tabsAlpha)
                    )
            );

            ClientFonts.msMedium[17].drawString(
                    matrixStack, panel.getCategory().name(),
                    tabX + 22, tabY + (tabHeight - Fonts.sfMedium.getHeight(10)) / 2 + 2f,
                    ColorUtils.rgba(
                            (int)(220 + tabHoverAnimation.getValue() * 35),
                            (int)(220 + tabHoverAnimation.getValue() * 35),
                            (int)(220 + tabHoverAnimation.getValue() * 35),
                            (int)(255 * tabsAlpha)
                    )
            );

            matrixStack.pop();
            tabY += tabHeight + 6;
        }

        float userRectWidth = 70;
        float userRectHeight = 32;
        float userRectX = containerX + (leftPanelWidth - userRectWidth) / 2;
        float userRectY = containerY + containerHeight - userRectHeight - 10;
        boolean userHovered = MathUtil.isHovered((float)mouseX, (float)mouseY, userRectX, userRectY, userRectWidth, userRectHeight);

        float userAlpha = (float) rectAnimations.get("tabs").getValue() * closingAlpha;

        if (selectedPanel != null && !isClosing) {
            selectedPanel.setX(containerX + leftPanelWidth);
            selectedPanel.setY(containerY + 37);
            selectedPanel.setWidth(containerWidth - leftPanelWidth - 7);
            selectedPanel.setHeight(containerHeight - 43);
            Scissor.push();
            Scissor.setFromComponentCoordinates(selectedPanel.getX(), selectedPanel.getY(),
                    selectedPanel.getWidth(), selectedPanel.getHeight());
            selectedPanel.render(matrixStack, mouseX, mouseY);
            Scissor.unset();
            Scissor.pop();
        }

        matrixStack.push();
        float searchScale = (float) searchAnimation.getValue() * closingScale;
        matrixStack.translate(searchField.getX() + searchField.getWidth()/2,
                searchField.getY() + searchField.getHeight()/2, 0);
        matrixStack.scale(searchScale, searchScale, 1);
        matrixStack.translate(-(searchField.getX() + searchField.getWidth()/2),
                -(searchField.getY() + searchField.getHeight()/2), 0);
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.pop();

        matrixStack.pop();
        mc.gameRenderer.setupOverlayRendering();
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getSearchQuery();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isClosing) return false;

        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
            if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
                updateRowHeights();
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    private Vector2i adjustMouseCoordinates(int mouseX, int mouseY) {
        return new Vector2i(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isClosing) return false;

        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        clickAnimation.animate(1, 0.2f, t -> DropDown.quadInOut((float) t));

        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            searchAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
            return true;
        }

        float tabWidth = 90;
        float tabHeight = 24;
        float tabX = containerX + (leftPanelWidth - tabWidth) / 2;
        float tabY = containerY + 40;

        for (Panel panel : panels) {
            if (MathUtil.isHovered((float) mouseX, (float) mouseY, tabX, tabY, tabWidth, tabHeight)) {
                selectedPanel = panel;
                saveSelectedCategory(panel.getCategory()); // Save the selected category
                SoundPlayer.playSound("guiclick.wav");
                clickAnimation.animate(1, 0.2f, t -> DropDown.quadInOut((float) t));
                panel.resetModuleAnimations();
                return true;
            }
            tabY += tabHeight + 6;
        }

        if (selectedPanel != null) {
            selectedPanel.mouseClick((float) mouseX, (float) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isClosing) return false;

        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtility.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        clickAnimation.animate(0, 0.2f, t -> DropDown.quadInOut((float) t));

        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean searchCheck(String string) {
        return this.isSearching() && !string.replaceAll(" ", "").toLowerCase().contains(this.getSearchText().replaceAll(" ", "").toLowerCase());
    }

    private void saveSelectedCategory(Category category) {
        Map<String, String> config = new HashMap<>();
        config.put("selectedCategory", category.name());
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Panel loadSelectedCategory() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Map<String, String> config = GSON.fromJson(reader, Map.class);
                String categoryName = config.get("selectedCategory");
                if (categoryName != null) {
                    try {
                        Category category = Category.valueOf(categoryName);
                        for (Panel panel : panels) {
                            if (panel.getCategory() == category) {
                                return panel;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // Category not found, fallback to default
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return panels.get(0); // Default to first panel if no saved category or error
    }
}