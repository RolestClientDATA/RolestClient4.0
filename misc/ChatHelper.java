package minecraft.rolest.modules.impl.misc;

import minecraft.rolest.modules.settings.impl.*;
import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;

import minecraft.rolest.utils.math.StopWatch;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "ChatHelper", category = Category.Misc,desc ="чат помощник")
public class ChatHelper extends Module {

    public final BooleanSetting autoAuth = new BooleanSetting("Авто логин", true);
    public final ModeSetting loginMode = new ModeSetting("Пароль", "tryhackmecom1337", "tryhackmecom1337", "1t_M4kes_N0n_S3ns3", "Custom").setVisible(() -> autoAuth.get());
    public final StringSetting customPassword = new StringSetting("Кастомный пароль", "123123123qwe", "Укажите текст для вашего пароля").setVisible(() -> autoAuth.get() && loginMode.is("Custom"));

    public final BooleanSetting autoText = new BooleanSetting("Авто текст", false);
    public final BindSetting textBind = new BindSetting("Кнопка", -1).setVisible(() -> autoText.get());
    public final ModeSetting textMode = new ModeSetting("Текст", "Корды", "Корды", "ezz", "Custom").setVisible(() -> autoText.get());
    public final StringSetting customText = new StringSetting("Кастомный текст", "жди сват", "Укажите текст").setVisible(() -> autoText.get() && textMode.is("Custom"));
    public final BooleanSetting globalChat = new BooleanSetting("Писать в глобал", true).setVisible(() -> autoText.get());

    public final BooleanSetting spammer = new BooleanSetting("Cпаммер", false);
    public final SliderSetting spamDelay = new SliderSetting("Задержка в секундах", 5, 1, 30, 0.5f).setVisible(() -> spammer.get());
    public final StringSetting spammerText = new StringSetting("Кастомный текст", "жди сват", "Укажите текст").setVisible(() -> spammer.get());
    public final BooleanSetting emoji = new BooleanSetting("Эмодзи", false);
    public List<String> lastMessages = new ArrayList<>();
    public String password;
    public StopWatch stopWatch = new StopWatch();

    public ChatHelper() {
        addSettings(autoAuth, loginMode, customPassword, autoText, textBind, textMode, customText, globalChat, spammer, spamDelay, spammerText, emoji);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!autoAuth.get() && !autoAuth.get() && !spammer.get()) {
            toggle();
        }

        if (loginMode.is("tryhackmecom1337")) {
            password = "tryhackmecom1337";
        } else if (loginMode.is("1t_M4kes_N0n_S3ns3")) {
            password = "1t_M4kes_N0n_S3ns3";
        } else if (loginMode.is("Custom")) {
            password = customPassword.get();
        }

        if (spammer.get()) {
            if (stopWatch.isReached(spamDelay.get().longValue() * 1000))
                mc.player.sendChatMessage(spammerText.get());
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (mc.player == null) return;
            if (e.getPacket() instanceof SChatPacket wrapper) {
                if (lastMessages.size() > 10) lastMessages.remove(0);
                lastMessages.add(wrapper.getChatComponent().getString());

                List<String> acceptWords = List.of("reg", "register", "Зарегистрируйтесь", "/reg Пароль ПовторитеПароль");

                List<String> loginWords = List.of("login", "/l", "Авторизуйтесь", "/login Пароль","/login Пароль Пароль","/login <Пароль>", "/login <password>","/login password");


                boolean containsWords = false;

                for (String lastMessage : lastMessages) {
                    for (String acceptWord : acceptWords) {
                        if (!containsWords) {
                            containsWords = lastMessage.contains(acceptWord);
                        }
                    }
                }

                boolean containsLoginWords = false;

                for (String lastMessage : lastMessages) {
                    for (String acceptWord : loginWords) {
                        if (!containsWords) {
                            containsLoginWords = lastMessage.contains(acceptWord);
                        }
                    }
                }

                boolean containsRegister = containsWords;

                boolean containsLogin = containsLoginWords;

                String emptyField = "Вы не указали пароль, регистрация под паролем " + TextFormatting.GREEN + "qweasdzxc";
                String success = "Ваш аккаунт был успешно зарегистрирован";
                String successLogin = "Авторизация успешно пройдена";
                if (containsLogin || containsRegister) {
                    if (password == null || password.equals("")) {
                        assert mc.player != null;
                        if (containsRegister) {
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                print(emptyField);
                                mc.player.sendChatMessage("/register " + "qweasdzxc123 qweasdzxc123");
                            }
                        }
                        if (containsLogin){
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                print(getName() + ": Я не знаю ваш пароль!");
                            }
                        }
                        lastMessages.clear();
                    } else {
                        assert mc.player != null;
                        if (containsRegister) {
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                mc.player.sendChatMessage("/register " + password + " " + password);

                            }
                        }
                        if (containsLogin){
                            if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                                mc.player.sendChatMessage("/login " + password);
                            }
                        }
                        lastMessages.clear();
                        GLFW.glfwSetClipboardString(Minecraft.getInstance().getMainWindow().getHandle(), password);
                        print("Пароль скопировам в буфер обмена!");
                    }
                }
            }
        }
    }

    @Subscribe
    public void onKeyPress(EventKey e) {
        if (e.getKey() == textBind.get() && autoText.get()) {
            String text = globalChat.get() ? "!" : "";
            String pos = (int) (mc.player.getPosX()) + ", " + (int) (mc.player.getPosY()) + ", " + (int) (mc.player.getPosZ());
            if (textMode.is("Корды")) {
                mc.player.sendChatMessage(text + pos);
            } if (textMode.is("ezz")) {
                mc.player.sendChatMessage(text + "ezz");
            } if (textMode.is("Custom")) {
                mc.player.sendChatMessage(text + customText.get());
            }
        }
    }
}
