package minecraft.rolest.modules.impl.misc;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;

@ModuleRegister(name = "BetterMinecraft", category = Category.Misc,desc ="улучшение майна")
public class BetterMinecraft extends Module {


    public final BooleanSetting smoothChat = new BooleanSetting("Плавный чат", true);
    public final BooleanSetting smoothTab = new BooleanSetting("Плавный таб", true); // пот
    public final BooleanSetting betterTab = new BooleanSetting("Улучшенный таб", true);
    public final BooleanSetting betterChat = new BooleanSetting("Улучшенный чат", true);

    public BetterMinecraft() {
        addSettings(betterTab,betterChat,smoothChat);
    }
}
