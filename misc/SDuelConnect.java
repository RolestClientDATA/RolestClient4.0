package minecraft.rolest.modules.impl.misc;


import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.utils.player.InventoryUtil;

@ModuleRegister(name="SDuelConnect", desc=" фаст заход дуэли SpookyTime", category= Category.Player)
public class SDuelConnect
        extends Module {
    public static void selectCompass() {
        int n = InventoryUtil.getHotBarSlot(Items.COMPASS);
        if (n == -1) {
            return;
        }
        SDuelConnect.mc.player.inventory.currentItem = n;
        SDuelConnect.mc.player.connection.sendPacket(new CHeldItemChangePacket(n));
        SDuelConnect.mc.playerController.processRightClick(SDuelConnect.mc.player, SDuelConnect.mc.world, Hand.MAIN_HAND);
    }

    @Subscribe
    private void onUpdate(EventUpdate eventUpdate) {
        Screen screen = SDuelConnect.mc.currentScreen;
        if (screen instanceof ChestScreen) {
            ChestScreen chestScreen = (ChestScreen)screen;
            for (int i = 0; i < ((ChestContainer)chestScreen.getContainer()).inventorySlots.size(); ++i) {
                if (((ChestContainer)chestScreen.getContainer()).getSlot(i).getStack().getItem() != Items.STICKY_PISTON) continue;
                ItemStack itemStack = ((ChestContainer)chestScreen.getContainer()).getSlot(i).getStack();
                SDuelConnect.mc.player.connection.sendPacket(new CClickWindowPacket(((ChestContainer)chestScreen.getContainer()).windowId, i, 0, ClickType.PICKUP, itemStack, ((ChestContainer)chestScreen.getContainer()).getNextTransactionID(SDuelConnect.mc.player.inventory)));
                return;
            }
        } else {
            SDuelConnect.selectCompass();
        }
    }

    @Subscribe
    private void onPacket(EventPacket eventPacket) {
        String string = SDuelConnect.mc.ingameGUI.getTabList().header.getString();
        if (string.contains("\u041f\u0440\u0438\u0432\u0430\u0442\u043a\u0430")) {
            this.print("\u0423\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u043b\u0441\u044f \u043d\u0430 \u0434\u0443\u044d\u043b\u0438!");
            this.toggle();
        }
    }
}

