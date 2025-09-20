package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;

@ModuleRegister(name = "CreeperFarm", desc = "Автоматически убивает криперов с обходом препятствий", category = Category.Player)
public class KriperFarm extends Module {

    private final SliderSetting distanceSetting = new SliderSetting("Дистанция", 10.0f, 5.0f, 200.0f, 0.5f);
    private final BooleanSetting runModeSetting = new BooleanSetting("Автоматически бегать", true);
    private final BooleanSetting retreatSetting = new BooleanSetting("Убегать от взрывов", true);
    private final SliderSetting retreatDistanceSetting = new SliderSetting("Дистанция бега", 7.0f, 3.0f, 15.0f, 0.5f).setVisible(() -> retreatSetting.get());
    private final SliderSetting obstacleCheckDistance = new SliderSetting("Дальность проверки", 1.5f, 0.5f, 3.0f, 0.1f);
    private final SliderSetting avoidAngle = new SliderSetting("Угол обхода", 45.0f, 15.0f, 90.0f, 1.0f);
    private final SliderSetting rotationSpeed = new SliderSetting("Скорость поворота", 0.3f, 0.1f, 1.0f, 0.05f);
    private final BooleanSetting jumpCheckbox = new BooleanSetting("Прыгать", false);
    public ModeListSetting collect = new ModeListSetting("Собирать",
            new BooleanSetting("Порох", true),
            new BooleanSetting("Пузырьки опыта", true)
    );

    private static final Minecraft mc = Minecraft.getInstance();
    private CreeperEntity targetCreeper;
    private ItemEntity targetItem;
    private long lastAttackTime = 0;
    private boolean isRetreating = false;
    private int retreatTicks = 0;
    private int avoidDirection = 1;
    private boolean isAutoMoving = false;
    private float lastYaw = 0;
    private float lastPitch = 0;

    public KriperFarm() {
        addSettings(distanceSetting, runModeSetting, retreatSetting,
                retreatDistanceSetting, obstacleCheckDistance, avoidAngle, rotationSpeed,
                jumpCheckbox, collect);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.world == null || mc.player == null) return;

