package minecraft.rolest.ui.dropdown.impl;

import lombok.Getter;
import lombok.Setter;
import minecraft.rolest.ui.dropdown.Panel;

@Getter
@Setter
public class Component implements IBuilder {

    public float x, y, width, height;
    public Panel panel;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void keyTyped(char typedChar, int keyCode) {

    }

    public boolean isVisible() {
        return true;
    }

}
