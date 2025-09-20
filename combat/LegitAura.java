package minecraft.rolest.modules.impl.combat;

import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.events.EventLivingTick;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "AimAssist", category = Category.Combat,desc ="Легит аура блять")
public class LegitAura extends Module {
    private final SliderSetting fieldOfView = new SliderSetting("Search Angle", 90.0f, 1.0f, 90.0f, 1.0f);
    private final SliderSetting yawSpeed = new SliderSetting("Yaw Speed", 35.0f, 10.0f, 70.0f, 5.0f);
    private final SliderSetting smoothFactor = new SliderSetting("Smoothness", 0.02f, 0.02f, 0.5f, 0.001f);
    public final BooleanSetting enablePitch = new BooleanSetting("Vertical Aiming", false);
    private final SliderSetting pitchSpeed = new SliderSetting("Pitch Speed", 2.0f, 0.0f, 20.0f, 1.0f)
            .setVisible(() -> enablePitch.get());

    private final BooleanSetting ignoreNaked = new BooleanSetting("Ignore Naked Players", true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", true);
    private final BooleanSetting ignoreInvisible = new BooleanSetting("Ignore Invisible", true);

    private PlayerEntity target;
    public LegitAura() {
        this.addSettings(fieldOfView, yawSpeed, pitchSpeed, enablePitch, ignoreNaked, ignoreFriends, ignoreInvisible);
    }

    @Subscribe
    public void onTick(EventLivingTick event) {
        // Поиск цели в радиусе 4 блоков с учетом заданного угла обзора
        this.target = findTarget(Minecraft.getInstance().player, 4.0, fieldOfView.get());
    }

    @Subscribe
    public void onGui(EventDisplay event) {
        if (this.target != null) {
            // Установка ротации на цель
            setRotation(this.target, yawSpeed.get(), pitchSpeed.get(), smoothFactor.get());
        }
    }

    private void setRotation(PlayerEntity target, float yawSpeed, float pitchSpeed, double smoothFactor) {
        Minecraft mc = Minecraft.getInstance();

        // Расчет позиции глаз цели и игрока
        Vector3d targetPos = target.getPositionVec().add(0.0, target.getEyeHeight() / 2.0, 0.0);
        Vector3d playerEyePos = mc.player.getEyePosition(1.0f);

        // Вычисление разницы координат
        double deltaX = targetPos.x - playerEyePos.x;
        double deltaY = targetPos.y - playerEyePos.y;
        double deltaZ = targetPos.z - playerEyePos.z;

        // Расчет углов поворота
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float distanceXZ = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(deltaY, distanceXZ)));

        // Вычисление разницы между текущими и целевыми углами
        float yawDifference = MathHelper.wrapDegrees(targetYaw - mc.player.rotationYaw);
        float pitchDifference = targetPitch - mc.player.rotationPitch - 4;

        // Ограничение скорости вращения
        float clampedYaw = MathHelper.clamp(yawDifference, -yawSpeed, yawSpeed);
        float clampedPitch = MathHelper.clamp(pitchDifference, -pitchSpeed, pitchSpeed);

        // Плавное применение ротации
        mc.player.rotationYaw += clampedYaw * smoothFactor;
        if (enablePitch.get()) {
            mc.player.rotationPitch += clampedPitch * smoothFactor;
        }
    }

    private PlayerEntity findTarget(PlayerEntity player, double range, double fov) {
        for (PlayerEntity potentialTarget : player.world.getEntitiesWithinAABB(
                PlayerEntity.class,
                player.getBoundingBox()
                        .expand(player.getLook(1.0f).scale(range))
                        .grow(1.0, 1.0, 1.0),
                LegitAura::isValidTarget)) {

            // Проверка условий игнорирования
            if (ignoreNaked.get() && isNaked(potentialTarget) ||
                    ignoreFriends.get() && FriendStorage.isFriend(potentialTarget.getScoreboardName()) ||
                    ignoreInvisible.get() && potentialTarget.isInvisible()) {
                continue;
            }

            // Проверка угла обзора и дистанции
            double angle = Math.acos(player.getLook(1.0f)
                    .dotProduct(potentialTarget.getPositionVec()
                            .subtract(player.getEyePosition(1.0f))
                            .normalize())) * 57.29577951308232; // Перевод в градусы
            double distance = player.getEyePosition(1.0f).distanceTo(potentialTarget.getPositionVec());

            if (angle <= fov && distance < range) {
                return potentialTarget;
            }
        }
        return null;
    }

    private boolean isNaked(PlayerEntity player) {
        return player.getTotalArmorValue() == 0;
    }

    private static boolean isValidTarget(PlayerEntity player) {
        Minecraft mc = Minecraft.getInstance();
        return player != mc.player && !player.isSpectator() && player.isAlive();
    }
}