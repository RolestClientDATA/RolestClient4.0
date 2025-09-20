package minecraft.rolest.modules.impl.misc;


import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.NotifyNigt;
import com.google.common.eventbus.Subscribe;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.utils.player.InventoryUtil;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;


@ModuleRegister(name = "FTAssist", category = Category.Player,desc ="фт хелпер боже")
public class FTHelper extends Module {


    private final BindSetting disorientationKey = new BindSetting("Кнопка дезорентации", -1);
    private final BindSetting trapKey = new BindSetting("Кнопка трапки", -1);
    private final BindSetting blatantKey = new BindSetting("Кнопка явной пыли", -1);
    private final BindSetting bowKey = new BindSetting("Кнопка арбалета", -1);
    private final BindSetting otrigaKey = new BindSetting("Кнопка отрыжки", -1);
    private final BindSetting serkaKey = new BindSetting("Кнопка серки", -1);
    private final BindSetting plastKey = new BindSetting("Кнопка пласта", -1);

    InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    boolean disorientationThrow, trapThrow, blatantThrow, serkaThrow, otrigaThrow, bowThrow, plastThrow;

    public FTHelper() {
        addSettings(disorientationKey, trapKey, blatantKey, serkaKey, bowKey, otrigaKey, plastKey);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == disorientationKey.get()) {
            disorientationThrow = true;
        }

        if (e.getKey() == trapKey.get()) {
            trapThrow = true;
        }

        if (e.getKey() == blatantKey.get()) {
            blatantThrow = true;
        }
        if (e.getKey() == otrigaKey.get()) {
            otrigaThrow = true;
        }
        if (e.getKey() == serkaKey.get()) {
            serkaThrow = true;
        }
        if (e.getKey() == bowKey.get()) {
            bowThrow = true;
        }
        if (e.getKey() == plastKey.get()) {
            plastThrow = true;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("дезориентация", true);
            int invSlot = getItemForName("дезориентация", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Дезориентация не найдена.", "", 3);
                disorientationThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Дезориентация была юзанута.", "", 3);
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            disorientationThrow = false;
        }

        if (trapThrow) {
            int hbSlot = getItemForName("трапка", true);
            int invSlot = getItemForName("трапка", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Трапка не найдена.", "", 3);
                trapThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
               NotifyNigt.NOTIFICATION_MANAGER.add("Tрапкa была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            trapThrow = false;
        }

        if (bowThrow) {
            int hbSlot = getItem(Items.CROSSBOW, true);
            int invSlot = getItem(Items.CROSSBOW, false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Арбалет не найден.", "", 3);
                bowThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.CROSSBOW)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Арбалет была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            bowThrow = false;
        }

        if (serkaThrow) {
            int hbSlot = getItemForName("серная", true);
            int invSlot = getItemForName("серная", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Серка отсутствует.", "", 3);
                serkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Cеркa была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            serkaThrow = false;
        }

        if (otrigaThrow) {
            int hbSlot = getItemForName("отрыжки", true);
            int invSlot = getItemForName("отрыжки", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Отрыга отсутствует.", "", 3);
                otrigaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Отрыга была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            otrigaThrow = false;
        }

        if (plastThrow) {
            int hbSlot = getItemForName("пласт", true);
            int invSlot = getItemForName("пласт", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Пласт не найдена", "", 3);
                plastThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Пласт была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            plastThrow = false;
        }

        if (blatantThrow) {
            int hbSlot = getItemForName("явная", true);
            int invSlot = getItemForName("явная", false);

            if (invSlot == -1 && hbSlot == -1) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Явная пыль не найдена", "", 3);
                blatantThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.TNT)) {
                NotifyNigt.NOTIFICATION_MANAGER.add("Явная пыль была успешно юзанута.", "", 3);
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            blatantThrow = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }


    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        disorientationThrow = false;
        trapThrow = false;
        blatantThrow = false;
        plastThrow = false;
        otrigaThrow = false;
        serkaThrow = false;
        bowThrow = false;
        delay = 0;
        super.onDisable();
    }

    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }
    private int getItem(Item input, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }
            if (itemStack.getItem() == input) {
                return i;
            }
        }
        return -1;

    }
}