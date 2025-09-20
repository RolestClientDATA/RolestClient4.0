package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;

import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.InventoryUtil;
import minecraft.rolest.utils.player.MoveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@ModuleRegister(name = "ElytraSpeed", category = Category.Movement)
public class ElytraSpeed extends Module {
    private final StopWatch stopWatch = new StopWatch();
    private final ModeSetting mode = new ModeSetting("Тип", "Grim", "Grim", "ReallyWorld");
    private final SliderSetting boostSpeed = new SliderSetting("Cкорость буста", 0.3F, 0.0F, 0.8F, 1.0E-4F);
    private final BooleanSetting safeMode = new BooleanSetting("Безопасный режим", true);
    private final BooleanSetting autoJump = new BooleanSetting("Авто прыжок", false);
    int oldItem = -1;
    public ElytraSpeed() {
        addSettings(mode, boostSpeed, safeMode, autoJump);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SEntityMetadataPacket && ((SEntityMetadataPacket)e.getPacket()).getEntityId() == mc.player.getEntityId()) {
            e.cancel();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (safeMode.get()) {
            if (mc.player.collidedHorizontally) {
                print(TextFormatting.RED + "Вы столкнулись с блоком!");
               // Rol.getInstance().getNotifyManager().add(0, new WarningNotify(getName() + ": Вы столкнулись с блоком!", 1000));
                toggle();
                return;
            }
        }

        mc.gameSettings.keyBindBack.setPressed(false);
        mc.gameSettings.keyBindLeft.setPressed(false);
        mc.gameSettings.keyBindRight.setPressed(false);

        if (this.autoJump.get() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround()) {
            mc.gameSettings.keyBindJump.setPressed(true);
        }
        int timeSwap = 600;
        if (mode.is("Grim")) {
            timeSwap = 200;
        }
        if (mc.player.isElytraFlying()) {
            mc.gameSettings.keyBindSneak.setPressed(true);
        } else {
            mc.gameSettings.keyBindSneak.setPressed(false);
        }

        if (InventoryUtil.getItemSlot(Items.FIREWORK_ROCKET) == -1 || mc.player.collidedHorizontally || !InventoryUtil.doesHotbarHaveItem(Items.ELYTRA)) {
            return;
        }

        if (this.autoJump.get() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
            mc.player.jump();
        }

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() != Items.ELYTRA || !(mc.player.fallDistance < 4.0f) || mc.player.isOnGround() || mc.player.isInWater() || mc.player.isInLava() || mc.player.collidedHorizontally || !stopWatch.hasTimeElapsed2(timeSwap)) continue;
            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            for (Entity entity : mc.world.getAllEntities()) {
                if (!(entity instanceof FireworkRocketEntity) || !(mc.player.getDistance(entity) < 4.0f) || entity.ticksExisted >= 30) continue;
                float speed = 0.9f + boostSpeed.get();
                MoveUtils.setMotion(speed);
            }
            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
            this.oldItem = i;
            if (!stopWatch.hasTimeElapsed2(timeSwap)) continue;
            InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, false);
            stopWatch.reset();
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (!Rol.getInstance().getModuleManager().getHitAura().isState() || Rol.getInstance().getModuleManager().getHitAura().getTarget() == null) {
            mc.player.rotationPitchHead = 15;
            e.setPitch(15);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (oldItem != -1) {
            if (mc.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA) {
                mc.playerController.windowClick(0, oldItem < 9 ? oldItem + 36 : oldItem, 38, ClickType.SWAP, mc.player);
            }
            oldItem = -1;
        }

        stopWatch.reset();
      //  return false;
    }
}
