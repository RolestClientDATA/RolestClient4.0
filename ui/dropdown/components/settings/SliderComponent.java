package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import net.minecraft.util.math.MathHelper;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.impl.render.WorldTweaks;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;

public class SliderComponent extends Component {

    private final SliderSetting setting;
    private final Animation hoverAnimation = new Animation();
    private boolean drag = false;
    private final float baseHeight = 20;
    private final float sidePadding = 6;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        setHeight(baseHeight);
        hoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        hoverAnimation.update();

        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight);
        hoverAnimation.animate(isHovered ? 1 : 0, 0.3f, Easings.QUAD_OUT);
        float hoverValue = (float) hoverAnimation.getValue();

        RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), baseHeight, 4, ColorUtils.rgba(20, 20, 20, 230));
        if (isHovered) {
            RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), baseHeight, 4, ColorUtils.rgba(255, 255, 255, (int) (30 * hoverValue)));
        }

        float textY = getY() + (baseHeight - ClientFonts.msMedium[15].getFontHeight()) / 2;
        ClientFonts.msMedium[15].drawString(stack, setting.getName(), getX() + sidePadding, textY + 2, ColorUtils.rgba(230, 230, 230, 255));

        float sliderWidth = getWidth() * 0.4f;
        float sliderX = getX() + getWidth() - sliderWidth - sidePadding;
        float sliderY = getY() + (baseHeight - 12) / 2;
        RenderUtility.drawRoundedRect(sliderX, sliderY, sliderWidth, 12, 3, ColorUtils.rgba(25, 25, 25, 200));

        float fillWidth = (float) ((setting.get() - setting.min) / (setting.max - setting.min) * (sliderWidth - 4));
        int themeColor = Rol.getInstance().getModuleManager().getWorldTweaks().isState() ? WorldTweaks.colorFog.get() : Theme.MainColor(1);
        RenderUtility.drawRoundedRect(sliderX + 2, sliderY + 2, fillWidth, 8, 2, ColorUtils.setAlpha(Theme.MainColor(1), 100));

        String valueText = String.format("%.1f", setting.get());
        ClientFonts.msMedium[14].drawString(stack, valueText, sliderX + sliderWidth - ClientFonts.msMedium[14].getWidth(valueText) - 4, sliderY + (12 - ClientFonts.msMedium[13].getFontHeight()) / 2 + 2f, ColorUtils.rgba(200, 200, 200, 255));

        if (drag) {
            float newValue = (float) MathHelper.clamp((mouseX - sliderX) / sliderWidth * (setting.max - setting.min) + setting.min, setting.min, setting.max);
            setting.set((float) MathUtil.round(newValue, setting.increment));
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX() + getWidth() - getWidth() * 0.4f - sidePadding, getY() + (baseHeight - 12) / 2, getWidth() * 0.4f, 12) && button == 0) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}