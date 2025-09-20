package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

@ModuleRegister(name = "NoRotate", category = Category.Misc)
public class NoServerDesync extends Module {

    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof SHeldItemChangePacket wrapper) {
            final int serverSlot = wrapper.getHeldItemHotbarIndex();
            if (serverSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        }
        if (e.isSend()) {
            if (this.isPacketSent) {
                if (e.getPacket() instanceof CPlayerPacket playerPacket) {
                    playerPacket.setRotation(targetYaw, targetPitch);
                    this.isPacketSent = false;
                }
            }
        }
    }

    public void sendRotationPacket(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.isPacketSent = true;
    }
}
