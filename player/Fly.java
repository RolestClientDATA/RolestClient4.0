package minecraft.rolest.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;

import static minecraft.rolest.modules.impl.render.WorldTweaks.mode;

@ModuleRegister(name = "Fly", category = Category.Movement)
public class Fly extends Module {
    private final ModeSetting mod = new ModeSetting("Mode", "HolyTime", "HolyTime","SpookyTime");
    private final Minecraft mc = Minecraft.getInstance();

    public Fly() {
        addSettings(mod);

    }
    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (this.mod.is("HolyTime")) {
            float SPEED = 0.055F;
            Minecraft.getInstance();
            PlayerEntity player = mc.player;
            if (player != null && player.isAlive()) {
                player.setMotion(player.getMotion().x, player.getMotion().y + 0.05499999761581421D, player.getMotion().z);
            }
            if (mode.is("SpookyTime")) {
                ItemStack currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

                if (currentStack.getItem() == Items.ELYTRA) {
                    if (mc.player.isOnGround()) {
                        mc.player.jump();
                        mc.player.rotationPitch = -90.0f;
                    } else if (ElytraItem.isUsable(currentStack) && !mc.player.isElytraFlying()) {
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                        mc.player.rotationPitch = -90.0f;
                    }

                    mc.player.rotationPitch = 0.0f;

                    Vector3d motion = mc.player.getMotion();
                    mc.player.setMotion(motion.x, motion.y * 1.08f, motion.z);
                }
            }

        }

    }
}