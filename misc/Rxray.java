package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.WorldEvent;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.modules.settings.impl.ColorSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.render.color.ColorUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

import java.util.HashMap;
import java.util.Map;

@ModuleRegister(name = "DiamondESP", category = Category.Misc)
public class Rxray extends Module {
    private final Map<Block, Integer> blocks = new HashMap<>();
//Фризи иди нахуй перестань насилавать тандерхак
    public static final ModeSetting pibor = new ModeSetting("Тема", "Свой", "Свой");
    public static final SliderSetting speedis = new SliderSetting("Скорость переливания", 5.0F, 5.0F, 40.0F, 2.5F);
    public static final ColorSetting color1 = (ColorSetting) new ColorSetting("Свой", ColorUtils.rgb(255, 255, 255)).setVisible(() -> pibor.is("Свой"));

    public Rxray() {
        this.blocks.put(Blocks.DIAMOND_ORE, getDynamicShulkerColor(0));
        addSettings(pibor, speedis, color1);
    }

    private int getDynamicShulkerColor(int index) {
        int speed = ((Float) speedis.get()).intValue();

        if (pibor.is("Дефолт")) {
            Theme.MainColor(0);
        } else if (pibor.is("Свой")) {
            return ColorUtils.gradient(color1.get(), color1.get(), index, speed);
        }

        return ColorUtils.rgb(255, 0, 0);
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        if (Minecraft.world != null) {
            int range = 20;
            BlockPos playerPos = Minecraft.player.getPosition();

            for (int x = -range; x < range; ++x) {
                for (int y = -range; y < range; ++y) {
                    for (int z = -range; z < range; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        Block block = Minecraft.world.getBlockState(pos).getBlock();
                        if (this.blocks.containsKey(block)) {
                            int color = getDynamicShulkerColor(0);
                            RenderUtils.drawBlockBox(pos, color);
                        }
                    }
                }
            }
        }
    }
}