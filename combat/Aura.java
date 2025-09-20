package minecraft.rolest.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

import minecraft.rolest.events.DEngineEvent;
import minecraft.rolest.events.EventInput;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventUpdate;
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
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.util.MathUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.opengl.GL11;
import minecraft.rolest.Rol;

import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.ui.dropdown.DropDown;
import minecraft.rolest.utils.animations.Animation;
import minecraft.rolest.utils.animations.impl.EaseInOutQuad;
import minecraft.rolest.utils.client.TimerUtility;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.math.SensUtils;
import minecraft.rolest.utils.player.AttackUtil;
import minecraft.rolest.utils.player.InventoryUtil;
import minecraft.rolest.utils.player.MoveUtils;
import minecraft.rolest.utils.player.PlayerUtils;
import minecraft.rolest.utils.render.RectUtility;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.platform.GlStateManager.depthMask;
import static java.lang.Math.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@ModuleRegister(name = "Aura", category = Category.Combat , desc = "обновленная ротация ауры новый вид")
public class Aura extends Module {
    @Getter
    final ModeSetting type = new ModeSetting("Тип", "Modern", "Modern", "Snap360", "ReallyWorld");
    final ModeSetting speedType = new ModeSetting("Скорость ротации", "Быстрая", "Быстрая", "Средняя", "Медленная");

