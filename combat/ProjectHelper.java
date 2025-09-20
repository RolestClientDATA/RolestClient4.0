package minecraft.rolest.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.SensUtils;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@ModuleRegister(name = "ProjectileHelper", category = Category.Combat)
public class ProjectHelper extends Module {
    private final ModeListSetting weapons = new ModeListSetting("Оружие",
            new BooleanSetting("Лук", true),
            new BooleanSetting("Трезубец", true));
    private final SliderSetting aimRange = new SliderSetting("Дистанция наводки", 30.0f, 10.0f, 50.0f, 1.0f);
    private final SliderSetting aimSpeed = new SliderSetting("Скорость наводки", 15.0f, 1.0f, 30.0f, 0.5f);
    //Tech_System
    private LivingEntity target;
    private Vector2f rotation;
    private boolean aiming;
    private boolean initialAim;
    private boolean wasCharging;

    public ProjectHelper() {
        addSettings(weapons, aimRange, aimSpeed);
    }

    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) {
            reset();
            return;
        }

        boolean charging = ValidItem() && mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;

        if (!charging && wasCharging) {
            reset();
        }
        wasCharging = charging;

        if (!charging) return;

        updateTarget();

        if (target != null) {
            if (!aiming) initialAim = true;
            aim();
        }
    }

   @Subscribe
    private void onMotion(EventMotion event) {
        if (target == null || !ValidItem() || !mc.player.isHandActive() || mc.player.getActiveHand() != Hand.MAIN_HAND)
            return;

        if (rotation != null) {
            event.setYaw(rotation.x);
            event.setPitch(rotation.y);
        }
    }

    private void aim() {
        if (target == null) return;
        Vector3d vec = target.getPositionVec().add(0, 1.2, 0)
                .subtract(mc.player.getEyePosition(1.0F));

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z))));

        if (rotation == null) {
            rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        }

        float yawDelta = wrapDegrees(yawToTarget - rotation.x);
        float pitchDelta = wrapDegrees(pitchToTarget - rotation.y);

        float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0f), aimSpeed.get());
        float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), aimSpeed.get()) / 3f;

        float yaw = rotation.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
        float pitch = clamp(rotation.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);

        float gcd = SensUtils.getGCDValue();
        yaw -= (yaw - rotation.x) % gcd;
        pitch -= (pitch - rotation.y) % gcd;

        rotation = new Vector2f(yaw, pitch);
        aiming = true;
    }


    private Vector3d predictTargetPos() {
        Vector3d pos = target.getPositionVec().add(0, target.getHeight() * 0.5, 0);

        if (target.getMotion().lengthSquared() > 0.001) {
            double distance = mc.player.getDistance(target);
            double projectileSpeed = Bow() ? 3.0 : Trident() ? 2.5 : 2.5;
            double predictionTime = distance / projectileSpeed;
            pos = pos.add(target.getMotion().scale(predictionTime));
        }

        return pos;
    }

    private boolean Bow() {
        ItemStack item = mc.player.getHeldItemMainhand();
        return item.getItem() instanceof BowItem;
    }

    private boolean Trident() {
        ItemStack item = mc.player.getHeldItemMainhand();
        return item.getItem() instanceof TridentItem;
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (ValidTarget(entity)) {
                targets.add((LivingEntity) entity);
            }
        }

        targets.sort(Comparator.comparingDouble(e -> mc.player.getDistanceSq(e)));
        target = targets.isEmpty() ? null : targets.get(0);
    }

    private boolean ValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof ClientPlayerEntity) return false;
        if (!entity.isAlive() || entity.ticksExisted < 10 || entity.isInvulnerable()) return false;
        if (mc.player.getDistanceSq(entity) > aimRange.get() * aimRange.get()) return false;
        if (entity.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        return true;
    }

    private boolean ValidItem() {
        ItemStack item = mc.player.getHeldItemMainhand();
        if (item.isEmpty()) return false;
        if (item.getItem() instanceof BowItem) return weapons.getValueByName("Лук").get();
        if (item.getItem() instanceof TridentItem) return weapons.getValueByName("Трезубец").get();
        return false;
    }

    private void reset() {
        if (mc.player != null) {
            rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        }
        target = null;
        aiming = false;
        initialAim = false;
    }

    public void onEnable() {
        super.onEnable();
        reset();
    }

    public void onDisable() {
        super.onDisable();
        reset();
        mc.gameSettings.keyBindUseItem.setPressed(false);
    }
}