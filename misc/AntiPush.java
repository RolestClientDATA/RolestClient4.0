package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SExplosionPacket;

@Getter
@ModuleRegister(name = "AntiPush", category = Category.Player)
public class AntiPush extends Module {

    public static ModeListSetting modes = new ModeListSetting("Тип",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Взрывы", false),
            new BooleanSetting("Блоки", true));

    public AntiPush() {
        addSettings(modes);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (modes.getValueByName("Взрывы").get()) {
                if (e.getPacket() instanceof SExplosionPacket) {
                    e.cancel();
                }
            }
        }
    }
}
