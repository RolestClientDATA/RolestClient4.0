package minecraft.rolest.modules.impl.combat;


import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;

@ModuleRegister(
        name = "TridentAim",
        category = Category.Combat
)
public class TridentAuto extends Module {
    private Entity target = null;
    private int throwTimer = 0;
    private boolean wasActive = false;
    public static final ModeListSetting targetFilter = new ModeListSetting("Цели",
            new BooleanSetting("Голые", true),
            new BooleanSetting("Алмазники", true),
            new BooleanSetting("Незеритовики", true));

    public TridentAuto() {
        addSettings(targetFilter);
    }

    @Subscribe
    private void onMotion(EventMotion event) {
        if (!this.isState()) {
            this.resetState();
        } else if (mc.player.getHeldItemMainhand().getItem() != Items.TRIDENT) {
            this.resetState();
        } else {
            this.wasActive = true;
            this.target = this.findTarget();
            if (this.target != null) {
                this.aimAtTarget(event, this.target);
                this.handleThrow();
            }
        }
    }

    private Entity findTarget() {
        PlayerEntity closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        boolean targetNaked = targetFilter.getValueByName("Голые").get();
        boolean targetDiamond = targetFilter.getValueByName("Алмазники").get();
        boolean targetNetherite = targetFilter.getValueByName("Незеритовики").get();

        for(PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isAlive()) {
                double distance = (double)mc.player.getDistance(player);
                if (!(distance >= closestDistance) && !(distance > (double)20.0F)) {
                    boolean isNaked = true;
                    boolean hasDiamond = false;
                    boolean hasNetherite = false;

                    for(EquipmentSlotType slot : new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET}) {
                        ItemStack armor = player.getItemStackFromSlot(slot);
                        if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem) {
                            isNaked = false;
                            String material = ((ArmorItem)armor.getItem()).getArmorMaterial().toString().toLowerCase();
                            if (material.contains("diamond")) {
                                hasDiamond = true;
                            } else if (material.contains("netherite")) {
                                hasNetherite = true;
                            }
                        }
                    }

                    boolean isValidTarget = targetNaked && isNaked || targetDiamond && hasDiamond || targetNetherite && hasNetherite;
                    if (isValidTarget) {
                        closestDistance = distance;
                        closestPlayer = player;
                    }
                }
            }
        }

        return closestPlayer;
    }

    private void aimAtTarget(EventMotion event, Entity target) {
        Vector3d playerPos = mc.player.getEyePosition(1.0F);
        Vector3d targetPos = target.getPositionVec().add((double)0.0F, (double)target.getHeight() * 0.3, (double)0.0F);
        Vector3d targetMotion = target.getMotion();
        float tridentSpeed = 2.5F;
        float gravity = 0.05F;
        double dX = targetPos.x - playerPos.x;
        double dY = targetPos.y - playerPos.y;
        double dZ = targetPos.z - playerPos.z;
        double horizontalDistance = (double) MathHelper.sqrt(dX * dX + dZ * dZ);
        double flightTime = horizontalDistance / (double)tridentSpeed;
        if (targetMotion.lengthSquared() > 0.01) {
            targetPos = targetPos.add(targetMotion.scale(flightTime));
            dX = targetPos.x - playerPos.x;
            dY = targetPos.y - playerPos.y;
            dZ = targetPos.z - playerPos.z;
            horizontalDistance = (double)MathHelper.sqrt(dX * dX + dZ * dZ);
        }

        if (horizontalDistance > (double)2.0F) {
            dY += (double)0.5F * (double)gravity * flightTime * flightTime;
        }

        float yaw = (float)(MathHelper.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        float pitch = (float)(-(MathHelper.atan2(dY, horizontalDistance) * (180D / Math.PI)));
        event.setYaw(yaw);
        event.setPitch(pitch);
    }

    private void handleThrow() {
        if (this.throwTimer >= 5) {
            if (mc.player.isHandActive() && mc.player.getItemInUseCount() >= 8) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                mc.player.stopActiveHand();
                this.throwTimer = 0;
            }
        } else {
            ++this.throwTimer;
        }

        if (!mc.player.isHandActive() && this.throwTimer == 1) {
            mc.player.setActiveHand(Hand.MAIN_HAND);
        }

    }

    private void resetState() {
        if (this.wasActive) {
            this.target = null;
            this.throwTimer = 0;
            if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT) {
                mc.player.stopActiveHand();
            }

            this.wasActive = false;
        }

    }
}

