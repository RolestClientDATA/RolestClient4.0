package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import net.minecraft.network.play.client.CPlayerPacket;


@ModuleRegister(name = "KTLeave", category = Category.Misc , desc = "ктлив фанскай")
public class KTLeave extends Module {


    String ktleave = "[KTLeave]";
    String prefix = " - ";

    public KTLeave() {
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (e instanceof EventUpdate) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(Float.NaN, Float.NaN, true));
            toggle();
        }

    }
}
