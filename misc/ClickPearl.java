package minecraft.rolest.modules.impl.misc;

import minecraft.rolest.events.EventKey;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.events.TickEvent;
import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;

import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.impl.combat.ItemCooldown;
import minecraft.rolest.modules.settings.impl.BindSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.utils.client.ClientUtil;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "ClickPearl", category = Category.Player,desc ="перка на бинд")
public class ClickPearl extends Module {
    final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Легитный");
    final BindSetting pearlKey = new BindSetting("Кнопка", -98);
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    final ItemCooldown itemCooldown;
    long delay;
    final StopWatch waitMe = new StopWatch();
    final StopWatch stopWatch = new StopWatch();
    final StopWatch stopWatch2 = new StopWatch();
    public ActionType actionType = ActionType.START;
    Runnable runnableAction;
    int oldSlot = -1;

    public ClickPearl(ItemCooldown itemCooldown) {
        this.itemCooldown = itemCooldown;
        addSettings(mode, pearlKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == pearlKey.get()) {
            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (ClientUtil.isConnectedToServer("funtime")) {
                    if (!waitMe.isReached(400)) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            keyBinding.setPressed(false);
                        }
                        return;
                    }
                }

                sendRotatePacket();

                oldSlot = mc.player.inventory.currentItem;

                if (mode.is("Обычный")) {
                    InventoryUtil.inventorySwapClick(Items.ENDER_PEARL, true);

                } else {
                    if (runnableAction == null) {
                        actionType = ActionType.START;
                        runnableAction = () -> vebatSoli();
                        stopWatch.reset();
                        stopWatch2.reset();
                    }
                }
            } else {
                ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

                if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                    itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
                }
            }
        }
    }

    @Subscribe
    public void onTick(TickEvent e) {
        if (runnableAction != null) {
            runnableAction.run();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void vebatSoli() {
        int slot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        Hand hand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (slot != -1) {
            interact(slot, hand);
        } else {
            runnableAction = null;
        }
    }

    private void swingAndSendPacket(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }

    private void interact(Integer slot, Hand hand) {
        if (actionType == ActionType.START) { // начало
            switchSlot(slot, hand);
            actionType = ActionType.WAIT;
        } else if (actionType == ActionType.WAIT && stopWatch.isReached(150L)) { // какая та хуйня
            actionType = ActionType.USE_ITEM;
        } else if (actionType == ActionType.USE_ITEM) {
            sendRotatePacket();
            swingAndSendPacket(hand);
            switchSlot(mc.player.inventory.currentItem, hand);
            actionType = ActionType.SWAP_BACK;
        } else if (actionType == ActionType.SWAP_BACK && stopWatch2.isReached(400L)) { // задержка на свап обратно
            mc.player.inventory.currentItem = oldSlot;
            runnableAction = null;
        }
    }


    private int findPearlAndThrow() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);




            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }

        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);

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


    private void switchSlot(int slot, Hand hand) {
        if (slot != mc.player.inventory.currentItem && hand != Hand.OFF_HAND) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            mc.player.inventory.currentItem = slot;
        }
    }

    private void sendRotatePacket() {
        if (Rol.getInstance().getModuleManager().getHitAura().getTarget() != null) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        }
    }

    public enum ActionType {
        START, WAIT, USE_ITEM, SWAP_BACK
    }
}