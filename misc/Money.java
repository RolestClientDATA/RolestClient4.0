package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.modules.api.Module;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ModuleRegister(
        name = "MoneyBegger",
        category = Category.Misc
)
public class Money extends Module {
    private final StopWatch messageTimer = new StopWatch();
    private final StopWatch commandTimer = new StopWatch();
    private int currentIndex = 0;
    private final Random random = new Random();
    private final String[] commands = {"/an311", "/an505", "/an312", "/an602", "/an603","/an503","/an102","/an104","/an105","/an507","/an304","/an305","/an306"};

    @Subscribe
    public void onUpdate(EventUpdate e) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().getConnection() == null) return;

        if (messageTimer.isReached(60000L)) {
            List<String> players = Minecraft.getInstance().getConnection().getPlayerInfoMap().stream()
                    .map(NetworkPlayerInfo::getGameProfile)
                    .map(profile -> profile.getName())
                    .filter(name -> !name.equalsIgnoreCase(player.getScoreboardName()))
                    .collect(Collectors.toList());

            if (!players.isEmpty()) {
                if (currentIndex >= players.size()) {
                    currentIndex = 0;
                }
                String targetPlayer = players.get(currentIndex);
                int randomAmount = 50000 + random.nextInt(50000);
                player.sendChatMessage("/msg " + targetPlayer + " Дай пожалуйста " + randomAmount + ", не хватает на мечту умоляю , Клянусь аллахом верну");
                currentIndex++;
            }
            messageTimer.reset();
        }

        if (commandTimer.isReached(1800000L)) { // 30 минут = 1 800 000 мс
            String randomCommand = commands[random.nextInt(commands.length)];
            player.sendChatMessage(randomCommand);
            commandTimer.reset();
        }
    }
}
