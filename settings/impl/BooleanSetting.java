package minecraft.rolest.modules.settings.impl;


import java.util.function.Supplier;

import minecraft.rolest.modules.settings.Setting;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    public float anim;
    @Override
    public BooleanSetting setVisible(Supplier<Boolean> bool) {
        return (BooleanSetting) super.setVisible(bool);
    }

}