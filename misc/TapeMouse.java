package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import minecraft.rolest.events.WorldEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.utils.client.IMinecraft;

@ModuleRegister(
        name = "Box 3D",
        category = Category.Render
)
public class TapeMouse extends Module {

    private static final double WIDTH = 0.6;
    private static final double HEIGHT = 1.8;

    // 50% прозрачная белая заливка
    private static final float[] FILL_COLOR = {1.0f, 1.0f, 1.0f, 0.5f};

    // Полностью непрозрачный белый контур
    private static final float[] LINE_COLOR = {1.0f, 1.0f, 1.0f, 1.0f};

    @Subscribe
    public void onDisplay(WorldEvent display) {

        Minecraft mc = IMinecraft.mc;
        if (mc.world == null || mc.player == null || mc.getRenderManager() == null) return;

        try {
            if (mc.gameSettings.fov == 0) return;

            renderHitboxes(mc, display.getPartialTicks());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void renderHitboxes(Minecraft mc, float partialTicks) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull(); // Важно для отображения со всех сторон

        Vector3d view = mc.getRenderManager().info.getProjectedView();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            renderPlayerHitbox(player, view, partialTicks);
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void renderPlayerHitbox(PlayerEntity player, Vector3d view, float partialTicks) {
        double x = lerp(player.lastTickPosX, player.getPosX(), partialTicks) - view.x;
        double y = lerp(player.lastTickPosY, player.getPosY(), partialTicks) - view.y;
        double z = lerp(player.lastTickPosZ, player.getPosZ(), partialTicks) - view.z;

        AxisAlignedBB box = new AxisAlignedBB(
                x - WIDTH/2, y, z - WIDTH/2,
                x + WIDTH/2, y + HEIGHT, z + WIDTH/2
        );

        // 1. Рисуем заливку со всех сторон
        renderBoxFill(box);

        // 2. Рисуем контур со всех сторон
        renderBoxOutline(box);
    }

    private void renderBoxFill(AxisAlignedBB box) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Все 6 граней куба (обе стороны)

        // Нижняя грань
        addQuad(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);

        // Верхняя грань
        addQuad(buffer, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ);

        // Северная грань (Z-)
        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ);

        // Южная грань (Z+)
        addQuad(buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);

        // Западная грань (X-)
        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        // Восточная грань (X+)
        addQuad(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.maxZ);

        Tessellator.getInstance().draw();
    }

    private void renderBoxOutline(AxisAlignedBB box) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        // 12 линий куба (все ребра)

        // Нижние линии
        addLine(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);

        // Верхние линии
        addLine(buffer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        // Вертикальные линии
        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);

        Tessellator.getInstance().draw();
    }

    private void addQuad(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4) {
        buffer.pos(x1, y1, z1).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x2, y2, z2).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x3, y3, z3).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x4, y4, z4).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
    }

    private void addLine(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2) {
        buffer.pos(x1, y1, z1).color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3]).endVertex();
        buffer.pos(x2, y2, z2).color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3]).endVertex();
    }

    private double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }



    public void register(LiteralArgumentBuilder<CommandSource> then) {
    }

    public class RenderLevelStageEvent {
    }
}
