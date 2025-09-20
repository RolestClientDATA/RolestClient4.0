package minecraft.rolest.utils;

import lombok.SneakyThrows;
import minecraft.rolest.utils.text.font.common.Lang;
import minecraft.rolest.utils.text.font.styled.StyledFont;


public class IconUtility {
    public static final String FONT_DIR = "/assets/minecraft/rolka/font/";

    public static volatile StyledFont[] sf = new StyledFont[50];
    public static volatile StyledFont[] icon = new StyledFont[50];

    @SneakyThrows
    public static void init() {

        for (int i = 8; i < 50;i++) {
            sf[i] = new StyledFont("sf.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 50;i++) {
            icon[i] = new StyledFont("icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
    }
}