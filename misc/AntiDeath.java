package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.modules.settings.impl.StringSetting;


@ModuleRegister(name = "AntiDeath", category = Category.Movement)
public class AntiDeath extends Module {
    public ModeSetting mod = new ModeSetting("Режим", "Автоматический", "Автоматический", "По бинду");
    private final StringSetting home = new StringSetting("Название хома", "", "название", false);
    public ModeSetting mod_save = new ModeSetting("Режим сейва", "Никакой", "Никакой" , "Хаб", "Кик");
    private final SliderSetting health_limit = new SliderSetting("Лимит здоровья", 4.0f, 1.0f, 19.5f, 0.5f);
    final BindSetting save = new BindSetting("Возвродиться", -1);
    boolean state = false;
    private long death_time = 0;
    private boolean joined = false;
    private BlockPos deathpos = null;
    private BlockPos spawn = new BlockPos(0, 90, 0);
    public AntiDeath() {
        addSettings(mod, mod_save,home, save, health_limit, kd);
    }
    private final SliderSetting kd = new SliderSetting("Задержка перед выходом", 1.0f, 0.1f, 4.0f, 0.1f);
    @Subscribe
    public void onUpdate(EventMotion e) {
        if(!mod_save.is("Никакой")) kd.setVisible(() -> true); else kd.setVisible(() -> false);
        if(mod_save.is("Клан хом")) home.setVisible(() -> true); else home.setVisible(() -> false);
        if (mod.is("По бинду")) save.setVisible(() -> true);
        else save.setVisible(() -> false);
        if (mod.is("По бинду")) health_limit.setVisible(() -> false);
        else health_limit.setVisible(() -> true);
        if (mc.player.getHealth() <= health_limit.get() && mod.is("Автоматический") && mc.player != null) {
            if(deathpos == null){
                deathpos = mc.player.getPosition();
            }
            mc.player.motion.y = 1.0;
            e.setOnGround(true);
            mc.player.setOnGround(true);
        }

        if (!mod.is("Автоматический") && state && mc.player != null) {
            if(deathpos == null){
                deathpos = mc.player.getPosition();
            }
            mc.player.motion.y = 1.0;
            e.setOnGround(true);
            mc.player.setOnGround(true);
        }

        if (death_time > 0 && System.currentTimeMillis() - death_time >= (long)(kd.get() * 1000)) {
            leave();
            death_time = -1;
        }
    }
    private void leave(){
        if(mod_save.is("Никакой"))return;
        if(mod_save.is("Хаб")){
            mc.player.sendChatMessage("/hub");
        }
        if(mod_save.is("Кик")){
            mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("Успешно"));
        }
        death_time = -1;
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SRespawnPacket packet) {
            if(joined){
                joined = false;
                return;
            }
            if (deathpos != null && mc.player != null) {
                BlockPos currentPos = mc.player.getPosition();
                double distanceSq = deathpos.distanceSq(currentPos.getX(), currentPos.getY(), currentPos.getZ(), false);
                double distanceToSpawnSq = spawn.distanceSq(currentPos.getX(), currentPos.getY(), currentPos.getZ(), false);
                double distance = MathHelper.sqrt(distanceSq);
                if (distance > 10) {
                    deathpos = null;
                    print("Слишком далеко от точки смерти");
                    return;
                }
                if(distanceToSpawnSq <= Math.pow(20, 2)){
                    print("Слишком далеко от точки смерти");
                    return;
                }
                deathpos = null;
            }
            death_time = System.currentTimeMillis();
        }
        if(e.getPacket() instanceof SJoinGamePacket packet){
            joined = true;
            deathpos = null;
        }
    }
    @Subscribe
    private void onEventKey(EventKey e) {
        if (e.getKey() == save.get() && save.visible.get()) {
            if (state) {
                state = false;
                print("Выключено");
            } else {
                state = true;
                print("Включено");
            }
        }
    }
    private int getAnarchyServerNumber() {
        if (mc.ingameGUI.getTabList().header != null) {
            String serverHeader = TextFormatting.getTextWithoutFormattingCodes(mc.ingameGUI.getTabList().header.getString());
            if (serverHeader != null && serverHeader.contains("Анархия-")) {
                return Integer.parseInt(serverHeader.split("Анархия-")[1].trim());
            }
        }
        return -1;
    }
    @Override
    public void onDisable() {
        super.onDisable();
        death_time = -1;
        joined = false;
    }


}