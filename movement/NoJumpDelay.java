package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;


@ModuleRegister(name = "NoJumpDelay", category = Category.Movement , desc = "прыгать без заддержки")
public class NoJumpDelay extends Module {

    private final BooleanSetting noPlaceDelay = new BooleanSetting("NoPlaceDelay", true);

    public NoJumpDelay() {
        this.addSettings(noPlaceDelay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;

        if (noPlaceDelay.get()) {
            mc.rightClickDelayTimer = 1;
        }
    }
}
