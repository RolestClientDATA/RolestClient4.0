package minecraft.rolest.ui.display.impl;

import minecraft.rolest.ui.display.ElementRenderer;
import minecraft.rolest.events.EventDisplay;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ArmorRenderer implements ElementRenderer {

    @Override
    public void render(EventDisplay eventDisplay) {

        int posX = window.getScaledWidth() / 2 + 95;
        int posY = window.getScaledHeight() - (16 + 2);

        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY, null);

            posX += 16 + 2;
        }
    }
}
