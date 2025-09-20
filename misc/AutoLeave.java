package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.player.PlayerUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "AutoLeave", category = Category.Player,desc ="автолив")
public class AutoLeave extends Module {

    final ModeSetting action = new ModeSetting("Действие", "Kick", "Kick", "/hub", "/spawn", "/home");
    final SliderSetting distance = new SliderSetting("Дистанция", 50.0f, 1.0f, 100.0f, 1.0f);

    public AutoLeave() {
        addSettings(action, distance);
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        mc.world.getPlayers().stream()
                .filter(this::isValidPlayer)
                .findFirst()
                .ifPresent(this::performAction);
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player.isAlive()
                && player.getHealth() > 0.0f
                && player.getDistance(mc.player) <= distance.get()
                && player != mc.player
                && PlayerUtils.isNameValid(player.getName().getString())
                && !FriendStorage.isFriend(player.getName().getString());
    }

    private void performAction(PlayerEntity player) {
        if (!action.get().equalsIgnoreCase("Kick")) {
            mc.player.sendChatMessage(action.get());
            mc.ingameGUI.func_238452_a_(new StringTextComponent("[AutoLeave] " + player.getGameProfile().getName()),
                    new StringTextComponent("test"), -1,
                    -1, -1);
        } else {
            mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("Вы вышли с сервера! \n" + player.getGameProfile().getName()));
        }
        toggle();
    }
}
