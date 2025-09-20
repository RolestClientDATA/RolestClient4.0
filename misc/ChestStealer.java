

package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.util.text.ITextComponent;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.client.ClientUtil;
import minecraft.rolest.utils.client.TimerUtility;
;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ChestStealer", category = Category.Misc, desc = "nn")
public class ChestStealer extends Module {



    private final SliderSetting stealDelay = new SliderSetting("Задержка",  50, 0, 500, 1);
    private final BooleanSetting random = new BooleanSetting("Рандом",  true);
    private final BooleanSetting closeEmpty = new BooleanSetting("Закрывать пустые",  true);
    private final BooleanSetting closeIsFull = new BooleanSetting("Закр. если фулл инв",  true);
    private final BooleanSetting leaveAfterLoot = new BooleanSetting("Ливать после лута",  false);
    private final BooleanSetting missSlots = new BooleanSetting("Промахиватся",  true);
    private final TimerUtility timerUtil =  new TimerUtility();

    public ChestStealer(){
        addSettings(stealDelay,random,closeEmpty,closeIsFull,leaveAfterLoot,missSlots);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player.openContainer instanceof ChestContainer container) {
            List<Integer> slotsForLoot = new ArrayList<>();
            for (int index = 0; index < container.inventorySlots.size(); ++index) {
                if (!container.getLowerChestInventory().getStackInSlot(index).isEmpty()) {
                    slotsForLoot.add(index);
                }
            }
            lootItems(slotsForLoot, container);
            if (closeEmpty.get() && container.getLowerChestInventory().isEmpty()) mc.player.closeScreen();
            if (leaveAfterLoot.get() && container.getLowerChestInventory().isEmpty() && !ClientUtil.isPvp())
                mc.player.connection.getNetworkManager().closeChannel(ITextComponent.getTextComponentOrEmpty("Вы покинули сервер \n" + " причина: Сундук пуст!"));
            if (missSlots.get()) missSlots(container);
            if (closeIsFull.get() && mc.player.inventory.getFirstEmptyStack() == -1) mc.player.closeScreen();
        }
    }





    private void missSlots(ChestContainer container) {
        int containerSize = container.getLowerChestInventory().getSizeInventory();

        for (int index = 0; index < containerSize; ++index) {
            if (container.getLowerChestInventory().getStackInSlot(index).isEmpty()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.1 && mc.player.ticksExisted % 30 == 0) {
                    mc.playerController.windowClick(container.windowId, index, 0, ClickType.PICKUP, mc.player);
                    return;
                }
            }
        }

    }

    private void lootItems(List<Integer> slots, ChestContainer container) {
        if (random.get()) Collections.shuffle(slots, ThreadLocalRandom.current());
        for (int index : slots) {
            if (timerUtil.hasReached(stealDelay.get().longValue(), true)) {
                mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

}