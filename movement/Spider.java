package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.MouseUtil;

@ModuleRegister(name = "Spider", category = Category.Movement)
public class Spider extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Grim", "Grim", "Matrix", "Funtime Fence", "FunTime","HighJump"); // Added FunTime mode
    private final SliderSetting spiderSpeed = new SliderSetting(
            "Speed",
            2.0f,
            1.0f,
            10.0f,
            0.05f
    ).setVisible(() -> !mode.is("Grim") && !mode.is("Funtime Fence") && !mode.is("FunTime")); // Hide for FunTime

    StopWatch stopWatch = new StopWatch();

    public Spider() {
        addSettings(spiderSpeed, mode);
    }

    /**
     * author moyten
     * wonderful boost yoo
     */
    @Subscribe
    private void onMotion(EventMotion motion) {
        switch (mode.get()) {
            case "Matrix" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                long speed = MathHelper.clamp(500 - (spiderSpeed.get().longValue() / 2 * 100), 0, 500);
                if (stopWatch.isReached(speed)) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.collidedVertically = true;
                    mc.player.collidedHorizontally = true;
                    mc.player.isAirBorne = true;
                    mc.player.jump();
                    stopWatch.reset();
                }
            }

            case "Grim" -> {
                int slotInHotBar = getSlotInInventoryOrHotbar(true);

                if (slotInHotBar == -1) {
                    print("Блоки не найдены!");
                    toggle();
                    return;
                }
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                if (mc.player.isOnGround()) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.jump();
                }
                if (mc.player.fallDistance > 0 && mc.player.fallDistance < 2) {
                    placeBlocks(motion, slotInHotBar);
                }
            }


            case "Funtime Fence" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                long speed = 200;
                if (stopWatch.isReached(speed)) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.collidedVertically = true;
                    mc.player.collidedHorizontally = true;
                    mc.player.isAirBorne = true;
                    mc.player.jump();
                    placeFenceStack(motion);
                    stopWatch.reset();
                }
            }
            case "FunTime" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                long speed = 57; // Fast placement for FunTime (100ms)
                if (stopWatch.isReached(speed)) {
                    motion.setOnGround(true);
                    placeFenceStack(motion);
                    mc.player.jump();
                    stopWatch.reset();
                }
            }
            case "HighJump" -> {
                if (!mc.player.collidedHorizontally) {

                }
                long speed = 57;
                if (stopWatch.isReached(speed)) {
                    motion.setOnGround(true);
                    placeFenceStack(motion);
                    mc.player.jump();
                    stopWatch.reset();
                }

            }

        }
    }

    private void placeFenceStack(EventMotion motion) {
        int slotFence = getSlotForFence();
        if (slotFence == -1) {
            print("Заборы не найдены!");
            toggle();
            return;
        }

        int lastSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = slotFence;

        BlockPos playerPos = mc.player.getPosition();

        for (int i = 1; i <= 2; i++) {
            BlockPos fencePos = playerPos.up(i);
            if (mc.world.getBlockState(fencePos).isAir()) {
                placeFenceBlock(fencePos, Hand.MAIN_HAND, mc.playerController, mc.world, mc.player);
            }
        }

        mc.player.inventory.currentItem = lastSlot;
    }


    private void placeBambooFence(EventMotion motion) {
        int slotBambooFence = getSlotForBambooFence();
        if (slotBambooFence == -1) {
            print("Бамбуковые заборы не найдены!");
            toggle();
            return;
        }

        int lastSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = slotBambooFence;

        BlockPos playerPos = mc.player.getPosition();
        BlockPos fencePos = playerPos.down(); // Place bamboo fence directly under the player

        if (mc.world.getBlockState(fencePos).isAir()) {
            placeFenceBlock(fencePos, Hand.MAIN_HAND, mc.playerController, mc.world, mc.player);
        }

        mc.player.inventory.currentItem = lastSlot; // Restore previous slot
    }

    private void placeFenceBlock(BlockPos pos, Hand hand, PlayerController playerController, World world, ClientPlayerEntity player) {
        if (!world.getBlockState(pos).isAir()) {
            return;
        }

        Direction direction = Direction.UP;
        BlockPos targetPos = pos.down();
        BlockRayTraceResult traceResult = new BlockRayTraceResult(
                new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                direction, targetPos, false);

        playerController.processRightClickBlock(player, (ClientWorld) world, hand, traceResult);
        player.swingArm(hand);
    }

    private int getSlotForFence() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.OAK_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.BIRCH_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.SPRUCE_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.ACACIA_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.DARK_OAK_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.JUNGLE_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.CRIMSON_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.WARPED_FENCE
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.BAMBOO
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.GRANITE_WALL
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.STONE_BRICK_WALL
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.COBBLESTONE_WALL
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.GRANITE_STAIRS
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.STONE_STAIRS
                || mc.player.inventory.getStackInSlot(i).getItem() == Items.COBBLESTONE_STAIRS
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.NETHER_BRICK_FENCE)
            {
                return i;
            }
        }
        return -1;
    }

    private int getSlotForBambooFence() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BAMBOO) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventoryOrHotbar(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH) {
                continue;
            }

            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    private void placeBlocks(EventMotion motion, int block) {
        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        motion.setPitch(80);
        motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());
        BlockRayTraceResult r = (BlockRayTraceResult) MouseUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
        mc.player.swingArm(Hand.MAIN_HAND);
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, r);
        mc.player.inventory.currentItem = last;
        mc.player.fallDistance = 0;
    }
}