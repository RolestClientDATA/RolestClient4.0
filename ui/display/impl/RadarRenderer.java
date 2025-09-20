package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Vector4f;
import minecraft.rolest.Rol;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.modules.api.ModuleManager;
import minecraft.rolest.modules.impl.render.HUD;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.ui.display.ElementRenderer;
import minecraft.rolest.utils.animations.Direction;
import minecraft.rolest.utils.animations.impl.EaseInOutQuad;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.PlayerEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;

import java.util.*;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RadarRenderer implements ElementRenderer {
    final Dragging drag;
    final EaseInOutQuad fadeAnimation = new EaseInOutQuad(300, 1.0, Direction.FORWARDS);
    final float width = 50;
    final float height = 67;
    final float radarSize = 50;
    final int maxDistance = 50;
    final float spacing = 5;
    final float playerDotSize = 4;
    final float crossLineWidth = 1.5f;

    @Override
    public void render(EventDisplay event) {
        MatrixStack ms = event.getMatrixStack();
        List<PlayerEntity> players = getNearbyPlayers();

        boolean shouldShow = !players.isEmpty() || mc.currentScreen instanceof ChatScreen;

        fadeAnimation.setDirection(shouldShow ? Direction.FORWARDS : Direction.BACKWARDS);
        fadeAnimation.setDuration(shouldShow ? 300 : 200);
        float fadeValue = (float) fadeAnimation.getOutput();

        if (fadeValue <= 0.01f) {
            return;
        }

        float posX = drag.getX();
        float posY = drag.getY();

        GlStateManager.pushMatrix();

        drawStyledRect(
                posX,
                posY,
                width,
                height,
                6
        );


        ClientFonts.icon[15].drawString(
                ms,
                "Q",
                posX + 5.5f,
                posY + 8f,
                ColorUtils.setAlpha(Theme.MainColor(0), (int)(255 * fadeValue))
        );
        Fonts.sfMedium.drawText(
                ms,
                "Radar",
                posX + 18,
                posY + 5f,
                ColorUtils.setAlpha(-1, (int)(255 * fadeValue)),
                8
        );

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);

        float radarX = posX + width / 2;
        float radarY = posY + height / 2 + 9;
        float radarRadius = radarSize / 2;

        RenderUtility.drawRoundedRect(
                radarX - radarRadius,
                radarY - crossLineWidth/2,
                radarSize,
                crossLineWidth,
                crossLineWidth/2,
                ColorUtils.setAlpha(ColorUtils.rgba(155, 155, 155, 50),
                        (int)(50 * fadeValue))
        );

        RenderUtility.drawRoundedRect(
                radarX - crossLineWidth/2,
                radarY - radarRadius,
                crossLineWidth,
                radarSize,
                crossLineWidth/2,
                ColorUtils.setAlpha(ColorUtils.rgba(155, 155, 155, 50),
                        (int)(50 * fadeValue))
        );

        RenderUtility.drawCircle(
                radarX,
                radarY,
                playerDotSize,
                ColorUtils.setAlpha(Theme.MainColor(0), (int)(255 * fadeValue))
        );

        for (PlayerEntity player : players) {
            renderPlayerOnRadar(ms, player, radarX, radarY, radarRadius, fadeValue);
        }

        Scissor.unset();
        Scissor.pop();
        GlStateManager.popMatrix();

        drag.setWidth(width);
        drag.setHeight(height);
    }

    private void renderPlayerOnRadar(MatrixStack ms, PlayerEntity player, float radarX, float radarY, float radarRadius, float fadeValue) {
        double playerX = mc.player.getPosX();
        double playerZ = mc.player.getPosZ();

        double dx = player.getPosX() - playerX;
        double dz = player.getPosZ() - playerZ;

        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if(distance > maxDistance) return;

        float scale = distance / maxDistance;

        float angle = (float) Math.atan2(dz, dx)
                - (float) Math.toRadians(mc.player.rotationYaw)
                - (float) Math.PI;

        float x = radarX + (float)(Math.cos(angle) * radarRadius * scale);
        float y = radarY + (float)(Math.sin(angle) * radarRadius * scale);

        RenderUtility.drawCircle(
                x,
                y,
                playerDotSize,
                ColorUtils.setAlpha(Theme.MainColor(0), (int)(255 * fadeValue))
        );
    }
    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {
        ModuleManager moduleManager = Rol.getInstance().getModuleManager();
        HUD blurblat = moduleManager.getHud();
        RenderUtility.drawRoundedRect(x - 1, y - 1, width + 1.5f, height + 2f  ,5,ColorUtils.rgba(50, 50, 50,255));
        RenderUtility.drawRoundedRect(x, y, width - 0.5f, height  ,4,ColorUtils.rgba(20, 20, 20,255));
        RenderUtility.drawRoundedRect(x, y + 16, width - 0.5f, height - 17 ,new Vector4f(2,6,2,6),ColorUtils.rgba(0,0,0,45));


    }
    private List<PlayerEntity> getNearbyPlayers() {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.getDistance(mc.player) <= maxDistance)
                .sorted(Comparator.comparingDouble(p -> p.getDistance(mc.player)))
                .collect(Collectors.toList());
    }
}