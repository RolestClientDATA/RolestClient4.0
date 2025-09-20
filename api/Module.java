package minecraft.rolest.modules.api;

import minecraft.rolest.modules.impl.combat.HitAura;
import minecraft.rolest.modules.impl.misc.ClientTune;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

import minecraft.rolest.Rol;
import minecraft.rolest.modules.settings.Setting;
import minecraft.rolest.utils.client.ClientUtil;
import minecraft.rolest.utils.client.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Module implements IMinecraft {
    final String name;
    final String desc;
    final Category category;
    boolean state;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Module() {
        this.name = getClass().getAnnotation(ModuleRegister.class).name();
        this.desc = getClass().getAnnotation(ModuleRegister.class).desc();
        this.category = getClass().getAnnotation(ModuleRegister.class).category();
        this.bind = getClass().getAnnotation(ModuleRegister.class).key();
    }

    public Module(String name, String desc, Category category) {
        this.name = name;
        this.category = category;
        this.desc = desc;
    }


    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public List<Setting<?>> getAllValues() {
        java.util.ArrayList<Setting<?>> allValues = new java.util.ArrayList<>();

        settings.forEach(value -> {
            allValues.add((Setting<?>) settings);
        });

        return allValues;
    }



    public void onEnable() {
       animation.animate(1, 0f, Easings.CIRC_OUT);
        Rol.getInstance().getEventBus().register(this);
    }

    ModuleManager moduleManager = Rol.getInstance().getModuleManager();
    HitAura hitAura = moduleManager.getHitAura();

    public void onDisable() {
       animation.animate(0, 0f, Easings.CIRC_OUT);
      Rol.getInstance().getEventBus().unregister(this);



    }


    public void toggle() {
        setState(!state, false);

    }



    public final void setState(boolean newState, boolean config) {
        if (state == newState) {
            return;
        }

        state = newState;

        try {
            if (state) {
                onEnable();

                NotifyNigt.NOTIFICATION_MANAGER.add(this.name + " включен.", "", 2);
            } else {
                onDisable();

                NotifyNigt.NOTIFICATION_MANAGER.add(this.name + " выключен.", "", 2);
            }


            if (!config) {
                ModuleManager moduleManager = Rol.getInstance().getModuleManager();
                ClientTune clientTune = moduleManager.getClientTune();
                if (clientTune != null && clientTune.isState()) {
                    String fileName = clientTune.getFileName(state);
                    float volume = clientTune.volume.get();
                    ClientUtil.playSound(fileName, volume, false);
                }

            }
        } catch (Exception e) {
            handleException(state ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику: " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
