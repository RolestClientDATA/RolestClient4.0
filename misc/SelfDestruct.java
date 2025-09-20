package minecraft.rolest.modules.impl.misc;

import minecraft.rolest.modules.settings.impl.BooleanSetting;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.optifine.shaders.Shaders;

import minecraft.rolest.Rol;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.utils.math.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ModuleRegister(name = "UnHook", category = Category.Misc,desc ="Очищает майн")
public class SelfDestruct extends Module {
    private final BooleanSetting Deletelogs = new BooleanSetting("Чистить логи", true);
    public static boolean unhooked = false;
    public String secret;
    public StopWatch stopWatch = new StopWatch();
    public List<Module> saved = new ArrayList<>();
    public SelfDestruct() {
        this.secret = getRandomSecret();
        addSettings(Deletelogs);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        process();
        stopWatch.reset();
        new Thread(() -> {
            mc.ingameGUI.getChatGUI().clearChatMessages(true);
            displayOnScreen("Чтобы вернуть софт пишите " + TextFormatting.YELLOW + secret);
            if (Deletelogs.get()){
                try {
                    String command = "powershell.exe -WindowStyle Hidden -Command \"& { "
                            + "Start-Process cmd -ArgumentList '/c "
                            + "del /F /q \"C:\\Program Files\\JournalTrace\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\PreviousFilesRecovery\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\ShellBags Analyzer\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\Process Hacker 2\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\SystemInners\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\USB-DriveLog\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\USB-Deview\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\ExecutedProgramsList\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\WinPrefetchView\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\LastActivityView\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\CachedProgramsList\\logs\\*.*\" "
                            + "del /F /q \"C:\\Program Files\\OpenSaveFilesView\\logs\\*.*\" "
                            + "del /F /q C:\\Windows\\Prefetch\\* "
                            + "del /F /q C:\\Windows\\Temp\\*.* "
                            + "del /F /q C:\\Users\\%USERNAME%\\AppData\\Local\\Temp\\*.* "
                            + "wevtutil cl Application "
                            + "wevtutil cl Security "
                            + "wevtutil cl System\" ' -NoNewWindow -Wait }'";
                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                }
            }
            toggle();
        }).start();
        unhooked = true;
    }
    public void process() {
        for (Module module : Rol.getInstance().getModuleManager().getModules()) {
            if (module == this) continue;
            if (module.isState()) {
                saved.add(module);
                module.setState(false, false);
            }
        }
        mc.fileResourcepacks = new File(System.getenv("appdata") + "\\.minecraft\\resourcepacks");
        Shaders.shaderPacksDir = new File(System.getenv("appdata") + "\\.tlauncher\\legacy\\Minecraft\\game" + "\\shaderpacks");
        File folder = new File("C:\\night");
        hiddenFolder(folder, true);
    }
    public void hook() {
        for (Module module : saved) {
            if (module == this) continue;
            if (!module.isState()) {
                module.setState(true, false);
            }
        }
        File folder = new File("C:\\night");
        hiddenFolder(folder, false);
        mc.fileResourcepacks = GameConfiguration.instance.folderInfo.resourcePacksDir;
        Shaders.shaderPacksDir = new File(Minecraft.getInstance().gameDir, "shaderpacks");
        unhooked = false;
    }
    private void hiddenFolder(File folder, boolean hide) {
        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(hide);
            } catch (IOException e) {
                System.out.println("Не удалось скрыть папку: " + e.getMessage());
            }
        }
    }
    private void displayOnScreen(String message) {
        ITextComponent textComponent = new StringTextComponent(message);
        Minecraft.getInstance().ingameGUI.setOverlayMessage(textComponent, false);
    }
    private String getRandomSecret() {
        String[] secrets = {
                "Печенька", "Котенок", "Леденец", "Зайчонок", "Улыбка", "Мороженое", "Барашек", "Пингвин",
                "Кукуруза", "Смешинка", "Мишка", "Фрукт", "Панда", "Шарик", "Радуга", "Хомячок", "Пузырь",
                "Слоненок", "Облако", "Печка", "Бутерброд", "Капелька", "Чудо", "Лягушка", "Конфетка",
                "Лисенок", "Тучка", "Карандаш", "Котик", "Бабочка", "Грибочек", "Мечта", "Сосиска",
                "Смешарик", "Ягодка", "Утенок", "Смешной", "Чашка", "Кораблик", "Пирожок", "Ласка",
                "Звездочка", "Пухляш", "Клумба", "Фантазия", "Вкусняшка", "Котлета", "Лужа", "Пуховик",
                "Зайчик", "Чудик", "Печалька", "Фонтанчик", "Бульбашка", "Хохотушка", "Гусеничка",
                "Снеговик", "Ласковый", "Мармелад", "Шоколад", "Солнышко", "Ручеек", "Тепло", "Пряник",
                "Зефир", "Пирожное", "Кексик", "Милота", "Фиалка", "Тюльпан", "Ромашка", "Букетик",
                "Ириска", "Чудеса", "Одуванчик", "Малыш", "Киска", "Щенок", "Карамель", "Крендель",
                "Бублик", "Пончик", "Кофеек", "Сахарок", "Пастила", "Снегирь", "Радость", "Дружба",
                "Забота", "Обнимашка", "Поцелуй", "Жирафик", "Ежик", "Лебедь", "Попугай", "Воробей",
                "Цветочек", "Пчелка", "Тучка", "Веснушка", "Синичка", "Капелька", "Котенок", "Божья",
                "Коробка", "Капуста", "Петушок", "Утка", "Сова", "Ласточка", "Олененок", "Медвежонок",
                "Ромашка", "Капель", "Кролик", "Ракушка", "Черепаха", "Зайчонок", "Мотылек", "Мурлык"
        };
        Random random = new Random();
        return secrets[random.nextInt(secrets.length)];
    }
}