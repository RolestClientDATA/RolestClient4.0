package minecraft.rolest.modules.impl.render;


import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.SliderSetting;

@ModuleRegister(name = "AspectRatio", category = Category.Render,desc ="растяг экрана")
public class AspectRatio extends Module {
    public SliderSetting width = new SliderSetting("Ширина", 1, 0.2f, 3.5f, 0.1f);
    public AspectRatio() {
        addSettings(width);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}