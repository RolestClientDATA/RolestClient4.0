package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

public class BooleanComponent extends Component {

    private final BooleanSetting setting;
    private final Animation hoverAnimation = new Animation();
    private final Animation toggleAnimation = new Animation();

    private final float baseHeight = 20;
    private final float sidePadding = 6;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(baseHeight);
        hoverAnimation.setValue(0);
        toggleAnimation.setValue(setting.get() ? 1 : 0);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        hoverAnimation.update();
        toggleAnimation.update();

        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight);
        hoverAnimation.animate(isHovered ? 1 : 0, 0.3f, Easings.QUAD_OUT);
        toggleAnimation.animate(setting.get() ? 1 : 0, 0.25f, Easings.CUBIC_OUT);

        float hoverValue = (float) hoverAnimation.getValue();
        float toggleValue = (float) toggleAnimation.getValue();

        // Фон компонента
        RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), baseHeight, 4, ColorUtils.rgba(20, 20, 20, 200));
        if (isHovered) {
            RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), baseHeight, 4, ColorUtils.rgba(255, 255, 255, (int) (30 * hoverValue)));
        }

        // Название
        float textY = getY() + (baseHeight - ClientFonts.msMedium[15].getFontHeight()) / 2;
        ClientFonts.msMedium[15].drawString(stack, setting.getName(), getX() + sidePadding, textY + 3, ColorUtils.rgba(230, 230, 230, 255));

        // Toggle switch
        float toggleWidth = 28;
        float toggleHeight = 14;
        float toggleX = getX() + getWidth() - toggleWidth - sidePadding;
        float toggleY = getY() + (baseHeight - toggleHeight) / 2;
        float circleRadius = (toggleHeight - 4) / 2;

        // Фон переключателя
        int backgroundColor = ColorUtils.interpolateColor(ColorUtils.rgba(60, 60, 60, 255), ColorUtils.rgba(0, 180, 80, 255), toggleValue);
        RenderUtility.drawRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, toggleHeight / 2, backgroundColor);

        // Плавная позиция кружка
        float knobX = toggleX + 2 + (toggleWidth - toggleHeight) * toggleValue;
        RenderUtility.drawRoundedRect(knobX, toggleY + 2, toggleHeight - 4, toggleHeight - 4, circleRadius, ColorUtils.rgba(255, 255, 255, 255));
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight) && button == 0) {
            setting.set(!setting.get());
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
