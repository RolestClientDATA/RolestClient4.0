package minecraft.rolest.ui.notify;

import minecraft.rolest.Rol;
import minecraft.rolest.config.FriendStorage;
import minecraft.rolest.modules.api.ModuleManager;
import minecraft.rolest.modules.impl.render.HUD;
import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.animations.Animation;
import minecraft.rolest.utils.animations.Direction;
import minecraft.rolest.utils.animations.impl.EaseBackIn;
import minecraft.rolest.utils.animations.impl.EaseInOutQuad;
import minecraft.rolest.utils.math.MathUtil;
import minecraft.rolest.utils.render.GaussianBlur;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.text.font.ClientFonts;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.concurrent.CopyOnWriteArrayList;

import static minecraft.rolest.utils.client.IMinecraft.mc;


public class NotificationManager {
    public static final FriendStorage NOTIFICATION_MANAGER = null;
    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList();
    private MathUtil AnimationMath;
    boolean state;

    public void add(String text, String content, int time) {
        this.notifications.add(new Notification(text, content, time));
    }

    public void draw(MatrixStack stack) {
        int yOffset = 0;
        for (Notification notification : this.notifications) {
            if (System.currentTimeMillis() - notification.getTime() <= (long)notification.time2 * 1000L - 300L) {
                notification.yAnimation.setDirection(Direction.FORWARDS);
            }
            notification.alpha = (float)notification.animation.getOutput();
            if (System.currentTimeMillis() - notification.getTime() > (long)notification.time2 * 1000L) {
                notification.yAnimation.setDirection(Direction.BACKWARDS);
            }
            if (notification.yAnimation.finished(Direction.BACKWARDS)) {
                this.notifications.remove(notification);
                continue;
            }
            float x = (float) mc.getMainWindow().scaledWidth() - (Fonts.sfMedium.getWidth(notification.getText(), 7.0f) + 8.0f) - 10.0f;
            float y = mc.getMainWindow().scaledHeight() - 40;
            notification.yAnimation.setEndPoint(yOffset);
            notification.yAnimation.setDuration(500);
            notification.setX(x);
            notification.setY(MathUtil.fast(notification.getY(), y -= (float)((double)notification.draw(stack) * notification.yAnimation.getOutput() + 3.0), 15.0f));
            ++yOffset;
        }
    }

    private class Notification {
        private float x = 0.0f;
        private float y = mc.getMainWindow().scaledHeight() + 28;
        private String text;
        private String content;
        private long time = System.currentTimeMillis();
        public Animation animation = new EaseInOutQuad(500, 1.0, Direction.FORWARDS);
        public Animation yAnimation = new EaseBackIn(500, 1.0, 1.0f);
        float alpha;
        int time2 = 3;
        private boolean isState;
        private boolean state;

        public Notification(String text, String content, int time) {
            this.text = text;
            this.content = content;
            this.time2 = time;
        }

        public float draw(MatrixStack stack) {
            mc.gameRenderer.setupOverlayRendering(2);

            float posX = mc.getMainWindow().getScaledWidth() / 2.0F;

            float posY = (float) mc.getMainWindow().getScaledHeight() + (float) mc.getMainWindow().getScaledHeight() / 2 - y - 30;

            float textWidth = Fonts.sfuy.getWidth(this.text + 28.0F + 15F,7);

            float XStart = posX - textWidth / 2.0F;

            //  RenderUtility.drawRoundedRect(XStart, posY + 15.0F, textWidth, 11.0F, 4.0F, ColorUtils.rgba(0, 0, 0, 200));
            ModuleManager moduleManager = Rol.getInstance().getModuleManager();
            HUD blurblat = moduleManager.getHud();

            if (blurblat.blur.get()) {
                GaussianBlur.startBlur();
                RenderUtility.drawRoundedRect(XStart, posY + 15.0F, textWidth, 13.0F, 3.0F, ColorUtils.rgba(5, 5, 5, 200));
                GaussianBlur.endBlur(20, 7);

            }
            RenderUtility.drawRoundedRect(XStart, posY + 15.0F, textWidth, 13.0F, 3.0F, ColorUtils.rgba(5, 5, 5, 200));
            RenderUtility.drawRoundedRect(XStart, posY + 15.0F, textWidth, 13.0F, 3.0F, ColorUtils.setAlpha(Theme.RectColor(0),50));



            //  RenderUtility.drawRoundedRect(XStart, posY + 15.0F, textWidth, 13.0F, 3.0F, ColorUtils.rgba(5, 5, 5, 175));








            float textX = posX - (Fonts.sfuy.getWidth(this.text,7));

            float iconwidth = Fonts.icons2.getWidth("J",9) + 9F;

            Fonts.sfuy.drawText(stack, this.text, XStart + iconwidth, posY + 18.3F, -1,7) ;

            //FUNCTIONS
            if (this.text.contains("включен")) {
                Fonts.icons2.drawText(stack, "J", (XStart + 4), (posY + 18F), -1 ,9);
            } else if (this.text.contains("выключен")) {
                Fonts.icons2.drawText(stack, "K", (XStart + 4), (posY + 18F), -1 ,9);
            }


            //FREELOOK
            if (this.text.contains("Отключите KillAura")) {
                ClientFonts.icons_nur[16].drawString(stack,"M",(double)(XStart + 4), (double) (posY + 20.0F), -1);
            }




            //FTHELPER
            if (this.text.contains("До следующего ивента")) {
                Fonts.sfuy.drawText(stack,"!!!",XStart + 6,  (posY +  18F), -1,8);
            }
            if (this.text.contains("не найден")) {
                Fonts.sfuy.drawText(stack,"!!!",XStart + 6,  (posY + 18F), -1,8);
            }
            if (this.text.contains("Заюзал")) {
                Fonts.sfuy.drawText(stack,"!!!",XStart + 6,  (posY + 18 ), -1,8);
            }

            mc.gameRenderer.setupOverlayRendering();

            return 14.0F;

        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public String getText() {
            return this.text;
        }

        public String getContent() {
            return this.content;
        }

        public long getTime() {
            return this.time;
        }
    }
}