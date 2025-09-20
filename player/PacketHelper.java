package minecraft.rolest.utils.player;

import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

@UtilityClass
public class PacketHelper {
    private static final Minecraft mc = Minecraft.getInstance();
    private static double lastReportedX;
    private static double lastReportedY;
    private static double lastReportedZ;
    private static float lastReportedYaw;
    private static float lastReportedPitch;
    private static boolean lastOnGround;

    public static void modifyPositionPacket(CPlayerPacket packet, Vector3d targetPos, float targetYaw, float targetPitch, boolean onGround, double speed, double jitterStrength, boolean antiDesync) {

        // Получаем текущие reported позиции из packet или из наших статических переменных
        double currentX = lastReportedX;
        double currentY = lastReportedY;
        double currentZ = lastReportedZ;
        float currentYaw = lastReportedYaw;
        float currentPitch = lastReportedPitch;
        boolean currentOnGround = lastOnGround;

        // Вычисляем разницу между целью и текущей позицией
        double deltaX = targetPos.x - currentX;
        double deltaY = targetPos.y - currentY;
        double deltaZ = targetPos.z - currentZ;
        float deltaYaw = targetYaw - currentYaw;
        float deltaPitch = targetPitch - currentPitch;

        // Нормализуем углы для правильного вычисления разницы
        while (deltaYaw > 180F) deltaYaw -= 360F;
        while (deltaYaw < -180F) deltaYaw += 360F;
        while (deltaPitch > 180F) deltaPitch -= 360F;
        while (deltaPitch < -180F) deltaPitch += 360F;

        // Применяем скорость (линейная интерполяция)
        double newX = currentX + deltaX * speed;
        double newY = currentY + deltaY * speed;
        double newZ = currentZ + deltaZ * speed;
        float newYaw = currentYaw + deltaYaw * (float) speed;
        float newPitch = currentPitch + deltaPitch * (float) speed;

        // Добавляем микро-джиттер для обхода паттернов
        newX += (Math.random() - 0.5) * jitterStrength;
        newY += (Math.random() - 0.5) * jitterStrength * 0.5; // Меньший джиттер по Y
        newZ += (Math.random() - 0.5) * jitterStrength;
        newYaw += (Math.random() - 0.5) * jitterStrength * 10f;
        newPitch += (Math.random() - 0.5) * jitterStrength * 10f;

        // Анти-десинк: иногда "проигрываем" лаг, отправляя старые позиции
        if (antiDesync && Math.random() < 0.05) { // 5% chance
            newX = currentX;
            newY = currentY;
            newZ = currentZ;
            newYaw = currentYaw;
            newPitch = currentPitch;
        }

        // Обновляем статические переменные
        lastReportedX = newX;
        lastReportedY = newY;
        lastReportedZ = newZ;
        lastReportedYaw = newYaw;
        lastReportedPitch = newPitch;
        lastOnGround = onGround;

        // Модифицируем пакет через рефлексию (это нужно сделать правильно)
        try {
            java.lang.reflect.Field xField = CPlayerPacket.class.getDeclaredField("x");
            java.lang.reflect.Field yField = CPlayerPacket.class.getDeclaredField("y");
            java.lang.reflect.Field zField = CPlayerPacket.class.getDeclaredField("z");
            java.lang.reflect.Field yawField = CPlayerPacket.class.getDeclaredField("yaw");
            java.lang.reflect.Field pitchField = CPlayerPacket.class.getDeclaredField("pitch");
            java.lang.reflect.Field onGroundField = CPlayerPacket.class.getDeclaredField("onGround");

            xField.setAccessible(true);
            yField.setAccessible(true);
            zField.setAccessible(true);
            yawField.setAccessible(true);
            pitchField.setAccessible(true);
            onGroundField.setAccessible(true);

            xField.set(packet, newX);
            yField.set(packet, newY);
            zField.set(packet, newZ);
            yawField.set(packet, newYaw);
            pitchField.set(packet, newPitch);
            onGroundField.set(packet, onGround);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reset() {
        lastReportedX = mc.player.getPosX();
        lastReportedY = mc.player.getPosY();
        lastReportedZ = mc.player.getPosZ();
        lastReportedYaw = mc.player.rotationYaw;
        lastReportedPitch = mc.player.rotationPitch;
        lastOnGround = mc.player.onGround;
    }
}