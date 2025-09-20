package minecraft.rolest.modules.impl.combat;

import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import com.google.common.eventbus.Subscribe;

@ModuleRegister(name = "NoFriendD", category = Category.Combat,desc ="не аттакует врага")
public class NoFriendHurt extends Module {
	
	// дайте мне по роже пж
	
	@Subscribe
    public void onEvent(EventPacket event) {
        if (event.getPacket() instanceof CUseEntityPacket) {
            CUseEntityPacket cUseEntityPacket = (CUseEntityPacket) event.getPacket();
            Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
            if (entity instanceof RemoteClientPlayerEntity &&
                    FriendStorage.isFriend(entity.getName().getString()) &&
                    cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                event.cancel();
            }
        }
    }
	
}
