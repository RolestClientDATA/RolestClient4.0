package minecraft.rolest.ui.dropdown.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import minecraft.rolest.modules.settings.impl.*;
import minecraft.rolest.ui.dropdown.components.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.settings.Setting;

import minecraft.rolest.ui.dropdown.Panel;

import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.Cursors;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.text.font.ClientFonts;

@Getter
public class ModuleComponent extends Component {
    private static final Vector4f ROUNDING_VECTOR = new Vector4f(4, 4, 4, 4);
    private static final float MODULE_HEIGHT = 24;

    private final Module module;
    private final Animation hoverAnimation = new Animation();
    private final Animation appearAnimation = new Animation();
    private final Animation openAnimation = new Animation();
    private final Animation toggleAnimation = new Animation();
    private boolean open;
    private boolean bind;
    private boolean showBindPrompt;
    private final Animation bindAnimation = new Animation();
    private final ObjectArrayList<Component> components = new ObjectArrayList<>();
    private Panel panel;
    private boolean hovered;

    public ModuleComponent(Module module) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            } else if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            } else if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            } else if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            } else if (setting instanceof ModeListSetting modeList) {
                components.add(new MultiBoxComponent(modeList));
            } else if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            } else if (setting instanceof ColorSetting color) {
                components.add(new ColorComponent(color));
            }
        }
        hoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
        appearAnimation.animate(0, 0.5f, Easings.CUBIC_OUT);
        openAnimation.animate(0, 0.3f, Easings.CUBIC_OUT);
        bindAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
        toggleAnimation.animate(module.isState() ? 1 : 0, 0.3f, Easings.QUAD_OUT);
        setHeight(MODULE_HEIGHT);
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
    }

    public boolean isOpen() {
        return open;
    }

    public float getSettingsHeight() {
        if (!open) return 0;
        float height = 0;
        for (Component component : components) {
            if (component.isVisible()) {
                height += component.getHeight();
            }
        }
        return height + 4;
    }

    public void resetAnimation() {
        appearAnimation.setValue(0);
        appearAnimation.animate(1, 0.5f, Easings.CUBIC_OUT);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        hoverAnimation.update();
        appearAnimation.update();
        openAnimation.update();
        bindAnimation.update();
        toggleAnimation.update();

        float animationValue = (float) appearAnimation.getValue();
        float offsetY = (0.95f - animationValue) * -50;
        float renderY = getY() + offsetY;
        float alpha = animationValue * 230;
        float openValue = (float) openAnimation.getValue();
        float totalHeight = MODULE_HEIGHT + (getSettingsHeight() * openValue);

        if (module.isState()) {
            RenderUtility.drawRoundedRect(
                    getX() - 1, renderY - 1, getWidth() + 2, totalHeight + 2,
                    6, ColorUtils.setAlpha(Theme.MainColor(1), 180)
            );
        }

        RenderUtility.drawRoundedRect(
                getX(), renderY, getWidth(), totalHeight,
                5, ColorUtils.rgba(15, 15, 15, (int) alpha)
        );

        // Toggle switch design
        float toggleX = getX() + getWidth() - 32;
        float toggleY = renderY + (MODULE_HEIGHT - 14) / 2;
        float toggleWidth = 24;
        float toggleHeight = 12;
        float innerCircleRadius = 5;

        RenderUtility.drawRoundedRect(
                toggleX, toggleY, toggleWidth, toggleHeight,
                toggleHeight / 2,
                ColorUtils.rgba(50, 50, 50, (int) (200 * animationValue))
        );

        float toggleProgress = (float) toggleAnimation.getValue();
        float innerCircleX = toggleX + 1 + (toggleWidth - innerCircleRadius * 2 - 2) * toggleProgress;
        RenderUtility.drawCircle(
                innerCircleX + innerCircleRadius, toggleY + toggleHeight / 2, innerCircleRadius + 2,
                module.isState()
                        ? ColorUtils.reAlphaInt(Theme.MainColor(1), (int) (255 * animationValue))
                        : ColorUtils.rgba(200, 200, 200, (int) (255 * animationValue))
        );

        String displayText;
        float bindAlpha = (float) bindAnimation.getValue();
        if (showBindPrompt && bindAlpha > 0) {
            int bindKey = module.getBind();
            if (bindKey == 0) {
                displayText = "Выберите букву...";
            } else {
                String keyName = GLFW.glfwGetKeyName(bindKey, GLFW.glfwGetKeyScancode(bindKey));
                displayText = "Бинд: " + (keyName != null ? keyName.toUpperCase() : "Unknown");
            }
        } else {
            displayText = module.getName();
        }

        float textX = getX() + 10;
        float textY = renderY + (MODULE_HEIGHT - ClientFonts.msSemiBold[14].getFontHeight()) / 2 + 3f;
        int textColor = ColorUtils.rgba(230, 230, 230, (int) (255 * (showBindPrompt ? bindAlpha : animationValue)));

        ClientFonts.msMedium[16].drawString(stack, displayText, textX, textY - 1f, textColor);
        float textWidth = ClientFonts.msMedium[16].getWidth(displayText);

        if (!components.isEmpty()) {
            float iconX = textX + textWidth + 4;
            float iconY = renderY + (MODULE_HEIGHT - 8) / 2;
            int iconColor = open ? ColorUtils.rgba(200, 200, 200, (int) (255 * animationValue))
                    : ColorUtils.rgba(150, 150, 150, (int) (255 * animationValue));

            RenderUtility.drawImage(
                    new ResourceLocation("rolka/images/default.png"),
                    iconX - 2f,
                    iconY - 1f,
                    8,
                    8,
                    iconColor
            );
        }

        if (open) {
            float settingsOffsetY = MODULE_HEIGHT;



            Scissor.push();
            Scissor.setFromComponentCoordinates(getX(), renderY + MODULE_HEIGHT, getWidth(), getSettingsHeight() * openValue);

            for (Component component : components) {
                if (!component.isVisible()) continue;

                float componentY = renderY + settingsOffsetY;
                component.setX(getX() + 5);
                component.setY(componentY);
                component.setWidth(getWidth() - 10);
                component.render(stack, mouseX, mouseY);
                settingsOffsetY += component.getHeight() * openValue;
            }

            Scissor.unset();
            Scissor.pop();
        }


        if (MathUtil.isHovered(mouseX, mouseY, getX(), renderY, getWidth(), MODULE_HEIGHT)) {
            if (!hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
                hovered = true;
                hoverAnimation.animate(1, 0.3f, Easings.QUAD_OUT);

            }

        } else if (hovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            hovered = false;
            hoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
        }
    }
    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        float animationValue = (float) appearAnimation.getValue();
        float offsetY = (1 - animationValue) * -50;
        float renderY = getY() + offsetY;
        float openValue = (float) openAnimation.getValue();
        float totalHeight = MODULE_HEIGHT + (getSettingsHeight() * openValue);



        if (MathUtil.isHovered(mouseX, mouseY, getX(), renderY, getWidth(), MODULE_HEIGHT)) {
            if (button == 0) {

                if (MathUtil.isHovered(mouseX, mouseY, getX(), renderY, getWidth(), totalHeight)) {

                    module.toggle();
                    toggleAnimation.animate(module.isState() ? 1 : 0, 0.3f, Easings.QUAD_OUT);
                }
            } else if (button == 1 && !components.isEmpty()) {
                open = !open;
                openAnimation.animate(open ? 1 : 0, 0.3f, Easings.CUBIC_OUT);
                Rol.getInstance().getDropDown().updateRowHeights();
            } else if (button == 2) {
                showBindPrompt = true;
                bindAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
                bind = true;
            }
            return;
        }



        if (open) {
            float settingsOffsetY = MODULE_HEIGHT;
            for (Component component : components) {
                if (!component.isVisible()) continue;

                float componentY = renderY + settingsOffsetY;
                component.setX(getX() + 5);
                component.setY(componentY);
                component.setWidth(getWidth() - 10);
                float componentHeight = component.getHeight();

                if (MathUtil.isHovered(mouseX, mouseY, getX() + 5, componentY, getWidth() - 10, componentHeight)) {
                    component.mouseClick(mouseX, mouseY, button);
                    return;
                }
                settingsOffsetY += componentHeight;
            }
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        if (open) {
            for (Component component : components) {
                if (component.isVisible()) {
                    component.mouseRelease(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (open) {
            for (Component component : components) {
                if (component.isVisible()) {
                    component.charTyped(codePoint, modifiers);
                }
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
                bind = false;
                showBindPrompt = false;
                bindAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
            } else {
                module.setBind(key);
                bind = false;
                bindAnimation.animate(1, 0.3f, Easings.QUAD_OUT);
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        bindAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
                        showBindPrompt = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        if (open) {
            for (Component component : components) {
                if (component.isVisible()) {
                    component.keyPressed(key, scanCode, modifiers);
                }
            }
        }
    }
}