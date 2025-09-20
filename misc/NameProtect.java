package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.StringSetting;
import net.minecraft.client.Minecraft;

@ModuleRegister(name = "NameProtect", desc = "Скрывает имя вас и ваших друзей", category = Category.Misc)
public class NameProtect extends Module {

    public static String fakeName = "";
    public BooleanSetting friend = new BooleanSetting("Скрывать друзей",false);

    public StringSetting name = new StringSetting(
            "Имя",
            "t.me/RolestSoftware",
            "Текст для замены"
    );

    public NameProtect() {
        addSettings(name , friend);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        fakeName = name.get();
    }

    public static String getReplaced(String input) {
        if (Rol.getInstance() != null && Rol.getInstance().getModuleManager().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }
}
