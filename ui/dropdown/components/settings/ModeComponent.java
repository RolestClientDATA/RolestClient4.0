package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import net.minecraft.util.ResourceLocation;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.impl.render.WorldTweaks;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;

public class ModeComponent extends Component {

    private final ModeSetting setting;
    private boolean expanded = false;
    private boolean showSelectedMode = true;
    private final Animation expandAnimation = new Animation();
    private final Animation hoverAnimation = new Animation();

    private final float baseHeight = 20;
    private final float itemHeight = 18;
    private final float spacing = 2;
    private final float padding = 4;
    private final float sidePadding = 6;

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(baseHeight);
        expandAnimation.animate(0, 0.3f, Easings.CUBIC_OUT);
        hoverAnimation.animate(0, 0.3f, Easings.QUAD_OUT);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        expandAnimation.update();
        hoverAnimation.update();

        float expandValue = (float) expandAnimation.getValue();
        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight);
        hoverAnimation.animate(isHovered && !expanded ? 1 : 0, 0.3f, Easings.QUAD_OUT);
        float hoverValue = (float) hoverAnimation.getValue();

        // Calculate current height
        float currentHeight = baseHeight;
        float listHeight = 0;
        if (expanded) {
            listHeight = (setting.strings.length * itemHeight + padding * 2) * expandValue;
            currentHeight = baseHeight + listHeight + spacing;
        }

        // Main background (stretches with expansion)
        RenderUtility.drawRoundedRect(
                getX(), getY(), getWidth(), currentHeight,
                4, ColorUtils.rgba(20, 20, 20, 200)
        );

        // Hover effect (only on base area)
        if (isHovered && !expanded) {
            RenderUtility.drawRoundedRect(
                    getX(), getY(), getWidth(), baseHeight,
                    4, ColorUtils.rgba(255, 255, 255, (int) (10 * hoverValue))
            );
        }

        // Setting name
        float textY = getY() + (baseHeight - ClientFonts.msMedium[14].getFontHeight()) / 2;
        String name = setting.getName();
        float maxNameWidth;

        if (showSelectedMode) {
            maxNameWidth = getWidth() - (getWidth() * 0.4f) - sidePadding * 2 - 10;
            float nameWidth = ClientFonts.msMedium[14].getWidth(name);
            if (nameWidth > maxNameWidth) {
                String ellipsis = "...";
                float ellipsisWidth = ClientFonts.msMedium[14].getWidth(ellipsis);
                float targetWidth = maxNameWidth - ellipsisWidth;

                StringBuilder trimmedName = new StringBuilder();
                for (int i = 0; i < name.length(); i++) {
                    String temp = trimmedName.toString() + name.charAt(i);
                    if (ClientFonts.msMedium[14].getWidth(temp) > targetWidth) {
                        break;
                    }
                    trimmedName.append(name.charAt(i));
                }
                name = trimmedName + ellipsis;
            }
        } else {
            maxNameWidth = getWidth() - sidePadding * 2;
        }

        ClientFonts.msMedium[15].drawString(
                stack, name,
                getX() + sidePadding, textY + 2,
                ColorUtils.rgba(230, 230, 230, 255)
        );

        // Selected mode rendering
        if (showSelectedMode) {
            float valueWidth = getWidth() * 0.4f;
            float valueX = getX() + getWidth() - valueWidth - sidePadding;
            float valueY = getY() + (baseHeight - 12) / 2;
            RenderUtility.drawRoundedRect(
                    valueX, valueY, valueWidth, 12,
                    3, ColorUtils.rgba(25, 25, 25, 200)
            );
            String selectedText = setting.get() != null ? setting.get() : "";
            ClientFonts.msMedium[15].drawString(
                    stack, selectedText,
                    valueX + 4, valueY + (12 - ClientFonts.msMedium[13].getFontHeight()) / 2 + 2f,
                    ColorUtils.rgba(200, 200, 200, 255)
            );
        }

        // Arrow icon
        ResourceLocation arrowIcon = new ResourceLocation("eva/images/gui/double-arrows.png");
        float iconSize = 8;
        float iconX = getX() + getWidth() - iconSize - sidePadding;
        float iconY = getY() + (baseHeight - iconSize) / 2;


        if (expanded && setting.strings != null) {
            float listY = getY() + baseHeight + spacing;
            float itemsHeight = (setting.strings.length * itemHeight) * expandValue;
            float adjustedListHeight = itemsHeight + padding * expandValue;

            // Dropdown list background
            if (adjustedListHeight > 0) {
                RenderUtility.drawRoundedRect(
                        getX() + sidePadding, listY, getWidth() - sidePadding * 2, adjustedListHeight,
                        4, ColorUtils.rgba(15, 15, 15, (int) (230 * expandValue))
                );
            }

            // List items
            for (int i = 0; i < setting.strings.length; i++) {
                String mode = setting.strings[i];
                float itemY = listY + padding + i * itemHeight;
                boolean itemHovered = MathUtil.isHovered(mouseX, mouseY, getX() + sidePadding, itemY, getWidth() - sidePadding * 2, itemHeight);

                int themeColor = Rol.getInstance().getModuleManager().getWorldTweaks().isState()
                        ? WorldTweaks.colorFog.get()
                        : Theme.MainColor(1);

                if (mode.equals(setting.get())) {
                    RenderUtility.drawRoundedRect(
                            getX() + sidePadding + 2, itemY - 1, getWidth() - sidePadding * 2 - 4, itemHeight - 2,
                            3, ColorUtils.setAlpha(themeColor, (int) (70 * expandValue))
                    );
                } else if (itemHovered) {
                    RenderUtility.drawRoundedRect(
                            getX() + sidePadding + 2, itemY - 1, getWidth() - sidePadding * 2 - 4, itemHeight - 2,
                            3, ColorUtils.rgba(255, 255, 255, (int) (20 * expandValue))
                    );
                }

                ClientFonts.msMedium[13].drawString(
                        stack, mode,
                        getX() + sidePadding + 4, itemY + (itemHeight - ClientFonts.msMedium[13].getFontHeight()) / 2,
                        ColorUtils.rgba(
                                mode.equals(setting.get()) ? 255 : 180 + (int) (50 * expandValue),
                                mode.equals(setting.get()) ? 255 : 180 + (int) (50 * expandValue),
                                mode.equals(setting.get()) ? 255 : 180 + (int) (50 * expandValue),
                                (int) (255 * expandValue)
                        )
                );
            }
        }

        setHeight(currentHeight);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight)) {
            if (button == 1) { // Right click
                showSelectedMode = !showSelectedMode;
                expanded = !expanded;
                expandAnimation.animate(expanded ? 1 : 0, 0.3f, Easings.CUBIC_OUT);
                return;
            }
        }

        if (expanded && setting.strings != null) {
            float listY = getY() + baseHeight + spacing;
            for (int i = 0; i < setting.strings.length; i++) {
                float itemY = listY + padding + i * itemHeight;
                if (MathUtil.isHovered(mouseX, mouseY, getX() + sidePadding, itemY, getWidth() - sidePadding * 2, itemHeight)) {
                    if (button == 0) { // Left click
                        setting.set(setting.strings[i]);
                        expanded = false;
                        expandAnimation.animate(0, 0.3f, Easings.CUBIC_OUT);
                        return;
                    }
                }
            }
        }

        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting != null && setting.visible.get();
    }
}