package minecraft.rolest.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.rolest.Rol;
import minecraft.rolest.config.StaffStorage;
import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.ui.display.ElementRenderer;
import minecraft.rolest.ui.display.ElementUpdater;

import minecraft.rolest.modules.impl.render.Theme;
import minecraft.rolest.utils.drag.Dragging;
import minecraft.rolest.utils.render.color.ColorUtils;
import minecraft.rolest.utils.render.rect.RenderUtility;
import minecraft.rolest.utils.render.gl.Scissor;
import minecraft.rolest.utils.render.font.Fonts;
import minecraft.rolest.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffListRenderer implements ElementRenderer, ElementUpdater {

    final Dragging dragging;
    float iconSizeX = 10;

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|С…РµР»Рї|Р°РґРј|РїРѕРґРґРµСЂР¶РєР°|РєСѓСЂР°|own|taf|curat|dev|supp|yt|СЃРѕС‚СЂСѓРґ).*");

    float width;
    float height;

    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6.6f;

        ITextComponent name = GradientUtil.gradient("StaffList");
        String namemod = "StaffList";



        RenderUtility.drawShadow(posX, posY, width, height, 7, ColorUtils.rgba(9, 8, 23, 1));
        RenderUtility.drawRoundedRect(posX - 1.3f, posY - 1.3f, width + 2.8f, height + 2.8f, 5,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));
        RenderUtility.drawRoundedRect(posX - 0.5f, posY - 0.5f, width + 1f, height + 1f, 4,  ColorUtils.setAlpha(ColorUtils.rgba(10, 15, 13, 90), 450));

        Scissor.push();
        try {
            Scissor.setFromComponentCoordinates(posX, posY, width, height);
            Fonts.sfui.drawText(ms, namemod, posX + padding, posY + padding, ColorUtils.rgb(255, 255, 255), fontSize);

            float imagePosX = posX + width - iconSizeX - padding;
            Fonts.icons2.drawText(ms, "L", imagePosX + 2f, posY + 7f,Theme.Text(0), fontSize);

            posY += fontSize + padding * 2;

            float maxWidth = Fonts.sfui.getWidth(name, fontSize) + padding * 2;
            float localHeight = fontSize + padding * 2;

            RenderUtility.drawRectHorizontalW(posX + 0.5f, posY, width - 1, 1.5f, 3, ColorUtils.rgba(46, 45, 58, (int) (255 * 1f)));
            posY += 4f;

            for (StaffListRenderer.Staff f : staffPlayers) {
                ITextComponent prefix = f.getPrefix();
                float prefixWidth = Fonts.sfui.getWidth(prefix, fontSize);
                String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
                float nameWidth = Fonts.sfui.getWidth(staff, fontSize);

                float localWidth = prefixWidth + nameWidth + Fonts.sfui.getWidth(f.getStatus().string, fontSize) + padding * 3;

                Fonts.sfui.drawText(ms, prefix, posX + padding, posY + 1, fontSize - 0.5f, 255);
                Fonts.sfui.drawText(ms, staff, posX + padding + prefixWidth, posY + 1, -1, fontSize - 0.5f);
                Fonts.sfui.drawText(ms, f.getStatus().string, posX + width - padding - Fonts.sfui.getWidth(f.getStatus().string, fontSize), posY
                        + 1, f.getStatus().color, fontSize - 0.5f);

                if (localWidth > maxWidth) {
                    maxWidth = localWidth;
                }

                posY += fontSize + padding;
                localHeight += fontSize + padding;
            }

            width = Math.max(maxWidth, 80);
            height = localHeight + 2.5f;
            dragging.setWidth(width);
            dragging.setHeight(height);
        } finally {
            Scissor.pop();
        }
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", -1),
        VANISHED("V", ColorUtils.rgb(254, 68, 68));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }
}