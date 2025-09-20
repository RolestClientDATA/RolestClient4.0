package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.client.TimerUtility;

@ModuleRegister(name = "ElytraBooster", category = Category.Movement)
public class ElytraBooster extends Module {
    public final ModeSetting mode = new ModeSetting("Мод", "Standart", "Standart", "BravoHVH");
    public final SliderSetting boostPower = new SliderSetting("Скорость буста", 1.63f, 1.55f, 1.8f, 0.01f).setVisible(() -> mode.is("Standart"));
    public double boosterSpeed;
    public boolean restart;
    public TimerUtility timerUtility = new TimerUtility();

    public ElytraBooster() {
        addSettings(mode, boostPower);
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket packet && mode.is("BravoHVH")) {
            restart = false;
            boosterSpeed = 0.7;
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mode.is("BravoHVH")) {
            if (!restart) {
                boosterSpeed = 1.5;
                if (timerUtility.isReached(1000)) {
                    restart = true;
                    timerUtility.reset();
                }
            }

            if (restart) boosterSpeed = Math.min(boosterSpeed + 0.05, 1.66800064); // 1.66800064
        } else {
            boosterSpeed = boostPower.get();
        }
    }
}
