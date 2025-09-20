package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.TextFormatting;
import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;

import java.util.HashMap;
import java.util.Map;

@ModuleRegister(name = "PotionHelper", category = Category.Misc, desc = "Уведомляет в чат о зельях, полученных игроками")
public class PotionHelper extends Module {
    private static final Map<Effect, String> EFFECT_TRANSLATIONS = new HashMap<>() {{
        put(Effects.SPEED, "Скорость");
        put(Effects.STRENGTH, "Сила");
        put(Effects.INSTANT_HEALTH, "Мгновенное здоровье");
        put(Effects.RESISTANCE, "Сопротивление");
        put(Effects.HASTE, "Спешка");
        put(Effects.REGENERATION, "Регенерация");
        put(Effects.WITHER, "Иссушение");
        put(Effects.POISON, "Отравление");
    }};

    private final BooleanSetting skipFriends = new BooleanSetting("Пропускать друзей", true);
    private final BooleanSetting allEffects = new BooleanSetting("Все эффекты", false);
    private final Map<Effect, BooleanSetting> effectSettings = new HashMap<>() {{
        put(Effects.SPEED, new BooleanSetting("Скорость", true));
        put(Effects.STRENGTH, new BooleanSetting("Сила", true));
        put(Effects.INSTANT_HEALTH, new BooleanSetting("Мгновенное здоровье", true));
        put(Effects.RESISTANCE, new BooleanSetting("Сопротивление", true));
        put(Effects.HASTE, new BooleanSetting("Спешка", true));
        put(Effects.REGENERATION, new BooleanSetting("Регенерация", true));
        put(Effects.WITHER, new BooleanSetting("Иссушение", true));
        put(Effects.POISON, new BooleanSetting("Отравление", true));
    }};

    private final Map<String, Map<Effect, Integer>> playerPotionEffects = new HashMap<>();

    public PotionHelper() {
        // Автоматически включать все настройки эффектов, если включены "Все эффекты"
        allEffects.setVisible(() -> {
            if (allEffects.get()) {
                effectSettings.values().forEach(setting -> setting.set(true));
            }
            return true;
        });

        // Регистрация настроек
        addSettings(skipFriends, allEffects);
        addSettings(effectSettings.values().toArray(new BooleanSetting[0]));
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            String playerName = player.getName().getString();
            if (player == mc.player || (skipFriends.get() && FriendStorage.isFriend(playerName))) {
                continue;
            }

            Map<Effect, Integer> currentEffects = new HashMap<>();
            StringBuilder effectMessages = new StringBuilder();

            for (EffectInstance effectInstance : player.getActivePotionEffects()) {
                Effect effect = effectInstance.getPotion();
                int amplifier = effectInstance.getAmplifier() + 1;
                currentEffects.put(effect, amplifier);

                if (shouldNotify(effect) && !playerPotionEffects.getOrDefault(playerName, new HashMap<>()).containsKey(effect)) {
                    String effectName = EFFECT_TRANSLATIONS.getOrDefault(effect, effect.getName());
                    String duration = formatDuration(effectInstance.getDuration());
                    effectMessages.append(String.format("%s%s %d (%s)\n",
                            TextFormatting.GRAY, effectName, amplifier, duration));
                }
            }

            if (effectMessages.length() > 0) {
                String message = String.format("%s[%s] Получил эффекты:\n%s",
                        TextFormatting.RED, playerName, effectMessages.toString().trim());
                print(message);
            }

            playerPotionEffects.put(playerName, currentEffects);
        }
    }

    private boolean shouldNotify(Effect effect) {
        if (allEffects.get()) return true;
        return effectSettings.getOrDefault(effect, new BooleanSetting("Неизвестный", false)).get();
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return minutes > 0 ? String.format("%dм %02dс", minutes, seconds) : String.format("%dс", seconds);
    }
}