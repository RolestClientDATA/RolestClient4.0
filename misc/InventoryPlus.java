package minecraft.rolest.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CCloseWindowPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@ModuleRegister(name = "InventoryPlus", category = Category.Misc,desc ="инв помощник")
public class InventoryPlus extends Module {

    public BooleanSetting xcarry = new BooleanSetting("XCarry", false);
    public BooleanSetting itemScroller = new BooleanSetting("ItemScroller", true);
    public BooleanSetting autoArmor = new BooleanSetting("AutoArmor", true);
    final SliderSetting delay = new SliderSetting("Задержка", 100.0f, 0.0f, 1000.0f, 1.0f).setVisible(() -> autoArmor.get());
    final BooleanSetting onlyInv = new BooleanSetting("Только в инве", false).setVisible(() -> autoArmor.get());
    final BooleanSetting workInMove = new BooleanSetting("Работать в движении", true).setVisible(() -> autoArmor.get());
    final StopWatch stopWatchAutoArmor = new StopWatch();

    public InventoryPlus() {
        addSettings(xcarry, itemScroller, autoArmor, delay, onlyInv, workInMove);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (autoArmor.get()) {
            if (!workInMove.get()) {
                if (MoveUtils.isMoving()) {
                    return;
                }
            }

            if (onlyInv.get()) {
                if (!(mc.currentScreen instanceof InventoryScreen)) {
                    return;
                }
            }
            PlayerInventory inventoryPlayer = AutoActions.mc.player.inventory;
            int[] bestIndexes = new int[4];
            int[] bestValues = new int[4];

            for (int i = 0; i < 4; ++i) {
                bestIndexes[i] = -1;
                ItemStack stack = inventoryPlayer.armorItemInSlot(i);

                if (!isItemValid(stack) || !(stack.getItem() instanceof ArmorItem armorItem)) {
                    continue;
                }

                bestValues[i] = calculateArmorValue(armorItem, stack);
            }

            for (int i = 0; i < 36; ++i) {
                Item item;
                ItemStack stack = inventoryPlayer.getStackInSlot(i);

                if (!isItemValid(stack) || !((item = stack.getItem()) instanceof ArmorItem)) continue;

                ArmorItem armorItem = (ArmorItem) item;
                int armorTypeIndex = armorItem.getSlot().getIndex();
                int value = calculateArmorValue(armorItem, stack);

                if (value <= bestValues[armorTypeIndex]) continue;

                bestIndexes[armorTypeIndex] = i;
                bestValues[armorTypeIndex] = value;
            }

            ArrayList<Integer> randomIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
            Collections.shuffle(randomIndexes);

            for (int index : randomIndexes) {
                int bestIndex = bestIndexes[index];

                if (bestIndex == -1 || (isItemValid(inventoryPlayer.armorItemInSlot(index)) && inventoryPlayer.getFirstEmptyStack() == -1))
                    continue;

                if (bestIndex < 9) {
                    bestIndex += 36;
                }

                if (!this.stopWatchAutoArmor.isReached(this.delay.get().longValue())) break;

                ItemStack armorItemStack = inventoryPlayer.armorItemInSlot(index);

                if (isItemValid(armorItemStack)) {
                    AutoActions.mc.playerController.windowClick(0, 8 - index, 0, ClickType.QUICK_MOVE, AutoActions.mc.player);
                }

                AutoActions.mc.playerController.windowClick(0, bestIndex, 0, ClickType.QUICK_MOVE, AutoActions.mc.player);
                this.stopWatchAutoArmor.reset();
                break;
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof CCloseWindowPacket && xcarry.get()) {
            e.cancel();
        }
    }

    private boolean isItemValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    private int calculateArmorValue(final ArmorItem armor, final ItemStack stack) {
        final int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        final IArmorMaterial armorMaterial = armor.getArmorMaterial();
        final int damageReductionAmount = armorMaterial.getDamageReductionAmount(armor.getEquipmentSlot());
        return ((armor.getDamageReduceAmount() * 20 + protectionLevel * 12 + (int) (armor.getToughness() * 2) + damageReductionAmount * 5) >> 3);
    }
}