        handleJumping();
        handleMovement();
        handleTargeting();
    }

    private void handleJumping() {
        if (jumpCheckbox.get()) {
            mc.gameSettings.keyBindJump.setPressed(true);
        } else {
            if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.gameSettings.keyBindJump.setPressed(false);
            }
        }
    }

    private void handleMovement() {
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;

        if (!isAutoMoving) {
            mc.gameSettings.keyBindForward.setPressed(mc.gameSettings.keyBindForward.isKeyDown());
            mc.gameSettings.keyBindBack.setPressed(mc.gameSettings.keyBindBack.isKeyDown());
        }
    }

    private void handleTargeting() {
        targetItem = findClosestCollectible();
        if (targetItem != null) {
            runToTarget(targetItem);
            resetRetreatState();
            return;
        }

        targetCreeper = findClosestCreeper();
        if (targetCreeper == null) {
            resetAutoMovement();
            resetRetreatState();
            return;
        }

        if (isTargetTooFar() || !hasLineOfSight(targetCreeper)) {
            resetAutoMovement();
            resetRetreatState();
            return;
        }

        handleCreeperInteraction();
    }

    private void handleCreeperInteraction() {
        float swell = targetCreeper.getSwelling(1.0f);
        if (retreatSetting.get() && swell > 0.5f) {
            handleRetreat();
        } else {
            handleAttack();
        }
    }

    private void handleRetreat() {
        retreatFromCreeper(targetCreeper);
        isRetreating = true;
        if (++retreatTicks >= 40) {
            resetRetreatState();
        }
    }

    private void handleAttack() {
        if (runModeSetting.get()) {
            runToTarget(targetCreeper);
        }
        attackCreeper(targetCreeper);
        isRetreating = false;
    }

    private CreeperEntity findClosestCreeper() {
        CreeperEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof CreeperEntity creeper && creeper.isAlive()) {
                double dist = mc.player.getDistanceSq(creeper);
                if (dist < closestDistance && dist <= distanceSetting.get() * distanceSetting.get()
                        && hasLineOfSight(creeper)) {
                    closest = creeper;
                    closestDistance = dist;
                }
            }
        }
        return closest;
    }

    private ItemEntity findClosestCollectible() {
        ItemEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity item && item.isAlive()) {
                boolean isGunpowder = item.getItem().getItem() == Items.GUNPOWDER;
                boolean isXpBottle = item.getItem().getItem() == Items.EXPERIENCE_BOTTLE;

                if ((isGunpowder && collect.getValueByName("Порох").get()) ||
                        (isXpBottle && collect.getValueByName("Пузырьки опыта").get())) {

                    double dist = mc.player.getDistanceSq(item);
                    if (dist < closestDistance && dist <= distanceSetting.get() * distanceSetting.get()
                            && hasLineOfSight(item)) {
                        closest = item;
                        closestDistance = dist;
                    }
                }
            }
        }
        return closest;
    }

    private boolean hasLineOfSight(Entity entity) {
        Vector3d start = mc.player.getEyePosition(1.0F);
        Vector3d end = entity.getBoundingBox().getCenter();
        RayTraceContext context = new RayTraceContext(start, end,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player);
        return mc.world.rayTraceBlocks(context).getType() == RayTraceResult.Type.MISS;
    }

    private boolean isObstructed() {
        if (isPlayerManuallyMoving()) return false;

        BlockPos playerPos = mc.player.getPosition();
        BlockPos[] checkPositions = {
                playerPos,
                playerPos.up(),
                playerPos.down(),
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west(),
                playerPos.north().up(),
                playerPos.south().up(),
                playerPos.east().up(),
                playerPos.west().up()
        };

        for (BlockPos pos : checkPositions) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (isSlab(block) || isCarpet(block)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSlab(Block block) {
        return block == Blocks.STONE_SLAB || block == Blocks.OAK_SLAB ||
                block == Blocks.COBBLESTONE_SLAB || block == Blocks.BIRCH_SLAB ||
                block == Blocks.SPRUCE_SLAB || block == Blocks.JUNGLE_SLAB ||
                block == Blocks.ACACIA_SLAB || block == Blocks.DARK_OAK_SLAB ||
                block == Blocks.NETHER_BRICK_SLAB || block == Blocks.QUARTZ_SLAB ||
                block == Blocks.RED_SANDSTONE_SLAB || block == Blocks.PURPUR_SLAB ||
                block == Blocks.PRISMARINE_SLAB || block == Blocks.DARK_PRISMARINE_SLAB ||
                block == Blocks.PRISMARINE_BRICK_SLAB || block == Blocks.MOSSY_COBBLESTONE_SLAB ||
                block == Blocks.SMOOTH_STONE_SLAB || block == Blocks.SMOOTH_SANDSTONE_SLAB ||
                block == Blocks.SMOOTH_QUARTZ_SLAB || block == Blocks.SMOOTH_RED_SANDSTONE_SLAB;
    }

    private boolean isCarpet(Block block) {
        return block == Blocks.WHITE_CARPET || block == Blocks.ORANGE_CARPET ||
                block == Blocks.MAGENTA_CARPET || block == Blocks.LIGHT_BLUE_CARPET ||
                block == Blocks.YELLOW_CARPET || block == Blocks.LIME_CARPET ||
                block == Blocks.PINK_CARPET || block == Blocks.GRAY_CARPET ||
                block == Blocks.LIGHT_GRAY_CARPET || block == Blocks.CYAN_CARPET ||
                block == Blocks.PURPLE_CARPET || block == Blocks.BLUE_CARPET ||
                block == Blocks.BROWN_CARPET || block == Blocks.GREEN_CARPET ||
                block == Blocks.RED_CARPET || block == Blocks.BLACK_CARPET;
    }

    private void runToTarget(Entity target) {
        if (target == null) return;

        isAutoMoving = true;
        float[] rotations = calculateRotations(target);
        smoothlyUpdateRotations(rotations);

        double dx = target.getPosX() - mc.player.getPosX();
        double dz = target.getPosZ() - mc.player.getPosZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 1.5) {
            handleMovementWithObstacles(dx, dz);
        } else {
            resetAutoMovement();
        }
    }

    private void smoothlyUpdateRotations(float[] rotations) {
        mc.player.rotationYaw = updateRotationSmoothly(mc.player.rotationYaw, rotations[0]);
        mc.player.rotationPitch = updateRotationSmoothly(mc.player.rotationPitch, rotations[1]);
    }

    private float updateRotationSmoothly(float current, float target) {
        float delta = MathHelper.wrapDegrees(target - current);
        return Math.abs(delta) < 0.1f ? target : current + delta * rotationSpeed.get();
    }

    private void handleMovementWithObstacles(double dx, double dz) {
        if (isObstructed()) {
            avoidObstacle(dx, dz);
        } else {
            moveForwardSmoothly();
        }
        adjustStrafeSmoothly(dx, dz);
    }

    private void avoidObstacle(double dx, double dz) {
        if (mc.player.collidedHorizontally) {
            avoidDirection *= -1;
        }

        float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float avoidAngleValue = avoidAngle.get() * avoidDirection;

        mc.player.rotationYaw = updateRotationSmoothly(mc.player.rotationYaw, angle + avoidAngleValue);
        moveForwardSmoothly();

        if (isStillObstructed()) {
            avoidDirection *= -1;
        }
    }

    private void moveForwardSmoothly() {
        if (!isPlayerManuallyMoving()) {
            mc.gameSettings.keyBindForward.setPressed(true);
            if (mc.player.getFoodStats().getFoodLevel() > 6) {
                mc.gameSettings.keyBindSprint.setPressed(true);
            }
        }
    }

    private void adjustStrafeSmoothly(double dx, double dz) {
        if (isPlayerManuallyMoving()) return;

        float yaw = mc.player.rotationYaw;
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90 - yaw;
        angle = MathHelper.wrapDegrees((float) angle);

        boolean shouldStrafe = Math.abs(angle) > 30;
        mc.gameSettings.keyBindLeft.setPressed(shouldStrafe && angle > 0);
        mc.gameSettings.keyBindRight.setPressed(shouldStrafe && angle < 0);
    }

    private void retreatFromCreeper(CreeperEntity creeper) {
        Vector3d direction = mc.player.getPositionVec().subtract(creeper.getPositionVec()).normalize();
        float[] rotations = calculateRetreatRotations(creeper);

        smoothlyUpdateRotations(rotations);

        double retreatX = mc.player.getPosX() + direction.x * retreatDistanceSetting.get();
        double retreatZ = mc.player.getPosZ() + direction.z * retreatDistanceSetting.get();
        handleMovementWithObstacles(retreatX - mc.player.getPosX(), retreatZ - mc.player.getPosZ());
    }

    private void attackCreeper(CreeperEntity creeper) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime >= 500) {
            double distance = mc.player.getDistance(creeper);
            if (distance <= 3.0 && creeper.getSwelling(1.0f) <= 0.5f) {
                mc.playerController.attackEntity(mc.player, creeper);
                mc.player.swingArm(Hand.MAIN_HAND);
                lastAttackTime = currentTime;
            }
        }
    }

    private void resetAutoMovement() {
        isAutoMoving = false;
        if (!isPlayerManuallyMoving()) {
            mc.gameSettings.keyBindForward.setPressed(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.player.rotationYaw = lastYaw;
            mc.player.rotationPitch = lastPitch;
        }
        mc.gameSettings.keyBindLeft.setPressed(false);
        mc.gameSettings.keyBindRight.setPressed(false);
    }

    private void resetRetreatState() {
        isRetreating = false;
        retreatTicks = 0;
    }

    private boolean isPlayerManuallyMoving() {
        return mc.gameSettings.keyBindForward.isKeyDown() ||
                mc.gameSettings.keyBindBack.isKeyDown() ||
                mc.gameSettings.keyBindLeft.isKeyDown() ||
                mc.gameSettings.keyBindRight.isKeyDown();
    }

    private boolean isTargetTooFar() {
        return mc.player.getDistanceSq(targetCreeper) > distanceSetting.get() * distanceSetting.get();
    }

    private boolean isStillObstructed() {
        Vector3d start = mc.player.getPositionVec();
        Vector3d end = start.add(mc.player.getLookVec().scale(obstacleCheckDistance.get()));
        return mc.world.rayTraceBlocks(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        )).getType() == RayTraceResult.Type.BLOCK;
    }

    private float[] calculateRotations(Entity entity) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0F);
        Vector3d targetPos = entity.getBoundingBox().getCenter();
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return new float[]{
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90,
                (float) -Math.toDegrees(Math.atan2(diffY, diffXZ))
        };
    }

    private float[] calculateRetreatRotations(Entity entity) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0F);
        Vector3d targetPos = entity.getBoundingBox().getCenter();
        double diffX = eyesPos.x - targetPos.x;
        double diffY = eyesPos.y - targetPos.y;
        double diffZ = eyesPos.z - targetPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return new float[]{
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90,
                (float) -Math.toDegrees(Math.atan2(diffY, diffXZ))
        };
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetAllStates();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetAllStates();
        mc.gameSettings.keyBindJump.setPressed(false);
    }

    private void resetAllStates() {
        lastAttackTime = 0;
        resetRetreatState();
        avoidDirection = 1;
        isAutoMoving = false;
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
        targetCreeper = null;
        targetItem = null;
    }
}