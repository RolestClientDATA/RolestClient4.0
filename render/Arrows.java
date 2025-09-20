package minecraft.rolest.modules.impl.render;

import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeListSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import com.google.common.eventbus.Subscribe;
import minecraft.rolest.Rol;
import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.events.EventChangeWorld;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.utils.math.animation.Animation;
import minecraft.rolest.utils.math.animation.util.Easings;
import minecraft.rolest.utils.player.MoveUtils;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

@ModuleRegister(name = "Arrows", category = Category.Render,desc ="Стрелки к игрокам")
public class Arrows extends Module {

    public ModeListSetting targets = new ModeListSetting("Отображать",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Предметы", false),
            new BooleanSetting("Мобы", false)
    );
    final SliderSetting razmer = new SliderSetting("Размер", 16f, 5f, 30f, 1f);
    final SliderSetting distantse = new SliderSetting("Радиус", 60f, 5f, 145f, 1f);


    public Arrows() {
        addSettings(targets,razmer,distantse);
    }


    @Setter
    @Getter
    private boolean render = false;
    private final Animation yawAnimation = new Animation();
    private final Animation moveAnimation = new Animation();
    private final Animation openAnimation = new Animation();
    private float addX;
    private float addY;
    private float lastYaw;
    private float lastPitch;

    private ResourceLocation arrow = new ResourceLocation("rolka/images/triangle2.png");

    @Override
    public void onDisable() {
        super.onDisable();
        setRender(false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        setRender(true);
    }

    @Subscribe
    public void onWorldChange(EventChangeWorld e) {
        setRender(isRender());
    }

    @Subscribe
    public void onDisplay(EventDisplay e) {
        openAnimation.update();
        moveAnimation.update();
        yawAnimation.update();

        if (!render && openAnimation.getValue() == 0 && openAnimation.isFinished()) return;

        final float moveAnim = calculateMoveAnimation();

        openAnimation.run(render ? 1 : 0, 0.3, Easings.BACK_OUT, true);
        moveAnimation.run(render ? moveAnim : 0, 0.5, Easings.BACK_OUT, true);
        yawAnimation.run(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.3, Easings.BACK_OUT, true);

        final double cos = Math.cos(Math.toRadians(yawAnimation.getValue()));
        final double sin = Math.sin(Math.toRadians(yawAnimation.getValue()));
        double radius = moveAnimation.getValue();
        final double xOffset = (scaled().x / 2F) - radius;
        final double yOffset = (scaled().y / 2F) - radius;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof PlayerEntity p) {
                if (!entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(p.getGameProfile().getName()))) continue;
                if (!(entity instanceof PlayerEntity && targets.getValueByName("Игроки").get()
                        || entity instanceof ItemEntity && targets.getValueByName("Предметы").get()
                        || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.getValueByName("Мобы").get()
                )) continue;
                if (entity == mc.player) continue;

                Vector3d vector3d = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
                final double xWay = (((entity.getPosX() + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks()) - vector3d.x) * 0.01D);
                final double zWay = (((entity.getPosZ() + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks()) - vector3d.z) * 0.01D);
                final double rotationY = -(zWay * cos - xWay * sin);
                final double rotationX = -(xWay * cos + zWay * sin);
                final double angle = Math.toDegrees(Math.atan2(rotationY, rotationX));
                double x = ((radius * Math.cos(Math.toRadians(angle))) + xOffset + radius);
                double y = ((radius * Math.sin(Math.toRadians(angle))) + yOffset + radius);
                Crosshair crosshair = Rol.getInstance().getModuleManager().getCrosshair();
                if (crosshair.isState() && crosshair.mode.is("Орбиз") && !crosshair.staticCrosshair.get() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    addX = crosshair.getAnimatedYaw();
                    addY = crosshair.getAnimatedPitch();
                } else {
                    addX = addY = 0;
                }

                x += addX;
                y += addY;

                if (isValidRotation(rotationX, rotationY, radius)) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, 0D);
                    GL11.glRotated(angle, 0D, 0D, 1D);
                    GL11.glRotatef(90F, 0F, 0F, 1F);


                    int color = FriendStorage.isFriend(TextFormatting.getTextWithoutFormattingCodes(entity.getName().getString())) ? ColorUtils.getColor(25, 227, 142) : (Rol.getInstance().getModuleManager().getHitAura().getTarget() == entity ? ColorUtils.getColor(242, 63, 67) : Theme.MainColor(0));

                    RenderUtility.drawImageAlpha(new ResourceLocation("rolka/images/triangle2.png"), -8.0F, -9.0F, razmer.get(), razmer.get(), color);
                    GL11.glPopMatrix();
                }
            }
            lastYaw = mc.player.rotationYaw;
            lastPitch = mc.player.rotationPitch;
        }
    }

    private float calculateMoveAnimation() {
        float set = distantse.get();
        if (mc.currentScreen instanceof ContainerScreen<?> container) {
            set = Math.max(container.ySize, container.xSize) / 2F + 75;
        }
        float moveAnim = set;
        if (MoveUtils.isMoving()) {
            moveAnim += mc.player.isSneaking() ? 5 : 10;
        } else if (mc.player.isSneaking()) {
            moveAnim -= 10;
        }
        return moveAnim;
    }

    private boolean isValidRotation(double rotationX, double rotationY, double radius) {
        final double mrotY = -rotationY;
        final double mrotX = -rotationX;
        return MathHelper.sqrt(mrotX * mrotX + mrotY * mrotY) < radius;
    }

}