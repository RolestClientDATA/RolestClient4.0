package minecraft.rolest.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.rolest.utils.client.IMinecraft;
import minecraft.rolest.utils.render.gl.Stencil;
import minecraft.rolest.utils.shaderbydobser.old.ShaderUtils;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;


public class GaussianBlur  {

    private static final ShaderUtils gaussianBlur = new ShaderUtils("blur");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniform("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) IMinecraft.mc.getMainWindow().getWidth(), 1.0F / (float) IMinecraft.mc.getMainWindow().getHeight());
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur(){
        Stencil.initStencilToWrite();
    }
    public static void endBlur(float radius, float compression) {
        Stencil.readStencilBuffer(1);

        framebuffer = ShaderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(IMinecraft.mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        IMinecraft.mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        gaussianBlur.setUniformf("direction", 0, compression);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.detach();

        Stencil.uninitStencilBuffer();
        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);
    }

    public static void blur(float radius, float compression) {
        framebuffer = ShaderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(IMinecraft.mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        IMinecraft.mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(0, compression, radius);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.detach();

        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
}