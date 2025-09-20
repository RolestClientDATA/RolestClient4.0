package minecraft.rolest.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.render.RenderUtils;
import minecraft.rolest.events.WorldEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.Setting;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;

import java.util.HashSet;
import java.util.Set;

@ModuleRegister(
        name = "Nuker",
        category = Category.Misc,
        desc = "симпл димпл папит сквиш"
)
public class Nuker extends Module {
    // Range setting
    final SliderSetting range = new SliderSetting("Range", 2.0F, 1.0F, 5.0F, 0.1F);
    // Breaking speed setting (interval in milliseconds)
    final SliderSetting breakSpeed = new SliderSetting("Break Speed", 1000.0F, 50.0F, 2000.0F, 50.0F);
    // Ore toggle settings
    final BooleanSetting coalOre = new BooleanSetting("Coal Ore", true);
    final BooleanSetting ironOre = new BooleanSetting("Iron Ore", true);
    final BooleanSetting goldOre = new BooleanSetting("Gold Ore", true);
    final BooleanSetting lapisOre = new BooleanSetting("Lapis Ore", true);
    final BooleanSetting diamondOre = new BooleanSetting("Diamond Ore", true);
    final BooleanSetting netheriteBlock = new BooleanSetting("Netherite Block", true);
    final BooleanSetting ancientDebris = new BooleanSetting("Ancient Debris", true);
    final BooleanSetting redstoneOre = new BooleanSetting("Redstone Ore", true);

    long last = 0L;
    final Set<Block> blocks = new HashSet<>();
    BlockPos renderPos = null;
    BlockPos targetBlock = null;

    public Nuker() {
        // Register all settings
        this.addSettings(new Setting[]{
                range, breakSpeed,
                coalOre, ironOre, goldOre, lapisOre,
                diamondOre, netheriteBlock, ancientDebris, redstoneOre
        });
        updateTargetBlocks();
    }

    // Update the set of target blocks based on boolean settings
    private void updateTargetBlocks() {
        blocks.clear();
        if (coalOre.get()) blocks.add(Blocks.COAL_ORE);
        if (ironOre.get()) blocks.add(Blocks.IRON_ORE);
        if (goldOre.get()) blocks.add(Blocks.GOLD_ORE);
        if (lapisOre.get()) blocks.add(Blocks.LAPIS_ORE);
        if (diamondOre.get()) blocks.add(Blocks.DIAMOND_ORE);
        if (netheriteBlock.get()) blocks.add(Blocks.NETHERITE_BLOCK);
        if (ancientDebris.get()) blocks.add(Blocks.ANCIENT_DEBRIS);
        if (redstoneOre.get()) blocks.add(Blocks.REDSTONE_ORE);
    }

    protected float[] rotations(PlayerEntity player) {
        return new float[0];
    }

    @Subscribe
    private void onWorld(WorldEvent worldEvent) {
        // Update target blocks in case settings changed
        updateTargetBlocks();

        int rangeValue = Math.round(range.get());
        long interval = Math.round(breakSpeed.get());
        Vector3d playerPos = Minecraft.getInstance().player.getPositionVec();

        // Check if the current target block is still valid
        if (targetBlock != null) {
            BlockState state = Minecraft.getInstance().world.getBlockState(targetBlock);
            if (state.getBlock() == Blocks.AIR) {
                targetBlock = null;
                renderPos = null;
                return;
            }

            double distance = playerPos.distanceTo(new Vector3d(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5));
            if (distance > rangeValue) {
                targetBlock = null;
                renderPos = null;
            } else if (System.currentTimeMillis() - last >= interval) {
                Minecraft.getInstance().playerController.onPlayerDamageBlock(targetBlock, Direction.UP);
                last = System.currentTimeMillis();
            }
            return;
        }

        // Scan for new target blocks
        for (int x = -rangeValue; x <= rangeValue; x++) {
            for (int z = -rangeValue; z <= rangeValue; z++) {
                for (int y = -4; y <= 4; y++) {
                    BlockPos pos = new BlockPos(playerPos.x + x, playerPos.y + y, playerPos.z + z);
                    BlockState state = Minecraft.getInstance().world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (blocks.contains(block) && state.getBlockHardness(Minecraft.getInstance().world, pos) > 0.0F) {
                        double distance = playerPos.distanceTo(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                        if (distance <= rangeValue && System.currentTimeMillis() - last >= interval) {
                            Minecraft.getInstance().playerController.onPlayerDamageBlock(pos, Direction.UP);
                            last = System.currentTimeMillis();
                            targetBlock = pos;
                            renderPos = pos;
                            return;
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    private void render(WorldEvent world) {
        if (renderPos != null) {
            RenderUtils.drawBlockBox(renderPos, -65536);
        }
    }
}