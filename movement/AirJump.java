package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.MathUtil;

@ModuleRegister(name = "AirJump", category = Category.Movement, desc = "AirJump на основе AirStuck механики")
public class AirJump extends Module {

    private final SliderSetting jumpPower = new SliderSetting("Мощность", 0.4f, 0.1f, 1.0f, 0.01f);
    private final SliderSetting motionFactor = new SliderSetting("Сопротивление", 0.287f, 0.1f, 0.5f, 0.001f);

    private boolean isJumping = false;
    private int jumpTicks = 0;

    public AirJump() {
        addSettings(jumpPower, motionFactor);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        // Защита от падения
        mc.player.fallDistance = 0;

        // Активация прыжка в воздухе
        if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isOnGround() && !isJumping) {
            isJumping = true;
            jumpTicks = 0;

            // Плавный подъем вместо резкого прыжка
            double currentMotionY = mc.player.getMotion().y;
            double newMotionY = currentMotionY < 0 ? jumpPower.get() : currentMotionY + (jumpPower.get() * 0.5f);

            // Микро-рандомизация как в AirStuck
            newMotionY += (MathUtil.random(-0.005f, 0.005f));
            newMotionY = Math.min(newMotionY, 0.5f); // Ограничение максимума

            mc.player.setMotion(mc.player.getMotion().x, newMotionY, mc.player.getMotion().z);
        }

        // Применяем механику AirStuck после прыжка
        if (isJumping) {
            jumpTicks++;

            // Плавное гашение скорости (прямо как в AirStuck)
            double motionX = mc.player.getMotion().x;
            double motionZ = mc.player.getMotion().z;
            double motionY = mc.player.getMotion().y;

            float reduceFactor = motionFactor.get();
            reduceFactor += (float) (MathUtil.random(-0.002f, 0.002f)); // Случайность

            mc.player.setMotion(
                    motionX * reduceFactor,
                    motionY,
                    motionZ * reduceFactor
            );

            // Авто-сброс через 10 тиков
            if (jumpTicks > 10 || mc.player.isOnGround()) {
                isJumping = false;
            }
        }

        // Сброс при отжатии прыжка
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            isJumping = false;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        isJumping = false;
        jumpTicks = 0;
    }
}