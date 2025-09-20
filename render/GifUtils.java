package minecraft.rolest.utils.render;

public class GifUtils {
    public int getFrame(int totalFrames, int frameDelay, boolean countFromZero) {
        long currentTime = System.currentTimeMillis();
        int i;
        i = (int) ((currentTime / frameDelay) % totalFrames) + (countFromZero ? 0 : 1);
        return i;
    }
}
