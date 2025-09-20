package minecraft.rolest.ui.dropdown;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import minecraft.rolest.Rol;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.ui.dropdown.components.ModuleComponent;
import minecraft.rolest.ui.dropdown.impl.IBuilder;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.render.gl.Scissor;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Category category;
    private final DropDown dropDown;
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    private List<ModuleComponent> modules = new ArrayList<>();
    private float max = 0;
    private float scroll = 0;
    private float targetScroll = 0;
    private Animation scrollAnimation = new Animation();
    private final List<Float> rowHeights = new ArrayList<>();

    public Panel(Category category, DropDown dropDown) {
        this.category = category;
        this.dropDown = dropDown;

        for (Module module : Rol.getInstance().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(module);
                component.setPanel(this);
                modules.add(component);
            }
        }
        scrollAnimation.animate(0, 0.3f, Easings.CUBIC_OUT);
    }

    public void resetModuleAnimations() {
        for (ModuleComponent module : modules) {
            module.resetAnimation();
        }
    }

    public List<ModuleComponent> getVisibleModules() {
        List<ModuleComponent> visibleModules = new ArrayList<>();
        String searchQuery = dropDown.getSearchText().toLowerCase().replaceAll(" ", "");

        if (searchQuery.isEmpty()) {
            visibleModules.addAll(modules); // При пустом поиске показываем все модули текущей категории
        } else {
            // Поиск только среди модулей текущей категории
            for (ModuleComponent component : modules) {
                String moduleName = component.getModule().getName().toLowerCase().replaceAll(" ", "");
                if (moduleName.contains(searchQuery)) {
                    visibleModules.add(component);
                }
            }
        }
        return visibleModules;
    }

    public void scrollBy(float delta) {
        float visibleHeight = height - 37;
        targetScroll = MathHelper.clamp(targetScroll + delta, -Math.max(0, max - visibleHeight), 0);
        scrollAnimation.animate(targetScroll, 0.3f, Easings.CUBIC_OUT);
    }

    public void updateHeights() {
        float previousMax = max;
        float previousScroll = scroll;

        rowHeights.clear();
        float leftOffsetY = 0;
        float rightOffsetY = 0;

        List<ModuleComponent> visibleModules = getVisibleModules();
        int moduleCount = visibleModules.size();
        int halfCount = (moduleCount + 1) / 2;

        for (int i = 0; i < halfCount; i++) {
            ModuleComponent leftModule = visibleModules.get(i);
            ModuleComponent rightModule = i + halfCount < visibleModules.size() ? visibleModules.get(i + halfCount) : null;

            float leftHeight = 24;
            if (leftModule.isOpen()) {
                leftHeight += leftModule.getSettingsHeight();
            }

            float rightHeight = 24;
            if (rightModule != null && rightModule.isOpen()) {
                rightHeight += rightModule.getSettingsHeight();
            }

            float rowHeight = Math.max(leftHeight, rightHeight != 0 ? rightHeight : 0);
            if (rowHeights.size() <= i) {
                rowHeights.add(rowHeight);
            } else {
                rowHeights.set(i, rowHeight);
            }

            leftOffsetY += leftHeight + 7;
            if (rightModule != null) {
                rightOffsetY += rightHeight + 7;
            }
        }
        max = Math.max(leftOffsetY, rightOffsetY);

        if (Math.abs(previousMax - max) > 50) {
            scroll = 0;
            targetScroll = 0;
            scrollAnimation.setValue(0);
        } else if (previousMax > 0) {
            float scrollRatio = previousScroll / previousMax;
            targetScroll = scrollRatio * max;
            scrollAnimation.animate(targetScroll, 0.3f, Easings.CUBIC_OUT);
        }

        float visibleHeight = height - 37;
        targetScroll = MathHelper.clamp(targetScroll, -max + visibleHeight, 0);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        float xdesc = Minecraft.getInstance().getMainWindow().getWidth() / 2;
        float ydesc = Minecraft.getInstance().getMainWindow().getHeight() / 2;
        String string = null;

        for (ModuleComponent modComponent : this.modules) {
            if (Rol.getInstance().getDropDown().searchCheck(modComponent.getModule().getName()) || !modComponent.isHovered(mouseX, mouseY))
                continue;
            string = modComponent.getModule().getDesc();
            if (string == null || string.isEmpty()) break;
            Scissor.pop();
            Fonts.sfsemibolt.drawCenteredText(stack, string, xdesc - 475, ydesc - 500, ColorUtils.rgb(255, 255, 255), 13.0f);
            Scissor.push();
            break;
        }
        scrollAnimation.update();
        scroll = (float) scrollAnimation.getValue();
        drawComponents(stack, mouseX, mouseY);
    }

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float leftOffsetY = 0;
        float rightOffsetY = 0;
        float columnWidth = (width - 20) / 2;
        float header = 7;
        float visibleHeight = height - header;

        List<ModuleComponent> visibleModules = getVisibleModules();
        int moduleCount = visibleModules.size();
        int halfCount = (moduleCount + 1) / 2;

        float contentHeight = 0;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y + header, width, visibleHeight);

        for (int i = 0; i < halfCount; i++) {
            ModuleComponent leftModule = visibleModules.get(i);
            ModuleComponent rightModule = i + halfCount < visibleModules.size() ? visibleModules.get(i + halfCount) : null;

            leftModule.getHoverAnimation().update();
            leftModule.getAppearAnimation().update();
            if (rightModule != null) {
                rightModule.getHoverAnimation().update();
                rightModule.getAppearAnimation().update();
            }

            float leftHeight = 24;
            if (leftModule.isOpen()) {
                leftHeight += leftModule.getSettingsHeight();
            }

            float rightHeight = 24;
            if (rightModule != null && rightModule.isOpen()) {
                rightHeight += rightModule.getSettingsHeight();
            }

            float leftScrolledY = y + header + leftOffsetY + scroll;
            float rightScrolledY = y + header + rightOffsetY + scroll;

            if (leftScrolledY + leftHeight >= y + header && leftScrolledY <= y + height) {
                leftModule.setX(x + 7);
                leftModule.setY(leftScrolledY);
                leftModule.setWidth(columnWidth);
                leftModule.setHeight(leftHeight);
                leftModule.render(stack, mouseX, mouseY);
            }

            if (rightModule != null && rightScrolledY + rightHeight >= y + header && rightScrolledY <= y + height) {
                rightModule.setX(x + 7 + columnWidth + 7);
                rightModule.setY(rightScrolledY);
                rightModule.setWidth(columnWidth);
                rightModule.setHeight(rightHeight);
                rightModule.render(stack, mouseX, mouseY);
            }

            leftOffsetY += leftHeight + 7;
            if (rightModule != null) {
                rightOffsetY += rightHeight + 7;
            }

            contentHeight = Math.max(leftOffsetY, rightOffsetY);
        }

        max = contentHeight;

        Scissor.unset();
        Scissor.pop();
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (mouseY < y || mouseY > y + height) return;

        float leftOffsetY = 0;
        float rightOffsetY = 0;
        float columnWidth = (width - 20) / 2;
        float header = 7;

        List<ModuleComponent> visibleModules = getVisibleModules();
        int moduleCount = visibleModules.size();
        int halfCount = (moduleCount + 1) / 2;

        boolean clickHandled = false;

        for (int i = 0; i < halfCount; i++) {
            ModuleComponent leftModule = visibleModules.get(i);
            ModuleComponent rightModule = i + halfCount < visibleModules.size() ? visibleModules.get(i + halfCount) : null;

            float leftHeight = 24;
            if (leftModule.isOpen()) {
                leftHeight += leftModule.getSettingsHeight();
            }

            float rightHeight = 24;
            if (rightModule != null && rightModule.isOpen()) {
                rightHeight += rightModule.getSettingsHeight();
            }

            float leftScrolledY = y + header + leftOffsetY + scroll;
            float rightScrolledY = y + header + rightOffsetY + scroll;

            float leftAnimationValue = (float) leftModule.getAppearAnimation().getValue();
            float leftOffsetAnim = (1 - leftAnimationValue) * -50;
            float leftRenderY = leftScrolledY + leftOffsetAnim;

            if (!clickHandled && MathUtil.isHovered(mouseX, mouseY, x + 7, leftRenderY, columnWidth, leftHeight)) {
                leftModule.mouseClick(mouseX, mouseY, button);
                clickHandled = true;
            }

            if (rightModule != null) {
                float rightAnimationValue = (float) rightModule.getAppearAnimation().getValue();
                float rightOffsetAnim = (1 - rightAnimationValue) * -50;
                float rightRenderY = rightScrolledY + rightOffsetAnim;

                if (!clickHandled && MathUtil.isHovered(mouseX, mouseY, x + 7 + columnWidth + 7, rightRenderY, columnWidth, rightHeight)) {
                    rightModule.mouseClick(mouseX, mouseY, button);
                    clickHandled = true;
                }
            }

            leftOffsetY += leftHeight + 7;
            if (rightModule != null) {
                rightOffsetY += rightHeight + 7;
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }
    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
    }
}