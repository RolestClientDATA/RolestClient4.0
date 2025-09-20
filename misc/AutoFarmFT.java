package minecraft.rolest.modules.impl.misc;



import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.utils.client.InvUtil;
import minecraft.rolest.utils.client.TimerUtility;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.MoveUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@ModuleRegister(name = "AutoFarm", category = Category.Misc,desc ="афк заработак")
public class AutoFarmFT extends Module {
    private final TimerUtility suspiciousTimer = new TimerUtility();
    private long lastMessageTime = 0L;
    private final StopWatch stopWatchMain = new StopWatch();
    private final StopWatch stopWatch = new StopWatch();
    private boolean repair, exp;
    private boolean isEating;
    private ModeSetting motyga = new ModeSetting("Вид мотыги", "Незер", "Незер", "Алмазная");
    public final BooleanSetting clan = new BooleanSetting("Класть деньги в клан", false);
    public final BooleanSetting kushat = new BooleanSetting("Анти Голод", false);
    public final BooleanSetting camerablat = new BooleanSetting("Поварачивать Камеру", false);
    private Item motygaitem;
    public AutoFarmFT() {
        addSettings(motyga, clan,kushat,camerablat);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        repair = false;
        exp = false;
    }


    @Subscribe
    public void onUpdate(EventUpdate e) {
        List<Item> landingItems = List.of(Items.POTATO, Items.CARROT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
        Slot hoeSlot;
        if (motyga.is("Незер")) {
            hoeSlot = InvUtil.getInventorySlot(Items.NETHERITE_HOE);
            motygaitem = Items.NETHERITE_HOE;
        } else {
            hoeSlot = InvUtil.getInventorySlot(Items.DIAMOND_HOE);
            motygaitem = Items.DIAMOND_HOE;
        }
        Slot expSlot = InvUtil.getInventorySlot(Items.EXPERIENCE_BOTTLE);
        Slot landingSlot = InvUtil.getInventorySlot(landingItems);
        int expCount = InvUtil.getInventoryCount(Items.EXPERIENCE_BOTTLE);
        Item mainHandItem = mc.player.getHeldItemMainhand().getItem();
        Item offHandItem = mc.player.getHeldItemOffhand().getItem();

        if (hoeSlot == null || MoveUtils.isMoving() || !stopWatchMain.isReached(500)) return;

        float itemStrength = 1 - MathHelper.clamp((float) hoeSlot.getStack().getDamage() / (float) hoeSlot.getStack().getMaxDamage(), 0, 1);


        if (itemStrength < 0.04) {
            repair = true;

        } else if (repair && itemStrength > 0.85) {
            repair = false;
            stopWatchMain.reset();
            exp = false;
            return;
        }
        exp = expCount >= 55 || expCount != 0 && exp;
        if (camerablat.get())
            mc.player.rotationPitch = 90;

        if (mc.player.inventory.getFirstEmptyStack() == -1) {
            if (!landingItems.contains(offHandItem)) {
                InvUtil.clickSlot(landingSlot, 40, ClickType.SWAP, false);
                return;
            }
            if (mc.currentScreen instanceof ContainerScreen<?> screen) {
                if (screen.getTitle().getString().equals("● Выберите секцию")) {
                    InvUtil.clickSlotId(21, 0, ClickType.PICKUP, true);
                    return;
                }
                if (screen.getTitle().getString().equals("Скупщик еды")) {
                    int slotIdSell = offHandItem.equals(Items.CARROT) ? 10 : offHandItem.equals(Items.POTATO) ? 11 : offHandItem.equals(Items.BEETROOT_SEEDS) ? 12 : 14;
                    InvUtil.clickSlotId(slotIdSell, 0, ClickType.PICKUP, true);
                    return;
                }
            }
            if (stopWatch.isReached(1000)) {
                mc.player.sendChatMessage("/buyer");
                stopWatch.reset();
            }
        } else if (repair) {
            if (exp) {
                if (mc.currentScreen instanceof ContainerScreen<?>) {
                    mc.player.closeScreen();
                    stopWatchMain.reset();
                    if (stopWatch.isReached(100)) ;
                } else if (mainHandItem.equals(motygaitem) && offHandItem.equals(Items.EXPERIENCE_BOTTLE)) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                } else {
                    if (!offHandItem.equals(Items.EXPERIENCE_BOTTLE)) {
                        if (stopWatch.isReached(100)) ;
                        InvUtil.clickSlot(expSlot, 40, ClickType.SWAP, false);
                        if (stopWatch.isReached(100)) ;

                    }
                    if (!mainHandItem.equals(motygaitem)) {
                        if (stopWatch.isReached(100)) ;
                        InvUtil.clickSlot(hoeSlot, mc.player.inventory.currentItem, ClickType.SWAP, false);
                    }
                }
            } else if (stopWatch.isReached(800)) {
                if (mc.currentScreen instanceof ContainerScreen<?> screen) {
                    if (screen.getTitle().getString().contains("Пузырек опыта")) {
                        mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getTag() != null && s.slotNumber < 45)
                                .min(Comparator.comparingInt(s -> AutoBuyexcellent.getPrice(s.getStack()) / s.getStack().getCount()))
                                .ifPresent(s -> InvUtil.clickSlot(s, 0, ClickType.QUICK_MOVE, true));
                        stopWatch.reset();
                        return;
                    } else if (screen.getTitle().getString().contains("Подозрительная цена")) {
                        if (stopWatch.isReached(500)) {
                            InvUtil.clickSlotId(0, 0, ClickType.QUICK_MOVE, true);
                            suspiciousTimer.reset();
                        }
                        return;
                    }
                }
                mc.player.sendChatMessage("/ah search Пузырёк Опыта");
                stopWatch.reset();
            }


        } else {
            BlockPos pos = mc.player.getPosition();
            if (mc.world.getBlockState(pos).getBlock().equals(Blocks.FARMLAND)) {
                if (mainHandItem.equals(motygaitem) && landingItems.contains(offHandItem)) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.OFF_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos, false)));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos.up(), false)));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos.up(), false)));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos.up(), false)));
                    mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pos.up(), Direction.UP));
                } else {
                    if (mc.currentScreen instanceof ContainerScreen<?>) {
                        mc.player.closeScreen();
                        stopWatchMain.reset();
                        return;
                    }
                    if (!mainHandItem.equals(motygaitem)) {
                        InvUtil.clickSlot(hoeSlot, mc.player.inventory.currentItem, ClickType.SWAP, false);
                    }
                    if (!landingItems.contains(offHandItem)) {
                        InvUtil.clickSlot(landingSlot, 40, ClickType.SWAP, false);
                    }
                    if (clan.get())
                        if (System.currentTimeMillis() - this.lastMessageTime >= 190000L) {
                            Minecraft.player.sendChatMessage("/clan invest 1000000");
                            this.lastMessageTime = System.currentTimeMillis();
                        }

                    if (kushat.get())
                        if (mc.player.getFoodStats().getFoodLevel() < 10) {
                            startEating();

                        } else if (mc.player.getFoodStats().getFoodLevel() > 20) {
                            stopEating();

                        }

                }
            }
        }
    }


    class AutoBuyexcellent {

        public static int getPrice(ItemStack itemStack) {
            CompoundNBT tag = itemStack.getTag();
            if (tag == null) return -1;
            String price = StringUtils.substringBetween(tag.toString(), "\"text\":\" $", "\"}]");
            if (price == null || price.isEmpty()) return -1;
            price = price.replaceAll(" ", "").replaceAll(",", "");
            return Integer.parseInt(price);
        }

    }



    private void stopEating() {
        mc.gameSettings.keyBindUseItem.setPressed(false);
        isEating = false;
    }

    private void startEating() {
        if (mc.currentScreen != null) {
            mc.currentScreen.passEvents = true;
        }
        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
            isEating = true;
        }
    }
}