    final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3f, 2.5f, 6f, 0.05f);
    final SliderSetting elytraRange = new SliderSetting("Дистанция на элитре", 6f, 0f, 16f, 0.05f);
    final SliderSetting preRange = new SliderSetting("Дистанция наводки", 0.3f, 0f, 3f, 0.05f).setVisible(() -> !type.is("Snap360"));
    final SliderSetting aliceNoise = new SliderSetting("Сила шума", 1.0F, 0.1F, 20.0F, 0.1F).setVisible(() -> type.is("SpookyTime"));
    final SliderSetting tick = new SliderSetting("Тики", 2f, 1f, 10f, 1f).setVisible(() -> type.is("Snap360"));
    public ModeSetting targetesp = new ModeSetting("Тип Наведения", "Клиент", "Клиент", "Ромб", "Кружок", "Квадрат", "Пенис", "Неотображать");
    final ModeListSetting targets = new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Животные", false),
            new BooleanSetting("Друзья", false),
            new BooleanSetting("Голые невидимки", true),
            new BooleanSetting("Невидимки", true)
    );

    final ModeListSetting consider = new ModeListSetting("Учитывать",
            new BooleanSetting("Хп", true),
            new BooleanSetting("Броню", true),
            new BooleanSetting("Дистанцию", true),
            new BooleanSetting("Баффы", true)
    );

    @Getter
    final ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Отжимать щит", false),
            new BooleanSetting("Ускорять ротацию при атаке", false),
            new BooleanSetting("Синхронизировать с TPS", false),
            new BooleanSetting("Фокусировать одну цель", true),
            new BooleanSetting("Коррекция движения", true),
            new BooleanSetting("Оптимальная дистанция атаки", false),
            new BooleanSetting("Резольвер", true)
    );

    @Getter
    final ModeListSetting moreOptions = new ModeListSetting("Триггеры",
            new BooleanSetting("Проверка луча", true),
            new BooleanSetting("Перелетать противника", true),
            new BooleanSetting("Бить через стены", true),
            new BooleanSetting("Не бить если кушаешь", false),
            new BooleanSetting("Не бить если в гуи", false)
    );

    final ModeSetting clickType = new ModeSetting("Режим кликов", "1.9", "1.8", "1.9");

    final SliderSetting minCPS = new SliderSetting("Мин. CPS", 7f, 1f, 10f, 1f).setVisible(() -> !clickType.is("1.9"));
    final SliderSetting maxCPS = new SliderSetting("Макс. CPS", 10f, 1, 20f, 1f).setVisible(() -> !clickType.is("1.9"));

    final SliderSetting elytraForward = new SliderSetting("Значение перелета", 3.5f, 0.5f, 8, 0.05f).setVisible(() -> moreOptions.getValueByName("Перелетать противника").get());

    public BooleanSetting wallBypass = new BooleanSetting("Wall Bypass", false).setVisible(() -> moreOptions.getValueByName("Бить через стены").get());
    public BooleanSetting noRotate = new BooleanSetting("Наводиться", false).setVisible(() -> (moreOptions.getValueByName("Не бить если кушаешь").get() || moreOptions.getValueByName("Не бить если в гуи").get()));
    public BooleanSetting smartCrits = new BooleanSetting("Умные криты", false).setVisible(() -> (options.getValueByName("Только криты").get()));
    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный").setVisible(() -> options.getValueByName("Коррекция движения").get());
    final ModeSetting critType = new ModeSetting("Крит хелпер", "None", "None", "Matrix", "NCP", "NCP+", "Grim");
    public ModeSetting sprints = new ModeSetting("Up-Sprint", "Grim", "Grim", "legit");
    @Getter
    private final StopWatch stopWatch = new StopWatch();
    public Vector2f rotateVector = new Vector2f(0, 0);
    @Getter
    @Setter
    private LivingEntity target;
    private static long lastTime = System.currentTimeMillis();
    private final Animation alpha = new EaseInOutQuad(600, 255);
    private Entity selected;
    private final TimerUtility timerUtility = new TimerUtility();


    int ticks = 0;
    boolean isRotated = false;
    boolean canWork = true, tpAuraRule = false;
    public Vector2f rotate = new Vector2f(0.0f, 0.0f);
    public Random random;

    final PotionThrower autoPotion;
    private Vector3d vector3d;

    float aimDistance() {
        return (!type.is("Резкая") ? preRange.get() : 0);
    }

    float maxRange() {
        return attackDistance() + (mc.player.isElytraFlying() ? elytraRange.get() : 0) + aimDistance();
    }

    public Aura(PotionThrower autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(type, attackRange,aliceNoise, preRange, targetesp, elytraRange, tick, targets, options, moreOptions, correctionType, elytraForward, wallBypass, smartCrits, noRotate, critType, sprints);
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (options.getValueByName("Коррекция движения").get() && correctionType.is("Незаметный") && canWork) {
            MoveUtils.fixMovement(eventInput, rotateVector.x);
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

        if (target != null && mc.player.getDistance(target) <= attackDistance()) {
            improvedCritHelper();
        }

        if (type.is("Snap")) {
            if (ticks > 0) {
                baseRotation(180, 90);
                ticks--;
            } else {
                reset();
            }
        } else {
            if (!isRotated) {
                baseRotation(80, 35);
            }
        }

        if (target != null) {

            float rotateSpeedYaw = (speedType.is("Средняя") ? (type.is("Snap360") ? 115 : 180) : 40);
            float rotateSpeedPitch = (speedType.is("Средняя") ? (type.is("Snap360") ? 65 : 90) : 35);
            isRotated = false;
            int minCPSValue = minCPS.get().intValue();
            int maxCPSValue = maxCPS.get().intValue();

            if (minCPSValue > maxCPSValue) {
                maxCPSValue = minCPSValue;
            }

            int minMS = 1000 / maxCPSValue;
            int maxMS = 1000 / minCPSValue;

            Random random = new Random();
            int randomMS = random.nextInt(maxMS - minMS + 1) + minMS;
            if (shouldPlayerFalling()) {
                timerUtility.setLastMS(1);
                ticks = tick.get().intValue();
                tpAuraRule = true;
                updateAttack();
                tpAuraRule = false;

                ticks = tick.get().intValue();
                tpAuraRule = true;
                updateAttack();
                tpAuraRule = false;
            }
            if (type.is("Snap360")) {
                if (ticks > 0 || mc.player.isElytraFlying()) {
                    adaptiveAim(); // Включаем адаптивный аим перед установкой ротации
                    setRotate(rotateSpeedYaw, rotateSpeedPitch);
                    ticks--;
                } else {
                    reset();
                }
            } else {
                if (!isRotated) {
                    adaptiveAim(); // Включаем адаптивный аим перед установкой ротации
                    setRotate(rotateSpeedYaw, rotateSpeedPitch);
                }
            }

        } else {
            int minCPSValue = minCPS.get().intValue();
            int maxCPSValue = maxCPS.get().intValue();

            if (minCPSValue > maxCPSValue) {
                maxCPSValue = minCPSValue;
            }

            int minMS = 1000 / maxCPSValue;
            int maxMS = 1000 / minCPSValue;

            Random random = new Random();
            int randomMS = random.nextInt(maxMS - minMS + 1) + minMS;
            timerUtility.setLastMS(1);
            reset();
        }
        if (target != null && isRotated && mc.player.getDistanceEyePos(target) <= attackDistance()) {
            improvedCritHelper();
        }
    }


    @Subscribe
    private void onWalking(EventMotion e) {
        if (target == null || !canWork) return;

        float yaw = rotateVector.x;
        float pitch = rotateVector.y;

        e.setYaw(yaw);
        e.setPitch(pitch);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = PlayerUtils.calculateCorrectYawOffset(yaw);
        mc.player.rotationPitchHead = pitch;

    }


    public void setRotate(float yawSpeed, float pitchSpeed) {
        if (mc.player.isElytraFlying() || speedType.is("Быстрая") && !type.is("Snap360")) {
            smartRotation();
        } else {
            baseRotation(yawSpeed, pitchSpeed);
        }

        if (moreOptions.getValueByName("Не бить если кушаешь").get() && mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && !noRotate.get()) {
            rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        }

        if (moreOptions.getValueByName("Не бить если кушаешь").get() && mc.player.isHandActive() && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && !noRotate.get()) {
            rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        }

        if (moreOptions.getValueByName("Не бить если в гуи").get() && !noRotate.get()) {
            if (mc.currentScreen != null && !(mc.currentScreen instanceof DropDown || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof IngameMenuScreen)) {
                rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            }
        }
    }

    public float attackDistance() {
        if (options.getValueByName("Оптимальная дистанция атаки").get() && !Rol.getInstance().getModuleManager().getFullBright().isState()) {
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

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!this.isValid(living)) continue;

            targets.add(living);
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

    float lastYaw, lastPitch;

    private void smartRotation() {
        isRotated = true;
        Vector3d vec3d = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));

        if (mc.player.isElytraFlying()) {
            smoothElytraRotation(); // Обрабатываем плавный поворот при полете

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
        float[] rotations = new float[]{(float) Math.toDegrees(Math.atan2(vecZ, vecX)) - 90.0F, (float) (-Math.toDegrees(Math.atan2(vecY, Math.hypot(vecX, vecZ))))};
        float deltaYaw = MathHelper.wrapDegrees(MathUtil.calculateDelta(rotations[0], rotateVector.x));
        float deltaPitch = MathUtil.calculateDelta(rotations[1], rotateVector.y);
        float limitedYaw = Math.min(Math.max(Math.abs(deltaYaw), 1.0F), 360);
        float limitedPitch = Math.min(Math.max(Math.abs(deltaPitch), 1.0F), 90);
        float finalYaw = rotateVector.x + (deltaYaw > 0.0F ? limitedYaw : -limitedYaw);
        float finalPitch = MathHelper.clamp(rotateVector.y + (deltaPitch > 0.0F ? limitedPitch : -limitedPitch), -90.0F, 90.0F);
        float gcd = SensUtils.getGCDValue();
        finalYaw = finalYaw - (finalYaw - rotateVector.x) % gcd;
        finalPitch = finalPitch - (finalPitch - rotateVector.y) % gcd;

        rotateVector = new Vector2f(finalYaw, finalPitch);

        if (options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = finalYaw;
        }


    }

    private void baseRotation(float rotationYawSpeed, float rotationPitchSpeed) {
        Vector3d vec = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));
        isRotated = true;

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
        float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));
        int roundYawDelta = (int) Math.abs(yawDelta);
        int roundPitchDelta = (int) Math.abs(pitchDelta);
        float yaw, pitch;


        switch (type.get()) {
            //  case "Old" -> {
            //      float clampedYaw = Math.min(Math.max(roundYawDelta, 1.0f), rotationYawSpeed);
            //      float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta) * 0.33f, 1.0f), rotationPitchSpeed);

            //      if (Math.abs(clampedYaw - lastYaw) <= 3.0f) {
            //          clampedYaw = lastYaw + 3.1f;
            //      }

            //      yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
            //      pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);

            //      float gcd = SensUtil.getGCDValue();
            //      yaw -= (yaw - rotateVector.x) % gcd;
            //      pitch -= (pitch - rotateVector.y) % gcd;

            //      rotateVector = new Vector2f(yaw, pitch);

            //      lastYaw = clampedYaw;
            //      lastPitch = clampedPitch;

            //      if (options.is("Коррекция движения").get()) {
            //          mc.player.rotationYawOffset = yaw;
            //      }
            //  }

            case "ReallyWorld" -> {
                float reactionTime = 0.03f + random.nextFloat() * 0.02f; // Реалистичная задержка реакции

                float maxYawSpeed = Math.max(2.0f, rotationYawSpeed * 0.85f);
                float maxPitchSpeed = Math.max(1.5f, rotationPitchSpeed * 0.75f);

                float clampedYaw = MathHelper.clamp(yawDelta + (random.nextFloat() * 0.2f - 0.1f), -maxYawSpeed, maxYawSpeed);
                float clampedPitch = MathHelper.clamp(pitchDelta * 0.3f + (random.nextFloat() * 0.1f - 0.05f), -maxPitchSpeed, maxPitchSpeed);

                if (Math.abs(clampedYaw - lastYaw) <= 2.5f) {
                    clampedYaw = lastYaw + (random.nextFloat() * 0.5f - 0.25f);
                }

                yaw = rotateVector.x + clampedYaw;
                pitch = MathHelper.clamp(rotateVector.y + clampedPitch, -89.0F, 89.0F);

                yaw = MathHelper.lerp(reactionTime, rotateVector.x, yaw);
                pitch = MathHelper.lerp(reactionTime, rotateVector.y, pitch);

                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;

                yaw += (random.nextFloat() * 0.4f - 0.2f);
                pitch += (random.nextFloat() * 0.3f - 0.15f);

                double targetSpeed = target.getMotion().length();
                double missChance = targetSpeed > 0.15 ? 0.07 : 0.03;
                if (random.nextDouble() < missChance) {
                    return;
                }
                long currentTime = System.currentTimeMillis();
                int minCPSValue = minCPS.get().intValue();
                int maxCPSValue = maxCPS.get().intValue();
                int minMS = 1000 / maxCPSValue;
                int maxMS = 1000 / minCPSValue;
                int attackDelay = random.nextInt(maxMS - minMS + 1) + minMS;

                if (currentTime - lastAttackTime < attackDelay) {
                    return;
                }

                if (mc.player.isOnGround()) {
                    double stopChance = 0.85;
                    if (random.nextDouble() < stopChance) {
                        mc.player.setMotion(mc.player.getMotion().mul(0.1, 1, 0.1)); // Минимальное движение
                    } else {
                        mc.player.setMotion(mc.player.getMotion().mul(0.3 + random.nextDouble() * 0.2, 1, 0.3 + random.nextDouble() * 0.2));
                    }
                }

                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(Hand.MAIN_HAND);
                lastAttackTime = currentTime;

                mc.player.rotationYawHead = yaw;
                mc.player.renderYawOffset = PlayerUtils.calculateCorrectYawOffset(yaw);
                mc.player.rotationPitchHead = pitch;

                rotateVector = new Vector2f(yaw, pitch);

                lastYaw = clampedYaw;
                lastPitch = clampedPitch;

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }

            case "Modern" -> {
                float clampedYaw = Math.min(Math.max(roundYawDelta + (float) (Math.random() * 0.2 - 0.1), 1.0f), rotationYawSpeed);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta) * 0.33f + (float) (Math.random() * 0.1 - 0.05), 1.0f), rotationPitchSpeed);

                if (Math.abs(clampedYaw - lastYaw) <= 3.0f) {
                    clampedYaw = lastYaw + (float) (Math.random() * 0.5 - 0.25);
                }

                yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);

                float reactionTime = 0.05f; // Время реакции человека
                yaw = (yaw - rotateVector.x) * reactionTime + rotateVector.x;
                pitch = (pitch - rotateVector.y) * reactionTime + rotateVector.y;

                float gcd = SensUtils.getGCDValue();
                yaw -= (yaw - rotateVector.x) % gcd;
                pitch -= (pitch - rotateVector.y) % gcd;

                yaw += (Math.random() * 2 - 1) * 0.5;
                pitch += (Math.random() * 2 - 1) * 0.5;

                if (mc.player.isOnGround() && mc.player.fallDistance > 0.0f) { // Условие критов
                    mc.player.motion.y -= 0.08f; // Псевдокритический прыжок
                }
                mc.player.attackTargetEntityWithCurrentItem(target); // Гарантированный удар

                mc.player.rotationYawHead = yaw;
                mc.player.renderYawOffset = PlayerUtils.calculateCorrectYawOffset(yaw);
                mc.player.rotationPitchHead = pitch;

                rotateVector = new Vector2f(yaw, pitch);

                lastYaw = clampedYaw;
                lastPitch = clampedPitch;

                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset = yaw;
                }
            }

            case "Snap360" -> {
                float speed = 2f; // Скорость поворота (чем выше, тем плавнее)
                float jitterYaw = (float) (Math.random() * 1.5 - 0.75); // ±0.75 градуса
                float jitterPitch = (float) (Math.random() * 1.0 - 0.5); // ±0.5 градуса

                // 10% шанс "промаха", чтобы не бить каждую миллисекунду
                boolean shouldMiss = Math.random() < 0.10;
                if (shouldMiss) {
                    jitterYaw += (Math.random() * 8 - 4); // ±4 градуса
                }

                // Текущие углы поворота
                float yaw3 = rotateVector.x;
                float pitch3 = rotateVector.y;

                // Плавное движение к цели (без резкого поворота)
                yaw3 += (yawDelta / speed) + jitterYaw;
                pitch3 = clamp(pitch3 + (pitchDelta / speed) + jitterPitch, -90, 360);

                // Обновляем угол поворота (гладко)
                rotateVector.x += (yaw3 - rotateVector.x) * 0.9f; // Интерполяция поворота
                rotateVector.y += (pitch3 - rotateVector.y) * 0.9f;

                // Коррекция движения, чтобы не было резкого торможения
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset += (rotateVector.x - mc.player.rotationYawOffset) + 0.55f;
                }
            }
            case "SpookyTime" -> {
                // Параметры для настройки
                float speed = 3.0f; // Скорость поворота (2.0 - резче, 4.0 - плавнее)
                float jitterMaxYaw = 0.6f; // Максимальный джиттер по yaw (±0.6°)
                float jitterMaxPitch = 0.4f; // Максимальный джиттер по pitch (±0.4°)
                float missChance = 0.08f; // 8% шанс небольшого промаха
                float missJitterYaw = 3.0f; // Дополнительный джиттер при промахе (±3°)

                // Вычисляем случайный джиттер
                float jitterYaw = (float) (Math.random() * jitterMaxYaw * 2 - jitterMaxYaw);
                float jitterPitch = (float) (Math.random() * jitterMaxPitch * 2 - jitterMaxPitch);

                // Случайный промах (8% вероятность)
                if (Math.random() < missChance) {
                    jitterYaw += (float) (Math.random() * missJitterYaw * 2 - missJitterYaw);
                }

                // Текущие углы и целевые дельты (предполසколы предполагаются)
                float currentYaw = mc.player.rotationYaw;
                float currentPitch = mc.player.rotationPitch;
                float targetYaw = yawDelta + currentYaw; // Целевой yaw (рассчитан ранее)
                float targetPitch = pitchDelta + currentPitch; // Целевой pitch (рассчитан ранее)

                // Плавная интерполяция к цели
                float newYaw = currentYaw + (targetYaw - currentYaw) / speed + jitterYaw;
                float newPitch = currentPitch + (targetPitch - currentPitch) / speed + jitterPitch;

                // Ограничиваем pitch (-89 до 89, чтобы избежать странных значений)
                newPitch = clamp(newPitch, -89.0f, 89.0f);

                // Применяем интерполяцию для большей плавности
//                newYaw = lerp(currentYaw, newYaw, 0.85f);
//                newPitch = lerp(currentPitch, newPitch, 0.85f);

                // Обновляем углы игрока
                mc.player.rotationYaw = newYaw;
                mc.player.rotationPitch = newPitch;

                // Коррекция движения (если включена)
                if (options.getValueByName("Коррекция движения").get()) {
                    mc.player.rotationYawOffset += (newYaw - mc.player.rotationYawOffset) * 0.6f;
                }
            }
        }
    }



    public void improvedCritHelper() {
        if (target == null) return;

        ItemStack mainHandItem = mc.player.getHeldItemMainhand();
        ItemStack offHandItem = mc.player.getHeldItemOffhand();
        Item mainItem = mainHandItem.getItem();
        Item offItem = offHandItem.getItem();

        if (!(mainItem instanceof AxeItem || mainItem instanceof SwordItem)) return;
        if (!mc.player.isPotionActive(Effects.HASTE)) return;

        // Бонус к скорости атаки (учет эффектов)
        float attackSpeedBonus = 1.0f;
        if (mc.player.isPotionActive(Effects.HASTE)) {
            attackSpeedBonus += 0.2f * (mc.player.getActivePotionEffect(Effects.HASTE).getAmplifier() + 1);
        }
        if (mc.player.isPotionActive(Effects.WEAKNESS)) {
            attackSpeedBonus -= 0.3f * (mc.player.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1);
        }

        // Проверка предметов в левой руке
        if (offItem instanceof ShieldItem) {
            attackSpeedBonus -= 0.1f; // Щит немного замедляет атаку
        } else if (offItem instanceof FireworkRocketItem) {
            attackSpeedBonus += 0.1f; // Возможное ускорение при ракетах
        } else if (offItem == Items.PLAYER_HEAD) {
            attackSpeedBonus += 0.3f; // Если в левой руке голова игрока (шар), даем бонус
        }

        // Проверка на критическую атаку
        boolean isJumpingCrit = mc.player.getMotion().y > 0.2 && mc.player.fallDistance == 0;
        boolean isFalling = mc.player.fallDistance > 0.2f && !mc.player.isOnGround() && !mc.player.isInWater();
        boolean isElytraCrit = mc.player.isElytraFlying() && mc.player.getMotion().y < -0.2 && mc.player.getMotion().length() > 0.5;
        boolean isFireworkBoosting = mc.player.getMotion().length() > 1.2;

        if (!isFalling && !isElytraCrit && !isFireworkBoosting) return;

        // Стабилизация при атаке в полете
        if (mc.player.isElytraFlying()) {
            mc.player.setMotion(mc.player.getMotion().x, -0.08, mc.player.getMotion().z);
        } else {
            mc.player.setMotion(mc.player.getMotion().x, mc.player.getMotion().y - 0.08, mc.player.getMotion().z);
        }

        // Вычисление скорости атаки
        float baseAttackSpeed = mainItem instanceof AxeItem ? 1.0f : 1.6f;
        float actualAttackSpeed = baseAttackSpeed * attackSpeedBonus;

        // Проверка тайминга атаки топором
        if (mainItem instanceof AxeItem) {
            if (mc.player.getCooledAttackStrength(0) < actualAttackSpeed / 4.0f) return;
        } else {
            if (mc.player.getCooledAttackStrength(0) < 1.0f) return;
        }

        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
    }


    private void improvedElytraCrits() {
        if (target == null || !mc.player.isElytraFlying()) return;

        // Проверка, что скорость достаточная для крита
        boolean isFastEnough = mc.player.getMotion().length() > 0.8;
        boolean isFalling = mc.player.getMotion().y < -0.2;

        if (!isFastEnough || !isFalling) return;

        // Имитация фиксации движения перед атакой для точного крита
        mc.player.setMotion(mc.player.getMotion().x * 0.95, mc.player.getMotion().y * 0.85, mc.player.getMotion().z * 0.95);

        if (mc.player.getCooledAttackStrength(0) < 1.0f) return; // Проверка отката удара

        // Гарантированная атака
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
    }


    private void smoothElytraRotation() {
        if (target == null || !mc.player.isElytraFlying()) return;

        Vector3d targetPos = target.getPositionVec();
        Vector3d playerPos = mc.player.getPositionVec();

        float targetYaw = (float) Math.toDegrees(Math.atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(targetPos.y - playerPos.y, mc.player.getMotion().length()));

        float yawSpeed = 5.0f;
        float pitchSpeed = 3.0f;

        rotateVector = new Vector2f(
                rotateVector.x + MathHelper.clamp(targetYaw - rotateVector.x, -yawSpeed, yawSpeed),
                rotateVector.y + MathHelper.clamp(targetPitch - rotateVector.y, -pitchSpeed, pitchSpeed)
        );

        mc.player.rotationYawHead = rotateVector.x;
        mc.player.rotationPitchHead = rotateVector.y;
    }


    private void adaptiveAim() {
        if (target == null) return;

        double distance = mc.player.getDistance(target);
        double adaptiveFactor = MathHelper.clamp(distance / 6.0, 0.5, 1.2); // Чем ближе, тем быстрее прицел

        float yawSpeed = (float) (7.0 * adaptiveFactor);
        float pitchSpeed = (float) (4.0 * adaptiveFactor);

        Vector3d targetPos = target.getPositionVec();
        Vector3d playerPos = mc.player.getPositionVec();

        float targetYaw = (float) Math.toDegrees(Math.atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(targetPos.y - playerPos.y, distance));

        rotateVector = new Vector2f(
                rotateVector.x + MathHelper.clamp(targetYaw - rotateVector.x, -yawSpeed, yawSpeed),
                rotateVector.y + MathHelper.clamp(targetPitch - rotateVector.y, -pitchSpeed, pitchSpeed)
        );

        mc.player.rotationYawHead = rotateVector.x;
        mc.player.rotationPitchHead = rotateVector.y;
    }

    private boolean antiFlagAttack() {
        if (target == null) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < calculateFinalDelay()) return false;

        float randomYaw = (float) (Math.random() * 1.5 - 0.75);
        float randomPitch = (float) (Math.random() * 1.0 - 0.5);

        mc.player.rotationYaw += randomYaw;
        mc.player.rotationPitch += randomPitch;

        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        lastAttackTime = currentTime;
        return false;
    }

    private long lastAttackTime = 0; // Хранит время последней атаки

    private int calculateRandomDelay() {
        int minCPSValue = minCPS.get().intValue();
        int maxCPSValue = maxCPS.get().intValue();

        if (minCPSValue > maxCPSValue) {
            maxCPSValue = minCPSValue;
        }

        int minMS = Math.round((1000f / maxCPSValue) * getEffectModifier()); // Учитываем эффекты
        int maxMS = Math.round((1000f / minCPSValue) * getEffectModifier());

        Random random = new Random();
        return random.nextInt(maxMS - minMS + 1) + minMS;
    }
    // Метод для получения задержки в зависимости от типа предмета

    private int getItemSpecificDelay() {
        ItemStack heldItem = mc.player.getHeldItemMainhand();
        Item item = heldItem.getItem();

        if (item instanceof AxeItem) {
            int baseDelay = 800;
            // Динамическая корректировка для эффектов
            if (mc.player.isPotionActive(Effects.HASTE)) {
                baseDelay -= 200 * (mc.player.getActivePotionEffect(Effects.HASTE).getAmplifier() + 1);
            }
            if (mc.player.isPotionActive(Effects.WEAKNESS)) {
                baseDelay += 200 * (mc.player.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1);
            }
            return Math.max(baseDelay, 200); // Минимальная задержка 200 мс
        }

        // Устанавливаем задержку для различных типов предметов
        if (item instanceof SwordItem) {
            return 550; // 550 мс для меча
        } else if (item instanceof AxeItem) {
            return 800; // 800 мс для топора
        } else if (item instanceof PickaxeItem) {
            return 700; // 700 мс для кирки
        } else if (item instanceof ShovelItem) {
            return 750; // 750 мс для лопаты
        } else if (item instanceof HoeItem) {
            // Проверяем конкретные виды мотыги
            if (item == Items.WOODEN_HOE) {
                return 650; // 650 мс для деревянной мотыги
            } else if (item == Items.STONE_HOE) {
                return 600; // 600 мс для каменной мотыги
            } else if (item == Items.IRON_HOE) {
                return 550; // 550 мс для железной мотыги
            } else if (item == Items.GOLDEN_HOE) {
                return 500; // 500 мс для золотой мотыги
            } else if (item == Items.DIAMOND_HOE) {
                return 450; // 450 мс для алмазной мотыги
            } else if (item == Items.NETHERITE_HOE) {
                return 400; // 400 мс для незеритовой мотыги
            } else {
                return 700; // По умолчанию для неизвестных мотыг
            }
        } else if (item instanceof ToolItem) {
            return 850; // 850 мс для других инструментов
        } else if (item instanceof BlockItem) {
            return 900; // 900 мс для блоков
        } else {
            return 500; // 500 мс по умолчанию (например, для рук)
        }
    }

    // Метод для расчета влияния эффектов на скорость атаки
    private float getEffectModifier() {
        float modifier = 1.0f;
        ItemStack heldItem = mc.player.getHeldItemMainhand();
        Item item = heldItem.getItem();

        // Учитываем эффект Mining Fatigue (замедление)
        if (mc.player.isPotionActive(Effects.MINING_FATIGUE)) {
            int amplifier = mc.player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier();
            modifier += (amplifier + 1) * 0.45f;
        }

        // Уменьшаем КУЛДАУН ТОЛЬКО ДЛЯ УДАРА (не трогаем прыжки)
        if (mc.player.isPotionActive(Effects.HASTE)) {
            int amplifier = mc.player.getActivePotionEffect(Effects.HASTE).getAmplifier();
            if (amplifier >= 1) {
                modifier -= (amplifier * 0.25f);
            }
        }

        return Math.max(modifier, 0.2f); // Ограничение минимального значения (чтобы атака не стала мгновенной)
    }

    // Метод для расчета итоговой задержки
    private int calculateFinalDelay() {
        int baseDelay = Math.max(calculateRandomDelay(), getItemSpecificDelay());
        float effectModifier = getEffectModifier(); // Используем getEffectModifier()

        int minDelay = 200; // Минимальная задержка атаки (изменяй для баланса)
        return Math.max(Math.round(baseDelay * effectModifier), minDelay);
    }

    private void updateAttack() {
        long currentTime = System.currentTimeMillis();

        int delay = calculateFinalDelay();

        if (currentTime - lastAttackTime < delay) {
            return; // Слишком рано для следующей атаки
        }

        if (target == null || mc.player.getDistanceEyePos(target) > attackDistance()) {
            return; // Цель вне досягаемости
        }

        if (!antiFlagAttack()) {
            return; // Если метод определил, что атака может зафлагать, отменяем её
        }


        if (mc.player.isElytraFlying() && (mc.player.getMotion().y > -0.2 || mc.player.getMotion().length() < 0.5)) {
            return; // Не атакуем при слишком малой скорости, чтобы не флагало
        }

        if (moreOptions.getValueByName("Проверка луча").get()
                && !moreOptions.getValueByName("Перелетать противника").get()
                && !mc.player.isElytraFlying()) {
            if (selected == null) {
                return;
            }
        }

        if (mc.player.isBlocking() && options.getValueByName("Отжимать щит").get()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        if (!moreOptions.getValueByName("Бить через стены").get() && !mc.player.canEntityBeSeen(target)) {
            return; // Нельзя видеть цель
        }

        if (moreOptions.getValueByName("Не бить если кушаешь").get()
                && mc.player.isHandActive()
                && mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT) {
            return; // Игрок ест
        }

        if (moreOptions.getValueByName("Не бить если в гуи").get()
                && mc.currentScreen != null
                && !(mc.currentScreen instanceof DropDown
                || mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof IngameMenuScreen)) {
            return; // Открыто GUI
        }

        // Устанавливаем время последней атаки перед выполнением атаки
        lastAttackTime = currentTime;

        // Выполнение атаки
        tpAuraRule = true;
        if (options.getValueByName("Ускорять ротацию при атаке").get()) {
            setRotate(70, 45);
        }


        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        timerUtility.isReached(600);
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        boolean is_sprint = mc.player.isSprinting();
        boolean sprint = false;
        if (is_sprint) {
            if (sprints.is("Грим")) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                if (mc.player.isInWater()) {
                    mc.player.setSprinting(false);
                    sprint = true;
                }
                sprint = true;
            }
            if (sprints.is("Legit")) {
                mc.player.connection.sendPacket(new CUseEntityPacket(target, mc.player.isSneaking()));

                new Thread(() -> {
                    try {
                        Thread.sleep(50);
                        mc.player.setSprinting(true);
                    } catch (InterruptedException ignored) {
                    }
                }).start();
            }
        }

        if (target instanceof PlayerEntity player && options.getValueByName("Ломать щит").get()) {
            breakShieldPlayer(player);
        }

        if (mc.player.isElytraFlying() && (mc.player.getMotion().y > -0.2 || mc.player.getMotion().length() < 0.5)) {
            return; // Не атакуем при слишком малой скорости, чтобы не флагало
        }

    }

    public boolean shouldPlayerFalling() {
        return AttackUtil.isPlayerFalling(options.getValueByName("Только криты").get() && smartCrits.get(), options.getValueByName("Синхронизировать с TPS").get(), correctionType.is("Сфокусированный"));
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.ticksExisted < 3) return false;
        if ((mc.player.getDistanceEyePos(entity)) > maxRange()) return false;

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity)) {
                return false;
            }
            if (!targets.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
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
        if (options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
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
        int minCPSValue = minCPS.get().intValue();
        int maxCPSValue = maxCPS.get().intValue();
        if (minCPSValue > maxCPSValue) {
            maxCPSValue = minCPSValue;
        }

        int minMS = 1000 / maxCPSValue;
        int maxMS = 1000 / minCPSValue;

        Random random = new Random();
        int randomMS = random.nextInt(maxMS - minMS + 1) + minMS;

        timerUtility.setLastMS(1);
        target = null;
        mc.timer.timerSpeed = 1;

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

