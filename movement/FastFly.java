package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.utils.player.MoveUtils;

@ModuleRegister(name="FastFly", category= Category.Movement , desc = "быстро летает с /fly")
public class FastFly extends Module {
    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (event instanceof EventUpdate && FastFly.mc.player.abilities.isFlying) {
            MoveUtils.setMotion(3);
            FastFly.mc.player.motion.y = 0.0;
            if (FastFly.mc.gameSettings.keyBindJump.isKeyDown()) {
                FastFly.mc.player.motion.y = 0.5;
                if (FastFly.mc.player.moveForward == 0.0f && !FastFly.mc.gameSettings.keyBindLeft.isKeyDown() && !FastFly.mc.gameSettings.keyBindRight.isKeyDown()) {
                    FastFly.mc.player.motion.y = 0.5;
                }
            }
            if (FastFly.mc.gameSettings.keyBindSneak.isKeyDown()) {
                FastFly.mc.player.motion.y = -0.5;
                if (FastFly.mc.player.moveForward == 0.0f && !FastFly.mc.gameSettings.keyBindLeft.isKeyDown() && !FastFly.mc.gameSettings.keyBindRight.isKeyDown()) {
                    FastFly.mc.player.motion.y = -0.5;



                }
            }
        }
    }
}