package minecraft.rolest.utils.text;

import minecraft.rolest.modules.impl.render.Theme;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class GradientUtil {

    public static StringTextComponent gradient(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.Text(i)))));
        }
        return text;
    }

    public static StringTextComponent gradient1(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.MainColor(i)))));
        }
        return text;
    }



    public static StringTextComponent gradienmainmenu(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.getMainMenu(i,i)))));
        }
        return text;
    }
    public static StringTextComponent mainmenu(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.MainColor(80)))));
        }
        return text;
    }

    public static IFormattableTextComponent white(String message) {
        return (new StringTextComponent(message)).setStyle(Style.EMPTY.setColor(Color.fromHex("#FFFFFF")));
    }

    public static IFormattableTextComponent blue(String message) {
        return (new StringTextComponent(message)).setStyle(Style.EMPTY.setColor(Color.fromHex("#6495ED")));
    }

    public static IFormattableTextComponent red(String message) {
        return (new StringTextComponent(message)).setStyle(Style.EMPTY.setColor(Color.fromHex("#FF0000")));
    }

    public static IFormattableTextComponent green(String message) {
        return (new StringTextComponent(message)).setStyle(Style.EMPTY.setColor(Color.fromHex("#00FF00")));
    }




    public static IFormattableTextComponent black(String message) {
        return (new StringTextComponent(message)).setStyle(Style.EMPTY.setColor(Color.fromHex("#000000")));
    }
}

