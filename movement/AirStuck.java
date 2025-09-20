package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import  minecraft.rolest.events.EventPacket;
import  minecraft.rolest.events.EventUpdate;
import  minecraft.rolest.modules.api.Category;
import  minecraft.rolest.modules.api.Module;
import  minecraft.rolest.modules.api.ModuleRegister;
import  minecraft.rolest.modules.settings.impl.SliderSetting;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@ModuleRegister(name = "AirStuck", category = Category.Movement)
public class AirStuck extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость", 0.2873f, 0.1f, 0.5f, 0.01f);
    private int teleportId = 0;

    public AirStuck() {
        addSettings(speed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Prevent fall damage
        mc.player.fallDistance = 0;


        double motionX = mc.player.getMotion().x;
        double motionZ = mc.player.getMotion().z;
        double motionY = 0;


        float moveSpeed = speed.get();
        moveSpeed += (float) (Math.random() * 0.005 - 0.0120);
        moveSpeed = Math.min(moveSpeed, 0.2700f);

        mc.player.setMotion(motionX * moveSpeed, motionY, motionZ * moveSpeed);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (e.getPacket() instanceof SPlayerPositionLookPacket) {
            SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) e.getPacket();
            teleportId = packet.getTeleportId();


            mc.player.connection.sendPacket(new CConfirmTeleportPacket(teleportId));


            mc.player.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            e.cancel();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.player.setMotion(0, 0, 0);
        }
    }
}