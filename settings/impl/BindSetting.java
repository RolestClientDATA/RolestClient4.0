package minecraft.rolest.modules.settings.impl;

import java.util.function.Supplier;

import minecraft.rolest.modules.settings.Setting;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BindSetting setVisible(Supplier<Boolean> bool) {
        return (BindSetting) super.setVisible(bool);
    }

}
