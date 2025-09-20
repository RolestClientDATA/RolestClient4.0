package minecraft.rolest.utils.text.font;

import lombok.SneakyThrows;
import minecraft.rolest.utils.text.font.common.Lang;
import minecraft.rolest.utils.text.font.styled.StyledFont;


public class ClientFonts {
    public static final String FONT_DIR = "/assets/minecraft/rolka/fonts/normal/";

    public static volatile StyledFont[] msBold = new StyledFont[50];
    public static volatile StyledFont[] msMedium = new StyledFont[50];
    public static volatile StyledFont[] msLight = new StyledFont[50];
    public static volatile StyledFont[] msRegular = new StyledFont[50];
    public static volatile StyledFont[] msSemiBold = new StyledFont[50];
    public static volatile StyledFont[] rolka = new StyledFont[50];
    public static volatile StyledFont[] roadRage = new StyledFont[50];
    public static volatile StyledFont[] small_pixel = new StyledFont[50];
    public static volatile StyledFont[] tech = new StyledFont[50];
    public static volatile StyledFont[] icon = new StyledFont[50];
    public static volatile StyledFont[] icons = new StyledFont[50];
    public static volatile StyledFont[] icons_wex = new StyledFont[50];
    public static volatile StyledFont[] icons_nur = new StyledFont[50];
    public static volatile StyledFont[] comfortaa = new StyledFont[50];
    public static volatile StyledFont[] interBold = new StyledFont[80];
    public static volatile StyledFont[] interMedium = new StyledFont[80];
    public static volatile StyledFont[] tenacity = new StyledFont[80];
    public static volatile StyledFont[] interRegular = new StyledFont[80];
    public static volatile StyledFont[] interSemiBold = new StyledFont[80];
    public static volatile StyledFont[] logo = new StyledFont[80];

    @SneakyThrows
    public static void init() {

        for (int i = 8; i < 50;i++) {
            msBold[i] = new StyledFont("Montserrat-Bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            logo[i] = new StyledFont("logo.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msLight[i] = new StyledFont("Montserrat-Light.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msMedium[i] = new StyledFont("Montserrat-Medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msRegular[i] = new StyledFont("Montserrat-Regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            msSemiBold[i] = new StyledFont("Montserrat-SemiBold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            rolka[i] = new StyledFont("rolka.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            roadRage[i] = new StyledFont("roadrage.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            small_pixel[i] = new StyledFont("small_pixel.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            tech[i] = new StyledFont("tech.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icon[i] = new StyledFont("icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icons[i] = new StyledFont("penus.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icons_wex[i] = new StyledFont("iconswex.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icons_nur[i] = new StyledFont("iconsnur.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            comfortaa[i] = new StyledFont("comfortaa-regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interRegular[i] = new StyledFont("inter_regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interMedium[i] = new StyledFont("inter_medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interSemiBold[i] = new StyledFont("inter_semibold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            tenacity[i] = new StyledFont("tenacity.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 80;i++) {
            interBold[i] = new StyledFont("inter_bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
    }
}