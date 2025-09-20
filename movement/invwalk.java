package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.InventoryCloseEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.MoveUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "InvMove", category = Category.Movement)
public class invwalk extends Module {

    private final List<IPacket<?>> delayedPackets = new ArrayList<>();
    public final BooleanSetting ftBypass = new BooleanSetting("Funtime Bypass", false);
    private final StopWatch delayTimer = new StopWatch();

    public invwalk() {
        addSettings(ftBypass);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (mc.player == null) return;

        if (isScreenBlockingMovement()) return;

        KeyBinding[] keys = {
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint
        };

        if (ftBypass.get() && !delayTimer.isReached(200)) {
            for (KeyBinding key : keys) key.setPressed(false);
            return;
        }

        updateKeyStates(keys);
    }

    @Subscribe
    public void onPacketSend(EventPacket e) {
        if (!ftBypass.get()) return;

        if (e.getPacket() instanceof CClickWindowPacket packet &&
                mc.currentScreen instanceof InventoryScreen &&
                MoveUtils.isMoving()) {

            delayedPackets.add(packet);
            e.cancel();
        }
    }

    @Subscribe
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!ftBypass.get()) return;

        if (mc.currentScreen instanceof InventoryScreen &&
                !delayedPackets.isEmpty() &&
                MoveUtils.isMoving()) {

            delayTimer.reset();

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    for (IPacket<?> p : delayedPackets) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    delayedPackets.clear();
                }
            }).start();

            e.cancel();
        }
    }

    private void updateKeyStates(KeyBinding[] keys) {
        for (KeyBinding key : keys) {
            boolean down = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), key.getDefault().getKeyCode());
            key.setPressed(down);
        }
    }

    private boolean isScreenBlockingMovement() {
        return mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen;
    }
}
