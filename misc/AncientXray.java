package minecraft.rolest.modules.impl.misc;



import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


import minecraft.rolest.events.WorldEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

@ModuleRegister(name = "AncientXray", category = Category.Misc,desc ="ищет чота")
public class AncientXray extends Module {
    private final List<BlockPos> highlightedDebris = new ArrayList<>();
    private final List<BlockPos> clicked = new ArrayList<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private BlockPos clicking = null;
    private int foundDebrisCount = 0;
    private static final int SEARCH_RADIUS = 20;
    private static final int CLICK_DELAY = 500;
    private static final Hand MAIN_HAND = Hand.MAIN_HAND;

    public AncientXray() {}

    @Subscribe
    private void onRender(WorldEvent e) {
        BlockPos playerPos = Minecraft.getInstance().player.getPosition();
        highlightedDebris.clear();

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = Minecraft.getInstance().world.getBlockState(pos).getBlock();
                    if (isTargetBlock(pos, block)) {
                        RenderUtils.drawBlockBox(pos, new Color(255, 255, 255).getRGB());
                        highlightedDebris.add(pos);
                    }
                }
            }
        }

        if (!highlightedDebris.isEmpty() && (clicking == null || !threadPool.isShutdown())) {
            startClickingThread();
        }
    }

    private boolean isTargetBlock(BlockPos pos, Block block) {
        return block == Blocks.ANCIENT_DEBRIS
                && hasAtLeastTwoAirBlocksAround(pos)
                && !hasTwoQuartzOrGoldNearby(pos)
                && hasAtLeastFiveAirInCube(pos)
                && !hasTooManyAncientDebrisNearby(pos);
    }

    private void startClickingThread() {
        threadPool.execute(() -> {
            for (BlockPos pos : highlightedDebris) {
                if (!clicked.contains(pos)) {
                    Minecraft.getInstance().player.connection.sendPacket(new CPlayerDiggingPacket(Action.START_DESTROY_BLOCK, pos, Direction.UP));
                    clicked.add(pos);
                    clicking = pos;
                    foundDebrisCount++;


                    try {
                        Thread.sleep(CLICK_DELAY);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            }
            clicking = null;
        });
    }




    private boolean hasAtLeastTwoAirBlocksAround(BlockPos pos) {
        int airBlockCount = 0;
        for (Direction direction : Direction.values()) {
            Block surroundingBlock = Minecraft.getInstance().world.getBlockState(pos.offset(direction)).getBlock();
            if (surroundingBlock == Blocks.AIR || surroundingBlock == Blocks.LAVA) {
                airBlockCount++;
                if (airBlockCount >= 2) return true;
            }
        }
        return false;
    }

    private boolean hasTwoQuartzOrGoldNearby(BlockPos pos) {
        int quartzOrGoldCount = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block nearbyBlock = Minecraft.getInstance().world.getBlockState(pos.add(x, y, z)).getBlock();
                    if (nearbyBlock == Blocks.NETHER_QUARTZ_ORE || nearbyBlock == Blocks.NETHER_GOLD_ORE) {
                        quartzOrGoldCount++;
                        if (quartzOrGoldCount >= 4) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasAtLeastFiveAirInCube(BlockPos pos) {
        int airBlockCount = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block nearbyBlock = Minecraft.getInstance().world.getBlockState(pos.add(x, y, z)).getBlock();
                    if (nearbyBlock == Blocks.AIR || nearbyBlock == Blocks.LAVA) {
                        airBlockCount++;
                        if (airBlockCount >= 4) return true;
                    }
                }
            }
        }
        return airBlockCount >= 4;
    }

    private boolean hasTooManyAncientDebrisNearby(BlockPos pos) {
        int ancientDebrisCount = 0;
        for (int x = -3; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 3; z++) {
                    Block nearbyBlock = Minecraft.getInstance().world.getBlockState(pos.add(x, y, z)).getBlock();
                    if (nearbyBlock == Blocks.ANCIENT_DEBRIS) {
                        ancientDebrisCount++;
                        if (ancientDebrisCount > 3) return true;
                    }
                }
            }
        }
        return false;
    }
}
