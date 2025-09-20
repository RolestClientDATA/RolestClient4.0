package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.optifine.render.RenderUtils;
import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.WorldEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.modules.settings.impl.BooleanSetting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


@ModuleRegister(name = "BaseFinder", category = Category.Misc)
public class BaseFinder extends Module {

    private BindSetting active = new BindSetting("Кнопка поиска", 0);

    public BooleanSetting notif = new BooleanSetting("Сообщение о сундуке", true);

    public BooleanSetting gps = new BooleanSetting("GPS на ближайший сундук", true);

    private final Map<TileEntityType<?>, Integer> tiles = new HashMap<>(Map.of(
            new ChestTileEntity().getType(), new Color(0, 187, 8).getRGB() //установка зеленого цвета есп, кто хочет может поменять
    ));

    public BaseFinder() {
        addSettings(this.active, this.notif, this.gps);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == this.active.get()) {
            boolean foundChest = false;
            for (TileEntity t : mc.world.loadedTileEntityList) {
                if (t instanceof net.minecraft.tileentity.ChestTileEntity) {
                    int x = t.getPos().getX();
                    int y = t.getPos().getY();
                    int z = t.getPos().getZ();
                    double distanceSq = mc.player.getDistanceSq(x, y, z);
                    if (distanceSq < 20000.0D) {
                        foundChest = true;
                        if (this.notif.get())
                            print("" + TextFormatting.GREEN + "Возможно была найдена база");
                        BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(mc.player.getPosX(), mc.player.getPosY() - 1.0D, mc.player.getPosZ()), Direction.UP, new BlockPos(x, y, z), false);
                        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult);
                        if (this.gps.get()) {
                            mc.player.sendChatMessage(".gps add Chest " + x + " " + y + " " + z);
                        }
                    }
                }
            }
            if (!foundChest) {
                print("" + TextFormatting.RED + "В этом регионе не нашлось баз");
            }
        }
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (!tiles.containsKey(tile.getType())) continue;

            BlockPos pos = tile.getPos();

            RenderUtils.drawBlockBox(pos, tiles.get(tile.getType()));
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ChestMinecartEntity) {
                RenderUtils.drawBlockBox(entity.getPosition(), -1);
            }
        }
    }

    public void onDisable() {
    }
}