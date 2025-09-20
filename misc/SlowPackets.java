package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import net.minecraft.network.IPacket;

import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleRegister(name = "SlowPackets", category = Category.Misc)
public class SlowPackets extends Module
{
    public static class TimedPacket {
        private final IPacket<?> packet;
        private final long time;

        public TimedPacket(final IPacket<?> packet, final long time) {
            this.packet = packet;
            this.time = time;
        }

        public IPacket<?> getPacket() {
            return packet;
        }

        public long getTime() {
            return time;
        }
    }

    private SliderSetting delay = new SliderSetting("Задержка", 1000,100,5000, 100);

    public SlowPackets() {
        addSettings(delay);
    }

    public static final ConcurrentLinkedQueue<TimedPacket> packets = new ConcurrentLinkedQueue<>();

    @Override
    public void onDisable() {
        super.onDisable();
        for (TimedPacket p : packets) {
            mc.player.connection.getNetworkManager().sendPacketWithoutEvent(p.getPacket());
        }
        packets.clear();

    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isSend()) {
            IPacket<?> packet = e.getPacket();
            packets.add(new TimedPacket(packet, System.currentTimeMillis()));
            e.cancel();
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        for (TimedPacket timedPacket : packets) {
            if (System.currentTimeMillis() - timedPacket.getTime() >= delay.get().intValue()) {
                mc.player.connection.getNetworkManager().sendPacketWithoutEvent(timedPacket.getPacket());
                packets.remove(timedPacket);
            }
        }
    }
}
