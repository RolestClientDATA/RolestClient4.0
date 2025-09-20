package minecraft.rolest.modules.api;

import minecraft.rolest.modules.impl.combat.*;
import minecraft.rolest.modules.impl.misc.*;
import minecraft.rolest.modules.impl.movement.*;
import minecraft.rolest.modules.impl.player.Fly;
import minecraft.rolest.modules.impl.render.*;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;
import minecraft.rolest.events.EventKey;


import minecraft.rolest.modules.impl.player.AntiAim;
import minecraft.rolest.modules.impl.player.FastEXP;
import minecraft.rolest.modules.impl.player.Nuker;

import minecraft.rolest.utils.render.font.Font;
import minecraft.rolest.utils.text.font.ClientFonts;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class ModuleManager {
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    private PlayerHelper playerHelper;
    private NoServerDesync noServerDesync;
    private HitAura hitAura;
   // private ElytraFly elytraFly;
    private ElytraSpeed elytraSpeed;
    private ViewModel viewModel;
    private AutoGapple autoGapple;
    private AirJump airJump;
    private AutoSprint autoSprint;
    private Velocity velocity;
    private NoRender noRender;
    private ChunkAnimation chunkAnimation;
    private KriperFarm kriperFarm;
    private InventoryPlus inventoryPlus;
    private ElytraHelper elytrahelper;
    private SDuelConnect sDuelConnect;
    private PotionThrower autopotion;
    private TriggerBot triggerbot;
    private ClickFriend clickfriend;
    private AirStuck airStuck;
    private FTHelper FTHelper;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private EntityBox entityBox;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoLeave autoLeave;
    private ClientTune clientTune;
    private AutoTotem autoTotem;
    private Crosshair crosshair;
    private DeathEffect deathEffect;
    private Strafe strafe;
    private ChinaHat chinaHat;
    private Snow snow;
    private Particles particles;
    private JumpCircle jumpCircle;
    private ItemPhysic itemPhysic;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private StorageESP storageESP;
    private Spider spider;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private Tracers tracers;
    private SelfDestruct selfDestruct;
    private BetterMinecraft betterMinecraft;
    private SeeInvisibles seeInvisibles;
    private Speed speed;
    private Money money;
    private WaterSpeed waterSpeed;
    private NoFriendHurt noFriendHurt;
    private ClickGui clickGui;
    private WorldTweaks worldTweaks;
    private Arrows arrows;
    private ChatHelper chatHelper;
    private AutoActions autoActions;
    private FullBright fullBright;
    private ElytraBooster elytraBooster;
    private AncientXray ancientXray;
    private SwingAnimation swingAnimation;
    private HUD hud;
    private AhHelper ahHelper;
    private Rxray rxray;
    private CustomF5 customF5;
    private FastEXP fastEXP;
    private NameTags nameTags;
    private Theme theme;
    private AutoFarmFT autoFarmFT;
    private AntiBot antiBot;
    private CustomModels customModels;
    private Nuker nuker;
    private AspectRatio aspectRatio;
    private AutoBuy autoBuy;
    private ShaderESP shaderESP;
    private LegitAura legitAura;
    private AntiAim frizypaste;
    private trailsbeta trailsbeta;
    private invwalk invwalk;
    private StrafeSP strafeSP;
    private NoSlow noSlow;
    private ShulkerChecker shulkerChecker;
    private Distance distance;
    private TridentAuto tridentAuto;
    private TapeMouse tapeMouse;
    private ItemsOverlay itemsOverlay;
    private BaseFinder baseFinder;
    private AutoDuel autoDuel;
    private HotBar hotBar;
    private Xray xray;
    private PotionHelper potionHelper;
    private FastFly fastFly;
    private NoJumpDelay noJumpDelay;
    private KTLeave ktLeave;
    private AntiDeath antiDeath;
    private Fly fly;
    private Scaffold scaffold;
    // private FastAttack fastAttack;
    public void init() {
        registerAll(
              // fastAttack = new FastAttack(),
             //   elytraFly = new ElytraFly(),
                airJump = new AirJump(),
                elytraSpeed = new ElytraSpeed(),
                scaffold = new Scaffold(),
                airStuck = new AirStuck(),
                invwalk = new invwalk(),
                sDuelConnect = new SDuelConnect(),
                fastFly = new FastFly(),
                noJumpDelay = new NoJumpDelay(),
                fly = new Fly(),
                antiDeath = new AntiDeath(),
                hotBar = new HotBar(),
                ktLeave = new KTLeave(),
                itemsOverlay = new ItemsOverlay(),
                xray = new Xray(),
                autoDuel = new AutoDuel(),
                antiBot = new AntiBot(),
                baseFinder = new BaseFinder(),
                potionHelper = new PotionHelper(),
                tapeMouse = new TapeMouse(),
                trailsbeta = new trailsbeta(),
                money = new Money(),
                tridentAuto = new TridentAuto(),
                distance = new Distance(),
                noSlow = new NoSlow(),
                storageESP = new StorageESP(),
                strafeSP = new StrafeSP(),
                shulkerChecker = new ShulkerChecker(),
                frizypaste = new AntiAim(),
                customModels = new CustomModels(),
                viewModel = new ViewModel(),
                particles = new Particles(),
                 shaderESP = new ShaderESP(),
                kriperFarm = new KriperFarm(),
                legitAura = new LegitAura(),
                theme = new Theme(),
                aspectRatio = new AspectRatio(),
                nuker = new Nuker(),
                autoFarmFT = new AutoFarmFT(),
                nameTags = new NameTags(),
                fastEXP = new FastEXP(),
                customF5 = new CustomF5(),
                rxray = new Rxray(),
                ahHelper = new AhHelper(),
                hud = new HUD(),
                swingAnimation = new SwingAnimation(),
                elytraBooster = new ElytraBooster(),
                fullBright = new FullBright(),
                autoActions = new AutoActions(),
                chatHelper = new ChatHelper(),
                arrows = new Arrows(),
                noServerDesync = new NoServerDesync(),
                playerHelper = new PlayerHelper(),
                worldTweaks = new WorldTweaks(),
                deathEffect = new DeathEffect(),
                clickGui = new ClickGui(),
                noFriendHurt = new NoFriendHurt(),
                waterSpeed = new WaterSpeed(),
                speed = new Speed(),
               autoGapple = new AutoGapple(),
                autoSprint = new AutoSprint(),
                velocity = new Velocity(),
                noRender = new NoRender(),
                inventoryPlus = new InventoryPlus(),
                seeInvisibles = new SeeInvisibles(),
                elytrahelper = new ElytraHelper(),
               autopotion = new PotionThrower(),
                triggerbot = new TriggerBot(),
                clickfriend = new ClickFriend(),
                FTHelper = new FTHelper(),
                entityBox = new EntityBox(),
                antiPush = new AntiPush(),
                freeCam = new FreeCam(),
                chestStealer = new ChestStealer(),
                autoLeave = new AutoLeave(),
                clientTune = new ClientTune(),
                crosshair = new Crosshair(),
                autoTotem = new AutoTotem(),
                itemCooldown = new ItemCooldown(),
                hitAura = new HitAura(autopotion),
                clickPearl = new ClickPearl(itemCooldown),
                autoSwap = new AutoSwap(),
                strafe = new Strafe(hitAura),
                chinaHat = new ChinaHat(),
                snow = new Snow(),
                jumpCircle = new JumpCircle(),
                itemPhysic = new ItemPhysic(),
                predictions = new Predictions(),
                noEntityTrace = new NoEntityTrace(),
                spider = new Spider(),
                nameProtect = new NameProtect(),
                noInteract = new NoInteract(),
                chunkAnimation = new ChunkAnimation(),
                tracers = new Tracers(),
                selfDestruct = new SelfDestruct(),
                betterMinecraft = new BetterMinecraft(),
                ancientXray = new AncientXray());

        sortModulesByWidth();

        Rol.getInstance().getEventBus().register(this);
    }

    private void registerAll(Module... modules) {
//        Arrays.sort(modules, Comparator.comparing(Module::getName));
        this.modules.addAll(List.of(modules));
    }

    private void sortModulesByWidth() {
        try {
            modules.sort(Comparator.comparingDouble(module -> {
                return ClientFonts.msSemiBold[17].getWidth(module.getClass().getName());
            }).reversed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Module> getSorted(Font font, float size) {
        return modules.stream().sorted((f1, f2) -> Float.compare(font.getWidth(f2.getName(), size), font.getWidth(f1.getName(), size))).toList();
    }

    public List<Module> get(final Category category) {
        return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
    }
    
    public int countEnabledModules() {
        int enabledModules = 0;
        for (Module module : modules) {
            if (module.isState()) {
                enabledModules++;
            }
        }
        return enabledModules;
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        for (Module Module : modules) {
            if (Module.getBind() == e.getKey()) {
                Module.toggle();
            }
        }
    }
}
