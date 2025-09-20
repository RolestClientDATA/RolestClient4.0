package minecraft.rolest.utils.rotation;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.Rol;
import minecraft.rolest.events.CameraEvent;
import minecraft.rolest.events.EventRotate;
import minecraft.rolest.utils.client.IMinecraft;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;

public class FreeLookHandler implements IMinecraft {

    public FreeLookHandler() {
        Rol.getInstance().getEventBus().register(this);
    }

    @Getter
    private static boolean active;
    @Getter
    private static float freeYaw, freePitch;

    @Subscribe
    public void onLook(EventRotate e) {
        if (active) {
            rotateTowards(e.getYaw(), e.getPitch());
            e.cancel();
        }
    }

    @Subscribe
    public void onCamera(CameraEvent e) {
        if (active) {
            e.yaw = freeYaw;
            e.pitch = freePitch;
        } else {
            freeYaw = e.yaw;
            freePitch = e.pitch;
        }
    }

    public static void setActive(boolean state) {
        if (active != state) {
            active = state;
            resetRotation();
        }
    }

    private void rotateTowards(double yaw, double pitch) {
        double d0 = pitch * 0.15D;
        double d1 = yaw * 0.15D;
        freePitch = (float) ((double) freePitch + d0);
        freeYaw = (float) ((double) freeYaw + d1);
        freePitch = MathHelper.clamp(freePitch, -90.0F, 90.0F);
    }

    private static void resetRotation() {
        mc.player.rotationYaw = freeYaw;
        mc.player.rotationPitch = freePitch;
    }
}