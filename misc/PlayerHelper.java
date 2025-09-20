package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventEntityLeave;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@ModuleRegister(name = "PlayerHelper", category = Category.Player)
public class PlayerHelper extends Module {

    public final BooleanSetting portalgodmode = new BooleanSetting("PortalGodMode", false);
    public final BooleanSetting srpspoofer = new BooleanSetting("SrpSpoofer", false);
    public final BooleanSetting leaveTracker = new BooleanSetting("LeaveTracker", true);
    public final BooleanSetting speedmine = new BooleanSetting("SpeedMine", false);
    public BooleanSetting ultraFast = new BooleanSetting("Мгновенно", false).setVisible(() -> speedmine.get());

    public final BooleanSetting deathPosition = new BooleanSetting("DeathPosition", false);
    public BooleanSetting autoGPS = new BooleanSetting("Авто GPS", false).setVisible(() -> deathPosition.get());
    public BooleanSetting autoWAY = new BooleanSetting("Авто Way", false).setVisible(() -> deathPosition.get());

    public PlayerHelper() {
        addSettings(portalgodmode, srpspoofer, leaveTracker, speedmine, ultraFast, deathPosition, autoGPS, autoWAY);
    }

    @Subscribe
    private void onEntityLeave(EventEntityLeave eel) {
        if (leaveTracker.get()) {
            Entity entity = eel.getEntity();

            if (!isEntityValid(entity)) {
                return;
            }

            String message = "Игрок " + entity.getDisplayName().getString() + " ливнул на " + entity.getStringPosition();

            print(message);
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CConfirmTeleportPacket && portalgodmode.get()) {
            e.cancel();
        }

        if (e.getPacket() instanceof SSendResourcePackPacket && srpspoofer.get()) {
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
            if (mc.currentScreen != null) {
                mc.player.closeScreen();
            }

            e.cancel();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (speedmine.get()) {
            mc.playerController.blockHitDelay = 0;
            if (!ultraFast.get()) mc.playerController.resetBlockRemoving();
            if (ultraFast.get() && mc.player.isOnGround()) {
                mc.playerController.curBlockDamageMP = 1;
            }
        }
    }

    private boolean isEntityValid(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity) || entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !(mc.player.getDistance(entity) < 100);
    }
}
