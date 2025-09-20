
package minecraft.rolest.modules.impl.combat;

import minecraft.rolest.events.*;
import minecraft.rolest.utils.player.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.MathHelper;
import minecraft.rolest.Rol;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.ui.dropdown.DropDown;
import minecraft.rolest.utils.animations.Animation;
import minecraft.rolest.utils.animations.impl.EaseInOutQuad;
import minecraft.rolest.utils.math.Vector4i;

import minecraft.rolest.utils.projections.ProjectionUtil;
import minecraft.rolest.utils.render.RectUtility;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.math.SensUtils;
import minecraft.rolest.utils.math.StopWatch;
import net.optifine.util.MathUtils;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.platform.GlStateManager.depthMask;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.sin;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static net.minecraft.util.math.MathHelper.*;
@ModuleRegister(
        name = "AttackAura",
        category = Category.Combat,
        desc = "ЕБАТЬ МАТЬ АНСОФТОВ"
)
public class HitAura extends Module {
    @Getter// ХАВХАХ СЕМИБОЛТ БОЛТ АВХЗДА ЛФДЫАД ЛТУПООООООЙЙЙ
    public ModeSetting type = new ModeSetting("Тип", "FunTime", "FunTime", "FunTime-FOV", "Снап", "Легит-Снап", "Плавная","SpookyTime","Нейро","HvH","ReallyWorld");
    public SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3f, 2.5f, 6f, 0.05f);
    final SliderSetting elytraRange = new SliderSetting("Дистанция на элитре", 6f, 0f, 16f, 0.05f);
    final ModeListSetting targets = new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true), new BooleanSetting("Голые", true), new BooleanSetting("Мобы", false), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false), new BooleanSetting("Голые невидимки", true), new BooleanSetting("Невидимки", true)
    );
    public ModeSetting targetesp = new ModeSetting("Тип Наведения", "Клиент", "Клиент", "Ромб", "Кружок","Квадрат","Пенис", "Неотображать");
    final ModeListSetting consider = new ModeListSetting("Учитывать", new BooleanSetting("Хп", true), new BooleanSetting("Броню", true), new BooleanSetting("Дистанцию", true), new BooleanSetting("Баффы", true)
    );
    @Getter
    final ModeListSetting options = new ModeListSetting("Опции", new BooleanSetting("Только криты", true), new BooleanSetting("Ломать щит", true), new BooleanSetting("Отжимать щит", false), new BooleanSetting("Синхронизировать с TPS", false), new BooleanSetting("Фокусировать одну цель", true), new BooleanSetting("Коррекция движения", true), new BooleanSetting("Оптимальная дистанция атаки", false), new BooleanSetting("Автоматический Прыгать", false), new BooleanSetting("Резольвер", false)
    );
    @Getter
    final ModeListSetting moreOptions = new ModeListSetting("Триггеры", new BooleanSetting("Проверка луча", true), new BooleanSetting("Перелетать противника", false), new BooleanSetting("Бить через стены", true), new BooleanSetting("Не бить если кушаешь", true), new BooleanSetting("Не бить если в гуи", true)
    );
    private final ModeSetting sprintmode = new ModeSetting("Мод Бега", "Обычный", "Обычный", "Легитный");
    final SliderSetting elytraForward = new SliderSetting("Значение перелета", 3.5f, 0.5f, 8, 0.05f).setVisible(() -> moreOptions.getValueByName("Перелетать противника").get());
    public BooleanSetting wallBypass = new BooleanSetting("Обход Через Стены", true).setVisible(() -> moreOptions.getValueByName("Бить через стены").get());
    public BooleanSetting smartCrits = new BooleanSetting("Умные криты", false).setVisible(() -> (options.getValueByName("Только криты").get()));
    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Свободный", "Свободный", "Сфокусированный").setVisible(() -> options.getValueByName("Коррекция движения").get());
    @Getter
    private final StopWatch stopWatch = new StopWatch();
    public Vector2f rotateVector = new Vector2f(0, 0);
    @Getter
    @Setter
    private LivingEntity target;
    private Entity selected;
    private static long lastTime = System.currentTimeMillis();
    int ticks = 0;
    boolean isRotated = false;
    boolean canWork = true, tpAuraRule = false;
    final PotionThrower autoPotion;
    private final Animation alpha = new EaseInOutQuad(600, 255);

    float aimDistance() {
        return 0;
    }

    float maxRange() {
        return attackDistance() + (mc.player.isElytraFlying() ? elytraRange.get() : 0) + aimDistance();
    }

    public HitAura(PotionThrower autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(type, attackRange, elytraRange, targets,sprintmode, consider, targetesp, options, moreOptions, elytraForward, wallBypass, smartCrits, correctionType);
    }
    int p;
    @Subscribe
    public void onInput(EventInput eventInput) {
        if (options.getValueByName("Коррекция движения").get() && correctionType.is("Свободный") && canWork) {
            MoveUtils.fixMovement(eventInput, rotateVector.x);
        }
        if (p > 0) {
            eventInput.setForward(0);
            p--;
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!canWork) return;

        if (options.getValueByName("Фокусировать одну цель").get() && (target == null || !isValid(target)) || !options.getValueByName("Фокусировать одну цель").get()) {
            updateTarget();
        }

        if (options.getValueByName("Резольвер").get()) {
            resolvePlayers();
            releaseResolver();
        }

        if (target != null && !(autoPotion.isState() && autoPotion.isActive())) {
            isRotated = false;
            if (shouldPlayerFalling() && (stopWatch.hasTimeElapsed())) {
                ticks = 2;
                tpAuraRule = true;
                updateAttack();
                tpAuraRule = false;
            }
            if (type.is("Снап")) {
                if (ticks > 0f || mc.player.isElytraFlying()) {
                    setRotate();
                    ticks--;
                } else {
                    reset();
                }
            } else if (type.is("Легит-Снап")) {
                if (ticks > 0 || mc.player.isElytraFlying()) {
                    setRotate();
                    ticks--;
                } else {
                    reset();
                }
            } else {
                if (!isRotated) {
                    setRotate();
                }
            }
        } else {
            stopWatch.setLastMS(0);
            reset();
        }
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (target != null) {
            e.setYaw(rotateVector.x);
            e.setPitch(rotateVector.y);
            mc.player.rotationYawHead = rotateVector.x;
            mc.player.renderYawOffset = PlayerUtils.calculateCorrectYawOffset2(rotateVector.x);
            mc.player.rotationPitchHead = rotateVector.y;
        }
    }

    public void setRotate() {
        if (mc.player.isElytraFlying()) {
            smartRotation();
        } else {
            baseRotation();
        }
        if (moreOptions.getValueByName("Не бить если кушаешь").get() && mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT) {
            rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        }
        if (moreOptions.getValueByName("Не бить если в гуи").get()) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof DropDown || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof IngameMenuScreen)) {
                rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            }
        }
    }

    public float attackDistance() {
        if (options.getValueByName("Оптимальная дистанция атаки").get()) {
            if (!mc.player.isSwimming())
                return 3.6f;
            else
                return 3.0f;
        }
        return attackRange.get();
    }


    public void resolvePlayers() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).resolve();
            }
        }
    }

    public void releaseResolver() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).releaseResolver();
            }
        }
    }

    private float getPitchDifference(Vector3d vector3d, Vector3d vector3d2, float f) {
        double d = vector3d2.x - vector3d.x;
        double d2 = vector3d2.y + (double) (target.getHeight() / 2.0f) - (vector3d.y + (double) HitAura.mc.player.getEyeHeight());
        double d3 = vector3d2.z - vector3d.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f2 = (float) (-Math.toDegrees(Math.atan2(d2, d4)));
        return wrapDegrees(f2 - f);
    }

    private float getYawDifference(Vector3d vector3d, Vector3d vector3d2, float f) {
        double d = vector3d2.x - vector3d.x;
        double d2 = vector3d2.z - vector3d.z;
        float f2 = (float) (Math.toDegrees(Math.atan2(d2, d)) - 90.0);
        float f3 = f2 - f;
        return wrapDegrees(f3);
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                targets.add(living);
            }
        }
        if (targets.isEmpty()) {
            target = null;
            return;
        }
        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }
        targets.sort(Comparator.comparingDouble(entity -> MathUtil.entity(entity, consider.getValueByName("Хп").get(), consider.getValueByName("Броню").get(), consider.getValueByName("Дистанцию").get(), maxRange(), consider.getValueByName("Баффы").get())));
        target = targets.get(0);
    }
    float lastPitch;
    private void smartRotation() {
        isRotated = true;
        Vector3d vec3d = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));
        if (mc.player.isElytraFlying()) {
            if (moreOptions.getValueByName("Перелетать противника").get()) {
                Vector3d targetPosition = target.getPositionVec();
                Vector3d scale = target.getForward().normalize().scale(elytraForward.get());
                vec3d = targetPosition.add(scale);
            } else {
                vec3d = MathUtil.getVector(target);
            }
        }
        double vecX = vec3d.x - ((mc.player.isElytraFlying() && moreOptions.getValueByName("Перелетать противника").get()) ? mc.player.getPosX() : 0);
        double vecY = vec3d.y - ((mc.player.isElytraFlying() && moreOptions.getValueByName("Перелетать противника").get()) ? mc.player.getPosY() : 0);
        double vecZ = vec3d.z - ((mc.player.isElytraFlying() && moreOptions.getValueByName("Перелетать противника").get()) ? mc.player.getPosZ() : 0);
        float[] rotations = new float[]{(float) Math.toDegrees(Math.atan2(vecZ, vecX)) - 90.0F, (float) (-Math.toDegrees(Math.atan2(vecY, hypot(vecX, vecZ))))};
        float deltaYaw = wrapDegrees(MathUtil.calculateDelta(rotations[0], rotateVector.x));
        float deltaPitch = MathUtil.calculateDelta(rotations[1], rotateVector.y);
        float limitedYaw = Math.min(Math.max(Math.abs(deltaYaw), 1.0F), 360);
        float limitedPitch = Math.min(Math.max(Math.abs(deltaPitch), 1.0F), 90);
        float finalYaw = rotateVector.x + (deltaYaw > 0.0F ? limitedYaw : -limitedYaw);
        float finalPitch = clamp(rotateVector.y + (deltaPitch > 0.0F ? limitedPitch : -limitedPitch), -90.0F, 90.0F);
        float gcd = SensUtils.getGCDValue();
        finalYaw = finalYaw - (finalYaw - rotateVector.x) % gcd;
        finalPitch = finalPitch - (finalPitch - rotateVector.y) % gcd;
        rotateVector = new Vector2f(finalYaw, finalPitch);
        if (options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = finalYaw;
        }
    }

    private void baseRotation() {
        Vector3d vec = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));
        isRotated = true;
        float yawToTarget = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
        float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));
        float yaw, pitch;
        int roundedYaw = (int) yawDelta;
        switch (type.get()) {
            case "Плавная" -> {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1f), 25);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1f), 5);
                yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f);
                pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -90.0F, 90.0F) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f);
                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;
                rotateVector = new Vector2f(yaw, pitch);
                clampedPitch = clampedYaw;
                lastPitch = clampedPitch;
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }
            case "Легит-Снап" -> {
                Vector3d vector3d16 = HitAura.mc.player.getPositionVec();
                Vector3d vector3d17 = target.getPositionVec();
                float f133 = this.getYawDifference(vector3d16, vector3d17, HitAura.mc.player.rotationYaw);
                float f134 = this.getPitchDifference(vector3d16, vector3d17, HitAura.mc.player.rotationPitch);
                if (Math.abs(f133) <= (32) && Math.abs(f134) <= 40.0f) {
                    float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0f), 90);
                    float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0f), 90);
                    yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                    pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -70.0F, 70.0F);
                    float gcd = SensUtils.getGCDValue();
                    yaw -= (yaw - rotateVector.x) % gcd;
                    pitch -= (pitch - rotateVector.y) % gcd;
                    rotateVector = new Vector2f(yaw, pitch);
                    if (options.getValueByName("Коррекция движения").get()) {
                        mc.player.rotationYawOffset = yaw;
                    }
                    break;
                }
                rotateVector = new Vector2f(HitAura.mc.player.rotationYaw, HitAura.mc.player.rotationPitch);
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = mc.player.rotationYaw;
                }
            }
            case "FunTime-FOV" -> {
                Vector3d vector3d16 = HitAura.mc.player.getPositionVec();
                Vector3d vector3d17 = target.getPositionVec();
                float f133 = this.getYawDifference(vector3d16, vector3d17, HitAura.mc.player.rotationYaw);
                float f134 = this.getPitchDifference(vector3d16, vector3d17, HitAura.mc.player.rotationPitch);
                if (Math.abs(f133) <= (40) && Math.abs(f134) <= 40.0f) {
                    yaw = rotateVector.x + roundedYaw / (float) 1.6 + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                    pitch = clamp(rotateVector.y + pitchDelta / (float) 1.6, -70, 70) + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                    if (!shouldPlayerFalling()) {
                        yaw = rotateVector.x + (mc.player.rotationYaw - rotateVector.x) / (float) 1.6 + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                        pitch = clamp(rotateVector.y + (mc.player.rotationPitch - rotateVector.y) / (float) 1.6, -70, 70) + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                    }
                    float gcd = SensUtils.getGCDValue();
                    yaw -= (yaw - rotateVector.x) % gcd;
                    pitch -= (pitch - rotateVector.y) % gcd;
                    rotateVector = new Vector2f(yaw, pitch);
                    if (options.getValueByName("Коррекция движения").get()) {
                        mc.player.rotationYawOffset = yaw;
                    }
                    break;
                }
                rotateVector = new Vector2f(HitAura.mc.player.rotationYaw, HitAura.mc.player.rotationPitch);
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = HitAura.mc.player.rotationYaw;
                }
            }
            case "Снап" -> {
                yaw = rotateVector.x + yawDelta;
                pitch = clamp(rotateVector.y + pitchDelta, -90, 90);
                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;
                rotateVector = new Vector2f(yaw, pitch);
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }
            case "FunTime" -> {
                float clampedYaw = clamp(Math.abs(yawDelta), 1f, 45f);
                float clampedPitch = clamp(Math.abs(pitchDelta), 1f, 40f);

                float dynamicSpeed = 0.65f + Math.min(clampedYaw + clampedPitch, 60f) / 120f;
                float interpolationSpeed = clamp(dynamicSpeed, 0.65f, 0.95f);

                float gcd = SensUtils.getGCDValue() * (0.8f + (float) Math.random() * 0.1f);
                float yawSign = yawDelta > 0 ? 1f : -1f;
                float pitchSign = pitchDelta > 0 ? 1f : -1f;

                float targetYaw = rotateVector.x + yawSign * clampedYaw;
                float targetPitch = clamp(rotateVector.y + pitchSign * clampedPitch, -90.0f, 90.0f);

                float t = applyInterpolation(interpolationSpeed, InterpolationType.SMOOTH);

                float newYaw = lerp(rotateVector.x, targetYaw, t);
                float newPitch = lerp(rotateVector.y, targetPitch, t * 0.92f);

                float yawJitter = (float) (Math.random() * 0.05f - 0.025f) * gcd;
                float pitchJitter = (float) (Math.random() * 0.05f - 0.025f) * gcd;

                newYaw = Math.round((newYaw + yawJitter) / gcd) * gcd;
                newPitch = Math.round((newPitch + pitchJitter) / gcd) * gcd;

                if (newYaw != rotateVector.x || newPitch != rotateVector.y) {
                    rotateVector = new Vector2f(newYaw, newPitch);
                    yaw = newYaw;
                    pitch = newPitch;

                    if (options.getValueByName("Коррекция движения").get()) {
                        mc.player.rotationYawOffset = yaw;
                    }
                }
        }
            case "SpookyTime" -> {
                float clampedYaw = clamp(Math.abs(yawDelta), 1f, 45f);
                float clampedPitch = clamp(Math.abs(pitchDelta), 1f, 40f);

                float interpolationSpeed = 0.85f;
                float randomness = 0.15f;
                float gcdMultiplier = 0.8f;

                float targetYaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                float targetPitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -90.0f, 90.0f);

                InterpolationType interpType = InterpolationType.LINEAR;

                yaw = customLerp(rotateVector.x, targetYaw, interpolationSpeed, interpType) + ThreadLocalRandom.current().nextFloat(-randomness, randomness);
                pitch = customLerp(rotateVector.y, targetPitch, interpolationSpeed, interpType) + ThreadLocalRandom.current().nextFloat(-randomness, randomness);

                float gcd = SensUtils.getGCDValue() * gcdMultiplier;
                yaw = Math.round(yaw / gcd) * gcd; // Simplified GCD adjustment
                pitch = Math.round(pitch / gcd) * gcd;

                rotateVector = new Vector2f(yaw, pitch);

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }

            case "Нейро" -> {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0.8f), 60f);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0.8f), 75f);

                float randomYaw = ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);
                float randomPitch = ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);

                yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + randomYaw;
                pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -70f, 70f) + randomPitch;

                if (!shouldPlayerFalling()) {
                    float smoothYaw = (mc.player.rotationYaw - rotateVector.x) * 0.5f;
                    float smoothPitch = (mc.player.rotationPitch - rotateVector.y) * 0.5f;

                    yaw = rotateVector.x + smoothYaw + randomYaw;
                    pitch = clamp(rotateVector.y + smoothPitch, -70f, 70f) + randomPitch;
                }

                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;

                rotateVector = new Vector2f(yaw, pitch);

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }
            case "HvH" -> {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1f), 90);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1f), 80);
                yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f);
                pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -90.0F, 90.0F) + ThreadLocalRandom.current().nextFloat(-0.5f, 0.5f);
                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;
                rotateVector = new Vector2f(yaw, pitch);
                clampedPitch = clampedYaw;
                lastPitch = clampedPitch;
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }
            case "ReallyWorld" -> {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0.8f), 65f);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0.8f), 75f);

                float randomYaw = ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);
                float randomPitch = ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);

                yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + randomYaw;
                pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -70f, 70f) + randomPitch;

                if (!shouldPlayerFalling()) {
                    float smoothYaw = (mc.player.rotationYaw - rotateVector.x) * 0.5f;
                    float smoothPitch = (mc.player.rotationPitch - rotateVector.y) * 0.5f;

                    yaw = rotateVector.x + smoothYaw + randomYaw;
                    pitch = clamp(rotateVector.y + smoothPitch, -70f, 70f) + randomPitch;
                }

                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;

                rotateVector = new Vector2f(yaw, pitch);

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }

        }
    }
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float randomRange(float min, float max) {
        return ThreadLocalRandom.current().nextFloat() * (max - min) + min;
    }

    private float applyInterpolation(float t, InterpolationType type) {
        return switch (type) {
            case LINEAR -> t;
            case SMOOTH -> t * t * (3f - 2f * t);             // Smoothstep
            case CUBIC -> t * t * t;                         // Ease In Cubic
            case EASE_OUT -> 1f - (1f - t) * (1f - t);       // Ease Out Quad
        };
    }

    enum InterpolationType {
        LINEAR,
        SMOOTH,
        CUBIC,
        EASE_OUT
    }


    private float normalizeYaw(float yaw) {
        yaw = yaw % 360f; // Wrap to [0, 360]
        if (yaw > 180f) yaw -= 360f; // Convert to [-180, 180]
        else if (yaw < -180f) yaw += 360f;
        return yaw;
    }
    // Кастомная функция интерполяции
    private float customLerp(float current, float target, float factor, InterpolationType type) {
        float t = clamp(factor, 0.0f, 1.0f); // Ограничение фактора
        switch (type) {
            case LINEAR:
                return current + (target - current) * t;
            case CUBIC:
                // Кубическая интерполяция для более плавного перехода
                float tCubic = t * t * (3.0f - 2.0f * t);
                return current + (target - current) * tCubic;
            case EASE_OUT:
                // Затухание для более "естественного" замедления
                float tEase = 1.0f - (float) Math.pow(1.0f - t, 3);
                return current + (target - current) * tEase;
            default:
                return current; // На случай ошибки
        }
    }


    private void updateAttack() {
        selected = MouseUtil.getMouseOver(target, rotateVector.x, rotateVector.y, attackDistance());
        if ((mc.player.getDistanceEyePos(target)) > attackDistance()) {
            return;
        }
        if (moreOptions.getValueByName("Проверка луча").get() && !mc.player.isElytraFlying() && !type.is("Снап") && !type.is("ФантаймБета")) {
            if (selected == null) {
                return;
            }
        }
        if (mc.player.isBlocking() && options.getValueByName("Отжимать щит").get()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }
        if (!moreOptions.getValueByName("Бить через стены").get()) {
            if (!mc.player.canEntityBeSeen(target)) {
                return;
            }
        } else {
            if (wallBypass.get() && !mc.player.canEntityBeSeen(target)) {
                target.getPosition().add(MathUtil.random(-0.15F, 0.15F), target.getBoundingBox().getYSize(), MathUtil.random(-0.15F, 0.15F));
            }
        }
        if (moreOptions.getValueByName("Не бить если кушаешь").get()) {
            if (mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
                return;
            }
        }
        if (moreOptions.getValueByName("Не бить если в гуи").get()) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof DropDown || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof IngameMenuScreen))
                return;
        }
        if (mc.player.serverSprintState && !mc.player.isInWater() && sprintmode.is("Обычный") &&shouldPlayerFalling()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            mc.player.serverSprintState = false;
            mc.player.setSprinting(false);
        }
        boolean isInLiquid = mc.player.isActualySwimming() || mc.player.isSwimming() && mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);
        boolean sprinting = mc.player.isSprinting();
        if (!isInLiquid && sprinting && sprintmode.is("Легитный") && shouldPlayerFalling())  {
            p = 1;
            if (mc.player.serverSprintState) return;
        }
        stopWatch.setLastMS(500);
        mc.playerController.attackEntity(HitAura.mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
        if (target instanceof PlayerEntity player && options.getValueByName("Ломать щит").get()) {
            breakShieldPlayer(player);
        }
    }

    public boolean shouldPlayerFalling() {
        return AttackUtil.isPlayerFalling(options.getValueByName("Только криты").get(), smartCrits.get(), options.getValueByName("Синхронизировать с TPS").get());
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity.ticksExisted < 3) return false;
        if ((mc.player.getDistanceEyePos(entity)) > maxRange()) return false;
        if (entity instanceof PlayerEntity p) {
            if (!entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(p.getGameProfile().getName()))) {
                return false;
            }
            if (FriendStorage.isFriend(p.getName().getString()) && Rol.getInstance().getModuleManager().getNoFriendHurt().isState()) {
                return false;
            }
            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        }
        if (entity instanceof PlayerEntity && !targets.getValueByName("Игроки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые невидимки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && !targets.getValueByName("Невидимки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
            return false;
        }
        if ((entity instanceof MonsterEntity || entity instanceof SlimeEntity || entity instanceof VillagerEntity) && !targets.getValueByName("Мобы").get()) {
            return false;
        }
        if (entity instanceof AnimalEntity && !targets.getValueByName("Животные").get()) {
            return false;
        }
        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtil.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtil.getInstance().getAxeInInventory(true);
            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }
            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    private void reset() {
        mc.player.rotationYawOffset = Integer.MIN_VALUE;
        rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        stopWatch.setLastMS(0);
        target = null;
        mc.timer.timerSpeed = 1;
    }
    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        return Math.max(10.0, 1000.0 / distance) * (size / 30.0) / (fov == 70.0 ? 1.0 : fov / 70.0);
    }
    @Subscribe
    private void onDisplay1(EventDisplay e) {
        double sin;
        Vector3d interpolated;
        float size;
        Vector2f pos;
        int alpha;
        if (e.getType() != EventDisplay.Type.PRE) {
            return;
        }
        if (targetesp.is("Ромб")) {
            if (this.getTarget() != null && this.getTarget() != mc.player) {
                float skoros = 1000;
                sin = Math.sin((double) System.currentTimeMillis() / skoros);
                interpolated = this.getTarget().getPositon(e.getPartialTicks());
                size = (float) this.getScale(interpolated, 10);
                boolean isDamaged = getTarget().hurtTime > 0;
                int color = isDamaged ? 0xf73b3b : Theme.MainColor(0);
                int color1 = isDamaged ? 0xf73b3b : ColorUtils.rgb(111, 111, 111);
                pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.getTarget().getHeight() / 1.95F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                alpha = (int) 255f;
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/target.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha)));
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/target.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
        if (targetesp.is("Квадрат")) {
            if (this.getTarget() != null && this.getTarget() != mc.player) {
                float skoros = 1000;
                sin = Math.sin((double) System.currentTimeMillis() / skoros);
                interpolated = this.getTarget().getPositon(e.getPartialTicks());
                size = (float) this.getScale(interpolated, 10);
                boolean isDamaged = getTarget().hurtTime > 0;
                int color = isDamaged ? 0xf73b3b : Theme.MainColor(0);
                int color1 = isDamaged ? 0xf73b3b : ColorUtils.rgb(111, 111, 111);
                pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.getTarget().getHeight() / 1.95F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                alpha = (int) 255f;
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/target2.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha)));
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/target2.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
        if (targetesp.is("Пенис")) {
            if (this.getTarget() != null && this.getTarget() != mc.player) {
                float skoros = 1000;
                sin = Math.sin((double) System.currentTimeMillis() / skoros);
                interpolated = this.getTarget().getPositon(e.getPartialTicks());
                size = (float) this.getScale(interpolated, 10);
                boolean isDamaged = getTarget().hurtTime > 0;
                int color = isDamaged ? 0xf73b3b : Theme.MainColor(0);
                int color1 = isDamaged ? 0xf73b3b : ColorUtils.rgb(111, 111, 111);
                pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.getTarget().getHeight() / 1.95F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                alpha = (int) 255f;
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/penis.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha), ColorUtils.setAlpha(color1, alpha)));
                RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/penis.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha), ColorUtils.setAlpha(color, alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }



    @Subscribe
    public void onRender(DEngineEvent e) {
        if (targetesp.is("Клиент")) {
            drawSoulsMarker2(e.getMatrix(), e);
        }
        if (targetesp.is("Кружок")) {
            drawSoulsMarker3(e.getMatrix(), e);

        }
    }

    public void drawSoulsMarker3(MatrixStack stack, DEngineEvent e) {
        if (getTarget() != null && getTarget() != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);

            double x = getTarget().getPosX();
            double y = getTarget().getPosY() + (getTarget().getHeight() - 25);
            double z = getTarget().getPosZ();
            double radius = 0.10 + getTarget().getWidth() / 1.7;
            float speed = 3.2f;
            float size = 0.25f;
            int particleCount = 35;
            int color = ColorUtils.multAlpha(Theme.MainColor(0), 1);
            int color2 = ColorUtils.rgb(255, 255, 255);
            int alpha = 1;


            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-camera.getProjectedView().getX(), -camera.getProjectedView().getY(), -camera.getProjectedView().getZ());


            Vector3d interpolated = MathUtils.interpolate(getTarget().getPositionVec(),
                    new Vector3d(getTarget().lastTickPosX, getTarget().lastTickPosY, getTarget().lastTickPosZ),
                    e.getPartialTicks());
            ms.translate(interpolated.x, interpolated.y + getTarget().getHeight() / 2, interpolated.z);


            RectUtility.bindTexture(new ResourceLocation("rolka/images/firefly.png"));


            float time = (System.currentTimeMillis() % 7000) / 1000.0f; // Smooth time progression
            double verticalOffset = Math.sin(time * speed) * 0.3; // Oscillate ±0.3 units vertically


            for (int i = 0; i < particleCount; i++) {
                Quaternion r = camera.getRotation().copy();
                // Calculate angle for each particle to form a circle
                double angle = (2 * Math.PI * i) / particleCount;
                double s = Math.cos(angle) * radius + 0.10; // X offset
                double c = Math.sin(angle) * radius; // Z offset
                double o = verticalOffset; // Y offset (up-down movement)


                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
                float currentSize = size;

                // Translate to particle position
                ms.push();
                ms.translate(s, o, c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);


                buffer.pos(ms.getLast().getMatrix(), 0, -currentSize, 0)
                        .color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput())))
                        .tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -currentSize, -currentSize, 0)
                        .color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput())))
                        .tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -currentSize, 0, 0)
                        .color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput())))
                        .tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0)
                        .color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput())))
                        .tex(1, 0).endVertex();

                buffer.pos(ms.getLast().getMatrix(), 0, -currentSize, 0)
                        .color(ColorUtils.reAlphaInt(color2, (int) (alpha * this.alpha.getOutput())))
                        .tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -currentSize, -currentSize, 0)
                        .color(ColorUtils.reAlphaInt(color2, (int) (alpha * this.alpha.getOutput())))
                        .tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -currentSize, 0, 0)
                        .color(ColorUtils.reAlphaInt(color2, (int) (alpha * this.alpha.getOutput())))
                        .tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0)
                        .color(ColorUtils.reAlphaInt(color2, (int) (alpha * this.alpha.getOutput())))
                        .tex(1, 0).endVertex();

                tessellator.draw();
                ms.pop();
            }


            ms.translate(-interpolated.x, -(interpolated.y + getTarget().getHeight() / 2), -interpolated.z);

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            RenderSystem.depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }
    public void drawSoulsMarker2(MatrixStack stack, DEngineEvent e) {
        if (getTarget() != null && getTarget() != mc.player) {
            stack.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double x = target.getPosX();
            double y = target.getPosY() + target.getHeight() / 2f;
            double z = target.getPosZ();
            double radius = 0.6f;
            float speed = 27;
            float size = 0.32f;
            double distance = 15;
            int lenght = 25;
            int maxAlpha = 255;
            int alphaFactor = 15;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            stack.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathUtil.interpolate(target.getPositionVec(), new Vector3d(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.8f;
            stack.translate(interpolated.x + 0.2f, interpolated.y + 0.5f, interpolated.z);
            mc.getTextureManager().bindTexture(new ResourceLocation("rolka/images/firefly.png"));
            for (int i = 0; i < lenght; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = sin(angle) * radius;
                double c = cos(angle) * radius;
                stack.translate(s, (c), -c);
                stack.translate(-size / 2f, -size / 2f, 0);
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                int color = ColorUtils.getColor(i);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(stack.getLast().getMatrix(), 0, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), 0, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                stack.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                stack.translate(-(s), -(c), (c));
            }
            for (int i = 0; i < lenght; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = sin(angle) * radius;
                double c = cos(angle) * radius;
                stack.translate(-s, s, -c);
                stack.translate(-size / 2f, -size / 2f, 0);
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                int color = ColorUtils.getColor(i);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(stack.getLast().getMatrix(), 0, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), 0, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                stack.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                stack.translate((s), -(s), (c));
            }
            for (int i = 0; i < lenght; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = sin(angle) * radius;
                double c = cos(angle) * radius;
                stack.translate(-(s), -(s), (c));
                stack.translate(-size / 2f, -size / 2f, 0);
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                int color = ColorUtils.getColor(i);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(stack.getLast().getMatrix(), 0, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, -size, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), -size, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(stack.getLast().getMatrix(), 0, 0, 0).color(RenderUtility.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                stack.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                stack.rotate(r);
                stack.translate(size / 2f, size / 2f, 0);
                stack.translate((s), (s), -(c));
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            stack.pop();
        }
    }
}