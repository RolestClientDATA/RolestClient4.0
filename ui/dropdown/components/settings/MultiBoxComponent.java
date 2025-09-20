package minecraft.rolest.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.impl.render.WorldTweaks;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.ui.dropdown.impl.Component;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;

import java.util.List;
import java.util.stream.Collectors;

public class MultiBoxComponent extends Component {

    private final ModeListSetting setting;
    private boolean expanded = false;
    private final Animation expandAnimation = new Animation();
    private final Animation hoverAnimation = new Animation();

    private final float baseHeight = 20;
    private final float itemHeight = 18;
    private final float spacing = 2;
    private final float padding = 4;
    private final float sidePadding = 6;

    public MultiBoxComponent(ModeListSetting setting) {
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

        float currentHeight = baseHeight;
        if (expanded) {
            float listHeight = (setting.get().size() * itemHeight + padding * 2) * expandValue;
            currentHeight = baseHeight + listHeight + spacing;
        }

        RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), currentHeight, 4, ColorUtils.rgba(20, 20, 20, 230));
        if (isHovered && !expanded) {
            RenderUtility.drawRoundedRect(getX(), getY(), getWidth(), baseHeight, 4, ColorUtils.rgba(255, 255, 255, (int) (30 * hoverValue)));
        }

        float textY = getY() + (baseHeight - ClientFonts.msMedium[15].getFontHeight()) / 2;
        String name = setting.getName();
        float maxNameWidth = getWidth() - sidePadding * 2;
        ClientFonts.msMedium[15].drawString(stack, name, getX() + sidePadding, textY + 2, ColorUtils.rgba(230, 230, 230, 255));

        List<String> activeModules = setting.get().stream()
                .filter(s -> s.get())
                .map(s -> s.getName())
                .collect(Collectors.toList());

        float valueWidth = getWidth() * 0.4f;
        float valueX = getX() + getWidth() - valueWidth - sidePadding;
        float valueY = getY() + (baseHeight - 12) / 2;
        RenderUtility.drawRoundedRect(valueX, valueY, valueWidth, 12, 3, ColorUtils.rgba(25, 25, 25, 200));

        String selectedText = activeModules.isEmpty() ? "None" : String.join(", ", activeModules);
        float availableWidth = valueWidth - 8; // 4 пикселя отступа с каждой стороны
        String truncatedText = truncateText(selectedText, availableWidth);

        ClientFonts.msMedium[14].drawString(stack,
                truncatedText,
                valueX + 4,
                valueY + (12 - ClientFonts.msMedium[14].getFontHeight()) / 2 + 2f,
                ColorUtils.rgba(200, 200, 200, 255));

        ResourceLocation arrowIcon = new ResourceLocation("eva/images/gui/double-arrows.png");
        float iconSize = 8;
        float iconX = getX() + getWidth() - iconSize - sidePadding;
        float iconY = getY() + (baseHeight - iconSize) / 2;

        if (expanded) {
            float listHeight = (setting.get().size() * itemHeight + padding * 2) * expandValue;
            float listY = getY() + baseHeight + spacing;
            RenderUtility.drawRoundedRect(getX() + sidePadding, listY, getWidth() - sidePadding * 2, listHeight - 2, 4, ColorUtils.rgba(15, 15, 15, (int) (230 * expandValue)));

            for (int i = 0; i < setting.get().size(); i++) {
                String mode = setting.get().get(i).getName(); // Исправлено: получение имени режима
                if (mode == null || mode.equals("java.lang.String")) {
                    mode = "Mode " + (i + 1); // Защита от некорректных значений
                    System.err.println("Warning: Invalid mode name at index " + i + " for setting: " + setting.getName());
                }
                float itemY = listY + padding + i * itemHeight;
                boolean itemHovered = MathUtil.isHovered(mouseX, mouseY, getX() + sidePadding, itemY, getWidth() - sidePadding * 2, itemHeight);

                int themeColor = Rol.getInstance().getModuleManager().getWorldTweaks().isState() ? WorldTweaks.colorFog.get() : Theme.MainColor(1);

                if (setting.get().get(i).get()) {
                    RenderUtility.drawRoundedRect(getX() + sidePadding + 2, itemY + 1, getWidth() - sidePadding * 2 - 4, itemHeight - 2, 3, ColorUtils.setAlpha(themeColor, (int) (70 * expandValue)));
                } else if (itemHovered) {
                    RenderUtility.drawRoundedRect(getX() + sidePadding + 2, itemY + 1, getWidth() - sidePadding * 2 - 4, itemHeight - 2, 3, ColorUtils.rgba(255, 255, 255, (int) (20 * expandValue)));
                }

                ClientFonts.msMedium[13].drawString(stack, mode, getX() + sidePadding + 4, itemY + (itemHeight - ClientFonts.msMedium[13].getFontHeight()) / 2 + 3f,
                        ColorUtils.rgba(setting.get().get(i).get() ? 255 : 180 + (int) (50 * expandValue),
                                setting.get().get(i).get() ? 255 : 180 + (int) (50 * expandValue),
                                setting.get().get(i).get() ? 255 : 180 + (int) (50 * expandValue),
                                (int) (255 * expandValue)));
            }

            setHeight(baseHeight + listHeight + spacing);
        } else {
            setHeight(baseHeight);
        }
    }

    private String truncateText(String text, float maxWidth) {
        if (text.equals("None")) return text;

        float textWidth = ClientFonts.msMedium[13].getWidth(text);
        if (textWidth <= maxWidth) return text;

        String[] parts = text.split(", ");
        StringBuilder result = new StringBuilder();
        float currentWidth = 0;
        float ellipsisWidth = ClientFonts.msMedium[13].getWidth("...");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            float partWidth = ClientFonts.msMedium[13].getWidth(part);
            float commaWidth = ClientFonts.msMedium[13].getWidth(", ");

            if (currentWidth + partWidth + (i > 0 ? commaWidth : 0) + ellipsisWidth > maxWidth) {
                if (i == 0) {
                    String truncatedPart = part;
                    while (ClientFonts.msMedium[13].getWidth(truncatedPart + "...") > maxWidth && truncatedPart.length() > 1) {
                        truncatedPart = truncatedPart.substring(0, truncatedPart.length() - 1);
                    }
                    return truncatedPart + "...";
                }
                result.append("...");
                break;
            }

            if (i > 0) {
                result.append(", ");
                currentWidth += commaWidth;
            }
            result.append(part);
            currentWidth += partWidth;
        }

        return result.toString();
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), baseHeight) && button == 1) {
            expanded = !expanded;
            expandAnimation.animate(expanded ? 1 : 0, 0.3f, Easings.CUBIC_OUT);
            return;
        }

        if (expanded) {
            float listY = getY() + baseHeight + spacing;
            for (int i = 0; i < setting.get().size(); i++) {
                float itemY = listY + padding + i * itemHeight;
                if (MathUtil.isHovered(mouseX, mouseY, getX() + sidePadding, itemY, getWidth() - sidePadding * 2, itemHeight) && button == 0) {
                    setting.get().get(i).set(!setting.get().get(i).get());
                    return;
                }
            }
        }

        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}