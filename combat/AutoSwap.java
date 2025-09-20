package minecraft.rolest.modules.impl.combat;

import minecraft.rolest.Rol;
import minecraft.rolest.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;

import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.utils.math.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "AutoSwap", category = Category.Combat,desc ="помогает свапать")
public class AutoSwap extends Module {
    final ModeSetting itemType = new ModeSetting("Предмет 1", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting swapType = new ModeSetting("Предмет 2", "Геплы", "Щит", "Геплы", "Тотем", "Шар");
    final BindSetting keyToSwap = new BindSetting("Кнопка", -1);
    public StopWatch wait = new StopWatch();
    int itemSlot;
    private int swap = 0;
    public AutoSwap() {
        addSettings(itemType, swapType, keyToSwap);
    }
    @Subscribe
    public void onEventKey(EventKey e) {
        if (e.isKeyDown(keyToSwap.get())) {
            swap = 1;
        }
    }
    @Subscribe
    private void onUpdate(EventUpdate e) {
        Item currentOffhandItem = mc.player.getHeldItemOffhand().getItem();
        Item firstItem = getItemByType(itemType.get());
        Item secondItem = getItemByType(swapType.get());
        if (currentOffhandItem == firstItem) {
            itemSlot = getSlot(getItemByType(swapType.get()));
        } else if (currentOffhandItem == secondItem) {
            itemSlot = getSlot(getItemByType(itemType.get()));
        } else {
            itemSlot = getSlot(getItemByType(itemType.get()));
        }
        if (itemSlot == -1) {
            swap = 0;
        }
        if (swap == 1 && itemSlot != -1) {
            if (itemSlot >= 0 && itemSlot < 9) {
                BlockPos blockPos = mc.player.getPosition();
                mc.player.connection.sendPacket(new CHeldItemChangePacket(itemSlot));
                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, blockPos, Direction.UP));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                swap = 0;
            }
            if (itemSlot > 9) {
                if (Rol.getInstance().getModuleManager().getInvwalk().ftBypass.get()) {
                    final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                            mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                            mc.gameSettings.keyBindSprint};
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    if (!MoveUtils.isMoving()) {
                        mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);
                        swap = 0;
                    }
                    return;
                }
                mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);
                swap = 0;
            }
        }
    }
    private Item getItemByType(String type) {
        return switch (type) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }
    private int getSlot(Item item) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }
}