package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CEntityActionPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.api.Module;

@ModuleRegister(name = "WaterSpeed", category = Category.Movement , desc = "ватерспиды фт")
public class WaterSpeed extends Module {
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!mc.player.isInWater() || !mc.player.isSwimming() || mc.player.ticksExisted % (9 + (mc.player.getEntityId() % 7)) != 0)
            return;
        double speed = Math.sqrt(mc.player.getMotion().x * mc.player.getMotion().x + mc.player.getMotion().z * mc.player.getMotion().z);
        double multiplier = 1.0 + (mc.player.ticksExisted % 600) * 0.0005;
        if (speed * multiplier > 4.5) {
            double scale = 4.5 / speed;
            mc.player.setVelocity(mc.player.getMotion().x * scale, mc.player.getMotion().y * 0.97, mc.player.getMotion().z * scale);
        } else {
            mc.player.setVelocity(mc.player.getMotion().x * multiplier, mc.player.getMotion().y * 0.97, mc.player.getMotion().z * multiplier);
        }
        if (mc.player.ticksExisted % (27 + (mc.player.getEntityId() % 17)) == 0 && Math.random() > 0.95) { // Increased randomness threshold from 0.9 to 0.95 for less frequent sprint toggling
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, mc.player.isSprinting() ? CEntityActionPacket.Action.STOP_SPRINTING : CEntityActionPacket.Action.START_SPRINTING));
        }
    }
}