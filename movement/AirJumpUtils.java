package minecraft.rolest.modules.impl.movement;

import minecraft.rolest.utils.client.IMinecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;

public class AirJumpUtils implements IMinecraft {

    public static void setMotionY(double y) {
        mc.player.setMotion(mc.player.getMotion().x, y, mc.player.getMotion().z);
    }

    public static void stabilizeMovement(double reduceFactor) {
        Vector3d motion = mc.player.getMotion();
        mc.player.setMotion(
                motion.x * reduceFactor,
                motion.y,
                motion.z * reduceFactor
        );
    }

    public static void sendPositionPacket() {
        mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(
                mc.player.getPosX(),
                mc.player.getPosY(),
                mc.player.getPosZ(),
                mc.player.rotationYaw,
                mc.player.rotationPitch,
                false
        ));
    }

    public static void sendSmallOffsetPacket() {
        double offset = 0.0000001;
        mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(
                mc.player.getPosX() + (Math.random() * offset * 2 - offset),
                mc.player.getPosY() + (Math.random() * offset * 2 - offset),
                mc.player.getPosZ() + (Math.random() * offset * 2 - offset),
                mc.player.rotationYaw,
                mc.player.rotationPitch,
                false
        ));
    }

    public static boolean shouldJump() {
        return mc.gameSettings.keyBindJump.isKeyDown() &&
                !mc.player.isOnGround() &&
                mc.player.getMotion().y <= 0;
    }
}