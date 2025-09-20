package minecraft.rolest.modules.settings.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import minecraft.rolest.modules.settings.Setting;

public class ModeListSetting extends Setting<List<BooleanSetting>> {
    public ModeListSetting(String name, BooleanSetting... strings) {
        super(name, Arrays.asList(strings));
    }

    public BooleanSetting getValueByName(String settingName) {
        return get().stream().filter(booleanSetting -> booleanSetting.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }


    public String getNames() {
        List<String> includedOptions = new ArrayList<>();
        for (BooleanSetting option : get()) {
            if (option.get()) {
                includedOptions.add(option.getName());
            }
        }
        return String.join(", ", includedOptions);
    }

    public BooleanSetting get(int index) {
        return get().get(index);
    }

    @Override
    public ModeListSetting setVisible(Supplier<Boolean> bool) {
        return (ModeListSetting) super.setVisible(bool);
    }
}