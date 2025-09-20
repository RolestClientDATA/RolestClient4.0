package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.impl.render.WorldTweaks;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;
import minecraft.rolest.utils.client.KeyStorage;

public class BindComponent extends Component {
    private static final float BASE_HEIGHT = 24f;
    private static final float SIDE_PADDING = 6f;

    private final BindSetting setting;
    private boolean activated = false;
    private final Animation hoverAnimation = new Animation();

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        setHeight(BASE_HEIGHT);
        hoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        hoverAnimation.update();

        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), BASE_HEIGHT);
        hoverAnimation.animate(isHovered ? 1 : 0, 0.3f, Easings.QUAD_OUT);
        float hoverValue = (float) hoverAnimation.getValue();

        RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), BASE_HEIGHT, 4,
                ColorUtils.rgba(40, 40, 40, 230));
        if (isHovered) {
            RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), BASE_HEIGHT, 4,
                    ColorUtils.rgba(255, 255, 255, (int) (30 * hoverValue)));
        }

        float textY = getY() + (BASE_HEIGHT - ClientFonts.msSemiBold[14].getFontHeight()) / 2;
        ClientFonts.msSemiBold[14].drawString(stack, setting.getName(), getX() + SIDE_PADDING, textY + 2,
                ColorUtils.rgba(230, 230, 230, 255));

        String bind = KeyStorage.getKey(setting.get());
        if (bind == null || setting.get() == -1) {
            bind = "None";
        }
        if (activated) {
            bind = "Press a key...";
        }

        float valueWidth = getWidth() * 0.4f;
        float valueX = getX() + getWidth() - valueWidth - SIDE_PADDING;
        float valueY = getY() + (BASE_HEIGHT - 12) / 2;

        int themeColor = Rol.getInstance().getModuleManager().getWorldTweaks().isState()
                ? WorldTweaks.colorFog.get() : Theme.MainColor(1);
        RenderUtility.drawRoundedRect(valueX, valueY, valueWidth, 12, 3,
                ColorUtils.rgba(30, 30, 30, 200));
        if (activated) {
            RenderUtility.drawRoundedRect(valueX, valueY, valueWidth, 12, 3,
                    ColorUtils.setAlpha(themeColor, (int) (70 * hoverValue)));
        }

        ClientFonts.msMedium[13].drawString(stack, bind, valueX + 4,
                valueY + (12 - ClientFonts.msMedium[13].getFontHeight()) / 2 + 2f,
                ColorUtils.rgba(200, 200, 200, 255));
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                setting.set(-1);
            } else {
                setting.set(key);
            }
            activated = false;
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), BASE_HEIGHT) && button == 0) {
            activated = !activated;
        }
        if (activated && button >= 1) {
            setting.set(-100 + button);
            activated = false;
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}