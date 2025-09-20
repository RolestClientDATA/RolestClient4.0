package minecraft.rolest.modules.impl.player;

import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.Setting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.StopWatch;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(
        name = "TrainAssist",
        category = Category.Misc
)
public class TrainLut extends Module {
    private final SliderSetting rangenuking = new SliderSetting("Дистанция Открития", 4.5F, 1.0F, 6.0F, 0.1F);
    private final SliderSetting intervalnuking = new SliderSetting("Задержка Открития", 70.0F, 1.0F, 1500.0F, 1.0F);
    private final SliderSetting delay = new SliderSetting("Скорость Лутания", 10.0F, 0.0F, 300.0F, 1.0F);
    private final StopWatch stopWatch = new StopWatch();
    private long lastOpenTime = 0L;
    private BlockPos lastOpenedBlock = null;

    public TrainLut() {
        this.addSettings(new Setting[]{this.rangenuking, this.intervalnuking, this.delay});
    }


    public void onUpdate(EventUpdate e) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        Vector3d playerPos = player.getPositionVec();
        long blockBreakInterval = (long) Math.round((Float) this.intervalnuking.get());

        if (getFreeSlots(player.inventory) <= 3) {
            this.disable();
            return;
        }

        if (System.currentTimeMillis() - lastOpenTime >= blockBreakInterval) {
            int range = Math.round((Float) this.rangenuking.get());
            BlockPos nearestBlock = findNearestChest(playerPos, range);

            if (nearestBlock != null) {
                rotateToChest(nearestBlock); // Поворачиваем камеру к сундуку
                BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(playerPos, Direction.UP, nearestBlock, false);
                mc.playerController.processRightClickBlock((ClientPlayerEntity) player, mc.world, Hand.MAIN_HAND, rayTraceResult);
                lastOpenTime = System.currentTimeMillis();
                lastOpenedBlock = nearestBlock;
            }
        }

        if (lastOpenedBlock != null && System.currentTimeMillis() - lastOpenTime >= 20L) {
            if (!containsDye(mc.player.openContainer)) {
                mc.player.closeScreen();
            }
            lastOpenedBlock = null;
        }

        // Заграбление лута
        Container container = mc.player.openContainer;
        if (container instanceof ChestContainer chestContainer) {
            IInventory lowerChestInventory = chestContainer.getLowerChestInventory();
            boolean isEmpty = true;

            for (int index = 0; index < lowerChestInventory.getSizeInventory(); ++index) {
                ItemStack stack = lowerChestInventory.getStackInSlot(index);
                if (shouldMoveItem(chestContainer, index) && !isContainerEmpty(stack)) {
                    isEmpty = false;
                    if ((Float) this.delay.get() == 0.0F) {
                        moveItem(chestContainer, index, lowerChestInventory.getSizeInventory());
                    } else if (stopWatch.isReached(((Float) this.delay.get()).longValue())) {
                        mc.playerController.windowClick(chestContainer.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                        stopWatch.reset();
                    }
                }
            }
            if (isEmpty) {
                mc.player.closeScreen();
            }
        }
    }

    private void disable() {
    }

    private BlockPos findNearestChest(Vector3d playerPos, int range) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos nearestBlock = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int dx = -range; dx <= range; ++dx) {
            for (int dy = -range; dy <= range; ++dy) {
                for (int dz = -range; dz <= range; ++dz) {
                    BlockPos targetPos = new BlockPos(playerPos.x + dx, playerPos.y + dy, playerPos.z + dz);
                    Block block = mc.world.getBlockState(targetPos).getBlock();

                    if (block == Blocks.CHEST || block == Blocks.BARREL || block == Blocks.ENDER_CHEST) {
                        double distance = playerPos.distanceTo(new Vector3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5));
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestBlock = targetPos;
                        }
                    }
                }
            }
        }
        return nearestBlock;
    }

    private void rotateToChest(BlockPos chestPos) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        // Вычисляем направление к сундуку
        double dX = chestPos.getX() + 0.5 - player.getPosX();
        double dZ = chestPos.getZ() + 0.5 - player.getPosZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F;

        // Плавная ротация
        float currentYaw = player.rotationYaw;
        float yawSpeed = 4.0f;

        // Изменяем yaw с учетом максимальной скорости поворота
        if (Math.abs(targetYaw - currentYaw) > yawSpeed) {
            if (targetYaw > currentYaw) {
                currentYaw += yawSpeed;
                if (currentYaw > targetYaw) {
                    currentYaw = targetYaw;
                }
            } else {
                currentYaw -= yawSpeed;
                if (currentYaw < targetYaw) {
                    currentYaw = targetYaw;
                }
            }
        } else {
            currentYaw = targetYaw;
        }

        player.rotationYaw = currentYaw;
    }


    private boolean containsDye(Container container) {
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (isWhiteListItem(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldMoveItem(ChestContainer container, int index) {
        ItemStack itemStack = container.getLowerChestInventory().getStackInSlot(index);
        return itemStack.getItem() != Item.getItemById(0);
    }

    private void moveItem(ChestContainer container, int index, int multi) {
        for (int i = 0; i < multi; ++i) {
            Minecraft mc = Minecraft.getInstance();
            mc.playerController.windowClick(container.windowId, index + i, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }

    public boolean isWhiteListItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item == Items.WHITE_DYE || item == Items.BLACK_DYE || item == Items.RED_DYE ||
                item == Items.GREEN_DYE || item == Items.BROWN_DYE || item == Items.BLUE_DYE ||
                item == Items.PURPLE_DYE || item == Items.LIGHT_BLUE_DYE || item == Items.LIGHT_GRAY_DYE ||
                item == Items.CYAN_DYE || item == Items.PINK_DYE || item == Items.GRAY_DYE ||
                item == Items.LIME_DYE || item == Items.YELLOW_DYE || item == Items.MAGENTA_DYE ||
                item == Items.ORANGE_DYE;
    }

    private boolean isContainerEmpty(ItemStack stack) {
        return !this.isWhiteListItem(stack);
    }

    private int getFreeSlots(IInventory inventory) {
        int freeSlots = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                freeSlots++;
            }
        }
        return freeSlots;
    }
}