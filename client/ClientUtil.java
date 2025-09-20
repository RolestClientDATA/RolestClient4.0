package minecraft.rolest.utils.client;

import minecraft.rolest.ui.mainmenu.AltScreen;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import minecraft.rolest.Rol;
import minecraft.rolest.ui.mainmenu.MainScreen;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.UUID;

import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@UtilityClass
public class ClientUtil implements IMinecraft {

    public static String getUsername() {
        return System.getProperty("user.name");
    }

    public static String getGreetingMessage() {
        LocalTime currentTime = LocalTime.now();

        if (currentTime.isBefore(LocalTime.of(6, 0))) {
            return "Доброй ночи";
        } else if (currentTime.isBefore(LocalTime.NOON)) {
            return "Доброе утро" ;
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            return "Добрый день";
        } else {
            return "Добрый вечер";
        }
    }   public boolean ctrlIsDown() {
        return GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public String pasteString() {
        return GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
    }
    private static Clip currentClip = null;
    private static boolean pvpMode;
    private static UUID uuid;

    public static String state = "";
    public static String alt = "";
    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;

      public static void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize("1357416731257077911", eventHandlers, true, null);
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageText = "Версия: 1.16.5" + "| Билд: " + Rol.build;
        discordRPC.Discord_UpdatePresence(discordRichPresence);


        new Thread(() -> {
            while (true) {
                try {
                    if (mc.currentScreen instanceof MainMenuScreen || mc.currentScreen instanceof MainScreen) {
                        state = "В главном меню";
                    } else if (mc.currentScreen instanceof MultiplayerScreen) {
                        state = "Выбирает сервер";  
                    } else if (mc.isSingleplayer()) {
                        state = "В одиночном мире";
                    } else if (mc.getCurrentServerData() != null) {
                        state = "Играет на " + mc.getCurrentServerData().serverIP.replace("mc.", "").replace("play.", "").replace("gg.", "").replace("go.", "").replace("join.", "").replace("creative.", "")
                                .replace(".top", "").replace(".ru", "").replace(".cc", "").replace(".space", "").replace(".eu", "").replace(".com", "").replace(".net", "").replace(".xyz", "").replace(".gg", "").replace(".me", "").replace(".su", "").replace(".fun", "").replace(".org", "").replace(".host", "")
                                .replace("localhost", "LocalServer").replace(":25565", "")
                        ;
                    } else if (mc.currentScreen instanceof OptionsScreen) {
                        state = "В настройках";
                    } else if (mc.currentScreen instanceof WorldSelectionScreen) {
                        state = "Выбирает мир";
                    } else if (mc.currentScreen instanceof AltScreen) {
                        state = "В меню выбора аккаунтов";
                    } else {
                        state = "Загрузка...";
                    }
//https://s3.gifyu.com/images/bSSNS.gif

                    discordRichPresence.largeImageKey = "1338248722081120348";
                    discordRichPresence.details = state;
                    discordRichPresence.state = "Моды: " + Rol.getInstance().getModuleManager().countEnabledModules() + "/" + Rol.getInstance().getModuleManager().getModules().size();
                    discordRPC.Discord_UpdatePresence(discordRichPresence);

                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }
            }
        }).start();
    }

    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }

    public void updateBossInfo(SUpdateBossInfoPacket packet) {
        if (packet.getOperation() == SUpdateBossInfoPacket.Operation.ADD) {
            if (StringUtils.stripControlCodes(packet.getName().getString()).toLowerCase().contains("pvp")) {
                pvpMode = true;
                uuid = packet.getUniqueId();
            }
        } else if (packet.getOperation() == SUpdateBossInfoPacket.Operation.REMOVE) {
            if (packet.getUniqueId().equals(uuid))
                pvpMode = false;
        }
    }
    public boolean isConnectedToServer(String ip) {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && mc.getCurrentServerData().serverIP.contains(ip);
    }
    public boolean isPvP() {
        return pvpMode;
    }


    public void playSound(String sound, float value, boolean nonstop) {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
        try {
            currentClip = AudioSystem.getClip();
            InputStream is = mc.getResourceManager().getResource(new ResourceLocation("rolka/sounds/" + sound + ".wav")).getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
            if (audioInputStream == null) {
                System.out.println("Sound not found!");
                return;
            }

            currentClip.open(audioInputStream);
            currentClip.start();
            FloatControl floatControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = floatControl.getMinimum();
            float max = floatControl.getMaximum();
            float volumeInDecibels = (float) (min * (1 - (value / 100.0)) + max * (value / 100.0));
            floatControl.setValue(volumeInDecibels);
            if (nonstop) {
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        currentClip.setFramePosition(0);
                        currentClip.start();
                    }
                });
            }
        } catch (Exception exception) {
            // Обработка исключения
            exception.printStackTrace();
        }
    }

    public void stopSound() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    public int calc(int value) {
        MainWindow rs = mc.getMainWindow();
        return (int) (value * rs.getGuiScaleFactor() / 2);
    }

    public Vec2i getMouse(int mouseX, int mouseY) {
        return new Vec2i((int) (mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2), (int) (mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2));
    }

    public static boolean isPvp() {
        return false;
    }
}
