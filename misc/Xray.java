package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;
import minecraft.rolest.events.WorldEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@ModuleRegister(name = "BlockESP", category = Category.Render)
public class Xray extends Module {

    private final BooleanSetting[] blockOptionsArray = new BooleanSetting[]{
            new BooleanSetting("Сундуки", true),
            new BooleanSetting("Бочки", true),
            new BooleanSetting("Воронки", true),
            new BooleanSetting("Печки", true),
            new BooleanSetting("Эндер сундуки", true),
            new BooleanSetting("Шалкеры", true),
            new BooleanSetting("Спавнера мобов", true),
            new BooleanSetting("Маяки", true),
            new BooleanSetting("Зелья варка", true),
            new BooleanSetting("Командные блоки", true),
            new BooleanSetting("Раздатчики", true),
            new BooleanSetting("Железная руда", true),
            new BooleanSetting("Алмазная руда", true),
            new BooleanSetting("Угольная руда", true),
            new BooleanSetting("Золотая руда", true),
            new BooleanSetting("Редстоун руда", true),
            new BooleanSetting("Изумрудная руда", true),
            new BooleanSetting("Лазуритовая руда", true),
            new BooleanSetting("Медная руда", true),
            new BooleanSetting("Древний обломок", true)
    };

    private final ModeListSetting blockOptions = new ModeListSetting("Опции", blockOptionsArray);

    private final Map<Object, Integer> blocksAndTiles = new HashMap<>();

    public Xray() {
        addSettings(blockOptions);
        updateBlockAndTileMap();
    }

    private void updateBlockAndTileMap() {
        blocksAndTiles.clear();

        for (BooleanSetting setting : blockOptionsArray) {
            if (setting.get()) {
                switch (setting.getName()) {
                    case "Сундуки" -> blocksAndTiles.put(TileEntityType.CHEST, new Color(215, 134, 11).getRGB());
                    case "Бочки" -> blocksAndTiles.put(TileEntityType.BARREL, new Color(131, 90, 33).getRGB());
                    case "Воронки" -> blocksAndTiles.put(TileEntityType.HOPPER, new Color(120, 120, 120).getRGB());
                    case "Печки" -> blocksAndTiles.put(TileEntityType.FURNACE, new Color(150, 150, 150).getRGB());
                    case "Эндер сундуки" -> blocksAndTiles.put(TileEntityType.ENDER_CHEST, new Color(63, 43, 150).getRGB());
                    case "Шалкеры" -> blocksAndTiles.put(TileEntityType.SHULKER_BOX, new Color(0, 194, 255).getRGB());
                    case "Спавнера мобов" -> blocksAndTiles.put(TileEntityType.MOB_SPAWNER, new Color(41, 250, 41).getRGB());
                    case "Маяки" -> blocksAndTiles.put(TileEntityType.BEACON, new Color(85, 255, 170).getRGB());
                    case "Зелья варка" -> blocksAndTiles.put(TileEntityType.BREWING_STAND, new Color(198, 66, 10).getRGB());
                    case "Командные блоки" -> blocksAndTiles.put(TileEntityType.COMMAND_BLOCK, new Color(255, 111, 79).getRGB());
                    case "Раздатчики" -> blocksAndTiles.put(TileEntityType.DISPENSER, new Color(100, 100, 100).getRGB());
                    case "Железная руда" -> blocksAndTiles.put(Blocks.IRON_ORE, new Color(216, 164, 115).getRGB());
                    case "Алмазная руда" -> blocksAndTiles.put(Blocks.DIAMOND_ORE, new Color(45, 197, 239).getRGB());
                    case "Угольная руда" -> blocksAndTiles.put(Blocks.COAL_ORE, new Color(54, 54, 54).getRGB());
                    case "Золотая руда" -> blocksAndTiles.put(Blocks.GOLD_ORE, new Color(255, 223, 0).getRGB());
                    case "Редстоун руда" -> blocksAndTiles.put(Blocks.REDSTONE_ORE, new Color(255, 0, 0).getRGB());
                    case "Изумрудная руда" -> blocksAndTiles.put(Blocks.EMERALD_ORE, new Color(0, 255, 0).getRGB());
                    case "Лазуритовая руда" -> blocksAndTiles.put(Blocks.LAPIS_ORE, new Color(38, 97, 156).getRGB());
                    case "Древний обломок" -> blocksAndTiles.put(Blocks.ANCIENT_DEBRIS, new Color(102, 62, 51).getRGB());
                }
            }
        }
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        updateBlockAndTileMap();

        if (mc.world != null) {
            BlockPos.getAllInBox(mc.player.getPosition().add(-16, -16, -16),
                            mc.player.getPosition().add(16, 16, 16))
                    .forEach(pos -> {
                        Block block = mc.world.getBlockState(pos).getBlock();
                        Integer color = blocksAndTiles.get(block);
                        if (color != null) {
                            RenderUtils.drawBlockBox(pos, color);
                        }
                    });

            for (TileEntity tile : mc.world.loadedTileEntityList) {
                Integer color = blocksAndTiles.get(tile.getType());
                if (color != null) {
                    BlockPos pos = tile.getPos();
                    RenderUtils.drawBlockBox(pos, color);
                }
            }

            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ChestMinecartEntity) {
                    RenderUtils.drawBlockBox(entity.getPosition(), -1);
                }
            }
        }
    }
}