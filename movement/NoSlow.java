package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.events.NoSlowEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import lombok.ToString;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@ToString
@ModuleRegister(name = "NoSlowDown", category = Category.Movement,desc ="ест ходя")
public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Мод", "Matrix", "Matrix", "Funsky/Spooky", "FuntimeSnow", "NEW","Spooky");

    public NoSlow() {
        addSettings(mode);
    }

    int ticks = 0;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.isHandActive()) {
            ticks++;
        } else {
            ticks = 0;
        }

    }

    @Subscribe
    public void onEating(NoSlowEvent e) {
        handleEventUpdate(e);
    }


    private void handleEventUpdate(NoSlowEvent eventNoSlow) {
        if (mc.player.isHandActive()) {
            switch (mode.get()) {
                case "Funsky/Spooky" -> handleGrimACMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "FuntimeSnow" -> handleFunTimeMode(eventNoSlow);
                case "NEW" -> handleNewGrimMode(eventNoSlow);
                case "Spooky" -> handleNuckerMode(eventNoSlow);
            }
        }
    }

    public void handleFunTimeMode(NoSlowEvent noSlowEvent) {
        if (mc.player.isOnGround() && (mc.world.getBlockState(new BlockPos(mc.player.getPositionVec())).getBlock() instanceof CarpetBlock
                || mc.world.getBlockState(new BlockPos(mc.player.getPositionVec())).getBlock() instanceof SnowBlock)) {
            noSlowEvent.cancel();
        }
    }

    private void handleMatrixMode(NoSlowEvent eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.cancel();
        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.5F : 0.4F;
                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.95F : 0.97F;
            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }
    }

    private void handleGrimACMode(NoSlowEvent noSlow) {
        if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            noSlow.cancel();
            return;
        }

        noSlow.cancel();
        sendItemChangePacket();
    }

    private void sendItemChangePacket() {
    }

    private void handleNewGrimMode(NoSlowEvent e) {
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;

        if (!(mc.player.getItemInUseCount() < 25 && mc.player.getItemInUseCount() > 4) &&
                mc.player.getHeldItemOffhand().getItem() != Items.SHIELD) {
            return;
        }

        if (mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();

            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old));
                mc.player.setSprinting(false);
                e.cancel();
            }

            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));

                if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.NONE) {
                    e.cancel();
                }
            }

            mc.playerController.syncCurrentPlayItem();
        }
    }

    private long lastPacketTime = 0;
    private int noSlowCooldown = 0;

    private void handleNuckerMode(NoSlowEvent eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.9;
        float speedMultiplier;
        eventNoSlow.cancel();
        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 1 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.45F : 0.458F;
                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.19F : 0.29F;
            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }
    }
}
