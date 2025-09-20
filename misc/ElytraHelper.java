package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ElytraAssist", category = Category.Player,desc ="помощник элитр")
public class ElytraHelper extends Module {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феерверков", -1);

    final BooleanSetting autoFireWork = new BooleanSetting("Авто феерверк", false);
    final BooleanSetting packet = new BooleanSetting("Обход", true);
    final SliderSetting timerFireWork = new SliderSetting("Таймер феера", 400, 100, 2000, 10).setVisible(() -> autoFireWork.get());
    final BooleanSetting autoFly = new BooleanSetting("Авто взлёт", false);
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, packet, autoFly, autoFireWork, timerFireWork);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static StopWatch stopWatch = new StopWatch();
    public static StopWatch fireWorkStopWatch = new StopWatch();
    long delay;
    boolean fireworkUsed;

    public StopWatch wait = new StopWatch();

    @Subscribe
    private void onEventKey(EventKey e) {
        if (e.getKey() == swapChestKey.get() && stopWatch.isReached(200L)) {
            changeChestPlate(currentStack);
            stopWatch.reset();
        }

        if (e.getKey() == fireWorkKey.get() && stopWatch.isReached(200L)) {
            if (mc.player.isElytraFlying())
                InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, false);
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (mc.player != null) {
            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (packet.get()) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }
        }

        if (autoFly.get() && currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(currentStack) && !mc.player.isElytraFlying() && !mc.player.abilities.isFlying) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        if (mc.player.isElytraFlying() && autoFireWork.get()) {
            if (fireWorkStopWatch.isReached(timerFireWork.get().longValue())) {
                InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET, false);
                fireWorkStopWatch.reset();
            }
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                InventoryUtil.moveItem(elytraSlot, 6);
                print(TextFormatting.RED + "Свапнул на элитру!");
                return;
            } else {
                print("Элитра не найдена!");
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0) {
            InventoryUtil.moveItem(armorSlot, 6);
            print(TextFormatting.RED + "Свапнул на нагрудник!");
        } else {
            print("Нагрудник не найден!");
        }
    }


    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        stopWatch.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
