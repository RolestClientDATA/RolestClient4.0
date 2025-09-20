package minecraft.rolest.ui.display.impl;
// ColorUtils.gradient(Theme.WaterColor, Theme.mainRectColor,2,9)
import com.mojang.blaze3d.platform.GlStateManager;
import  minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.ui.display.ElementRenderer;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.animations.Animation;
import minecraft.rolest.utils.animations.Direction;
import minecraft.rolest.utils.animations.impl.EaseBackIn;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.render.*;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.gl.Stencil;
import minecraft.rolest.utils.render.rect.RenderUtility;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetInfoRenderer implements ElementRenderer {
    final StopWatch stopWatch = new StopWatch();
     final Dragging drag;
    LivingEntity entity = null;
    boolean allow;
    final Animation animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;

    @Override
    public void render(EventDisplay eventDisplay) {
        entity = getTarget(entity);

        float rounding = 6;
        boolean out = !allow || stopWatch.isReached(1000);
        animation.setDuration(out ? 400 : 300);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            float posX = drag.getX();
            float posY = drag.getY();

            float headSize = 28;
            float spacing = 5;

            float width = 210 / 1.75f;
            float height = 85 / 1.75f;
            drag.setWidth(width);
            drag.setHeight(height);

            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();

            healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);

            float animationValue = (float) animation.getOutput();

            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = posX + (width * halfAnimationValueRest);
            float testY = posY + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;

            GlStateManager.pushMatrix();

            sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());
            float finalPosY = posY;
            drawStyledRect(posX, finalPosY, width, height - 4, 9);
//RenderUtility Theme.text(0)
            Stencil.initStencilToWrite();
            RenderUtility.drawRoundedRect(posX + spacing - 1.25f, posY + spacing - 1.5f, 75 / 2f, 75 / 2f, 8,
                    ColorUtils.rgba(25, 26, 40, 165));
            Stencil.readStencilBuffer(1);
            drawTargetHead(entity, posX + spacing - 1.25f, posY + spacing - 1.5f, 75 / 2f, 75 / 2f);
            Stencil.uninitStencilBuffer();
            Scissor.push();

            Scissor.setFromComponentCoordinates(testX, testY, testW - 6, testH);

            Fonts.sfbold.drawText(eventDisplay.getMatrixStack(), entity.getName().getString(),
                    posX + headSize + spacing + spacing + 8.75f, posY + spacing + 4.1f, -1, 8);
            Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(),
                    "HP: " + ((int) hp),
                    posX + headSize + spacing + spacing + 8.75f,
                    posY + spacing + 17, ColorUtils.rgb(203, 203, 203), 6);

            Scissor.unset();
            Scissor.pop();



            float hpBarWidth = width - 52;
            float hpBarHeight = 7;
            float hpBarX = posX + headSize + spacing + spacing + 8.75f - 0.9f;
            float hpBarY = posY + height - spacing * 1.5f - 10;

            // Фон полоски HP
            RenderUtility.drawRoundedRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight, 2,
                    ColorUtils.rgba(203, 203, 203, 45));

            // Обычные HP
            RenderUtility.drawRoundedRect(hpBarX, hpBarY,
                    hpBarWidth * healthAnimation, hpBarHeight, 2,
                    Theme.Text(0));

            GlStateManager.popMatrix();
        }
    }

    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Rol.getInstance().getModuleManager().getHitAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            drawFace(rendererManager.getEntityTexture(entity), x, y, 8F, 8F, 8F, 8F, width, height, 64F, 64F, entity);
        }
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawFace(ResourceLocation res, float d,
                         float y,
                         float u,
                         float v,
                         float uWidth,
                         float vHeight,
                         float width,
                         float height,
                         float tileWidth,
                         float tileHeight,
                         LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
        GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {
        RenderUtility.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 90));
        RenderUtility.drawShadow(x + 5, y + 5, width, height, 5, ColorUtils.rgba(10, 15, 13, 15));
    }
}