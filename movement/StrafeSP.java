package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.utils.client.TimerUtility;
import minecraft.rolest.utils.player.StrafeMovement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.AxisAlignedBB;

@ModuleRegister(name = "SpookyTime", category = Category.Movement,desc ="спиды спуки дуэль")
public class StrafeSP extends Module {

    private ModeSetting mode = new ModeSetting("Обход", "SpookyTime", "SpookyTime");


    private StrafeMovement strafe = new StrafeMovement();
    private boolean enabled = false;
    public static int stage;
    public double less, stair, moveSpeed;
    public boolean slowDownHop, wasJumping, boosting, restart;
    private int prevSlot = -1;
    public TimerUtility stopWatch = new TimerUtility();
    public TimerUtility racTimer = new TimerUtility();

    public StrafeSP() {
        addSettings(mode);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }


    @Subscribe
    public void onUpdate(EventUpdate e) {
        switch (mode.get()) {
            case "SpookyTime" -> {
                AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.12);
                int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
                boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
                if (canBoost && !mc.player.isOnGround()) {
                    mc.player.jumpMovementFactor = armorstans > 1 ? 1f / (float) armorstans : 0.10f;
                }
            }
        }
    }
}

