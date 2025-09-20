package minecraft.rolest.modules.impl.misc;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;

@ModuleRegister(name = "AutoBuy", category = Category.Misc)
public class AutoBuy extends Module {

    public BindSetting setting = new BindSetting("Кнопка открытия", -1);

    public AutoBuy() {
        addSettings(setting);
    }
}
