package minecraft.rolest.modules.impl.movement;

import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;

@ModuleRegister(name = "AutoSprint", category = Category.Movement,desc ="Ты совсем долбаеб?")
public class AutoSprint extends Module {
    public BooleanSetting saveSprint = new BooleanSetting("Сохранять спринт", false);
    public AutoSprint() {
        addSettings(saveSprint);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
            mc.player.setSprinting(MoveUtils.isMoving());
    }
}
