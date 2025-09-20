package minecraft.rolest.modules.impl.movement;

import minecraft.rolest.events.*;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.impl.combat.HitAura;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.player.DamagePlayerUtil;
import minecraft.rolest.utils.player.InventoryUtil;
import minecraft.rolest.utils.player.MoveUtils;
import minecraft.rolest.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


@ModuleRegister(name = "Strafe", category = Category.Movement,desc ="кс2")
public class Strafe extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", "Matrix Hard", "Matrix", "Matrix Hard");
    private final BooleanSetting elytra = new BooleanSetting("Буст с элитрой", false);
    private final SliderSetting setSpeed = new SliderSetting("Скорость", 1.5F, 0.5F, 2.5F, 0.1F).setVisible(() -> elytra.get());
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", false);
    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F).setVisible(() -> damageBoost.get());

    private final BooleanSetting onlyGround = new BooleanSetting("Только на земле", false).setVisible(() -> mode.is("Matrix Hard"));
    private final BooleanSetting autoJump = new BooleanSetting("Прыгать", false);
    private final BooleanSetting moveDir = new BooleanSetting("Направление", true);

    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private final StrafeMovement strafeMovement = new StrafeMovement();
    private final HitAura hitAura;
    public static int waterTicks;

    public boolean check() {
        return Rol.getInstance().getModuleManager().getHitAura().getTarget() != null && Rol.getInstance().getModuleManager().getHitAura().isState();
    }

    public Strafe(HitAura hitAura) {
        this.hitAura = hitAura;
        addSettings(mode, elytra, setSpeed, damageBoost, boostSpeed, onlyGround, autoJump, moveDir);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (mode.is("Grim")) return;
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(MovingEvent e) {
        if (mode.is("Grim")) return;
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(PostMoveEvent e) {
        if (mode.is("Grim")) return;
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mode.is("Grim")) return;
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (mode.is("Grim")) return;
        handleDamageEvent(e);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (moveDir.get() && !check()) {
            mc.player.rotationYawHead = MoveUtils.moveYaw(mc.player.rotationYaw);
            mc.player.renderYawOffset = MoveUtils.moveYaw(mc.player.rotationYaw);

        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (autoJump.get()) {
            if (mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
                mc.player.jump();
            }
        }

        if (!elytra.get()) return;
        int elytra = InventoryUtil.getInstance().getHotbarSlotOfItem();

        if (mc.player.isInWater() || mc.player.isInLava() || waterTicks > 0 || elytra == -1)
            return;
        if (mc.player.fallDistance != 0 && mc.player.fallDistance < 0.1 && mc.player.motion.y < -0.1) {
            if (elytra != -2) {
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
            }
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));

            if (elytra != -2) {
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if  (mode.is("Matrix Hard")) {
            if (strafes()) {
                handleStrafesEventAction(action);
            }
            if (strafeMovement.isNeedSwap()) {
                handleNeedSwapEventAction(action);
            }
        }
    }

    private void handleEventMove(MovingEvent eventMove) {
        int elytraSlot = InventoryUtil.getInstance().getHotbarSlotOfItem();

        if (elytra.get() && elytraSlot != -1) {
            if (MoveUtils.isMoving() && !mc.player.onGround && mc.player.fallDistance >= 0.15 && eventMove.isToGround()) {
                MoveUtils.setMotion(setSpeed.get());
                strafeMovement.setOldSpeed(setSpeed.get() / 1.06);
            }
        }

        if (mc.player.isInWater() || mc.player.isInLava()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (mode.is("Matrix Hard")) {
            if (onlyGround.get())
                if (!mc.player.isOnGround()) return;

            if (strafes()) {
                handleStrafesEventMove(eventMove);
            } else {
                strafeMovement.setOldSpeed(0);
            }
        }

        if (mode.is("Matrix")) {
            if (waterTicks > 0) return;
            if (MoveUtils.isMoving() && MoveUtils.getMotion() <= 0.289385188) {
                if (!eventMove.isToGround()) {
                    MoveUtils.setStrafe(MoveUtils.reason(false) || mc.player.isHandActive() ? MoveUtils.getMotion() - 0.00001f : 0.245f - (new Random().nextFloat() * 0.000001f));
                }
            }
        }
    }

    private void handleEventPostMove(PostMoveEvent eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.get()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(MovingEvent eventMove) {


        if (damageBoost.get())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.get().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        if (mc.player.isInWater() || waterTicks > 0) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
    }
}
