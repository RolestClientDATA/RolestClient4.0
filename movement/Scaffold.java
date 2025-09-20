package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.EventInput;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.impl.misc.SlowPackets;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.MoveUtils;
import minecraft.rolest.utils.player.RayTraceUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;

@ModuleRegister(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    public BooleanSetting blink = new BooleanSetting("Блинк", false);
    public BooleanSetting fromBack = new BooleanSetting("Спина", true); // новая настройка: строить только сзади

    private BlockCache blockCache, lastBlockCache;
    public Vector2f rotation;
    private float savedY;
    private final Random random = new Random();
    public static final ConcurrentLinkedQueue<SlowPackets.TimedPacket> packets = new ConcurrentLinkedQueue<>();

    public Scaffold() {
        addSettings(blink, fromBack);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.timer.timerSpeed = 1;
        rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        for (SlowPackets.TimedPacket p : packets) {
            mc.player.connection.getNetworkManager().sendPacketWithoutEvent(p.getPacket());
        }
        packets.clear();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        if (mc.player != null)
            savedY = (float) mc.player.getPosY();
    }

    public boolean sneak;
    public StopWatch stopWatch = new StopWatch();

    @Subscribe
    public void onPacket(EventPacket e) {
        if (blink.get()) {
            if (mc.player != null && mc.world != null && !mc.isSingleplayer() && !mc.player.getShouldBeDead()) {
                if (e.isSend()) {
                    IPacket<?> packet = e.getPacket();
                    packets.add(new SlowPackets.TimedPacket(packet, System.currentTimeMillis()));
                    e.cancel();
                }
            } else setState(false, false);
        }
    }

    @Subscribe
    public void onInput(EventInput e) {
        if (rotation != null) {
            RayTraceResult result = RayTraceUtils.rayTrace(3, rotation.x, rotation.y, mc.player);
            if (result.getType() != RayTraceResult.Type.BLOCK && mc.world.getBlockState(mc.player.getPosition().add(0, -0.5f, 0)).getBlock() == Blocks.AIR) {
                e.setSneak(true);
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        mc.player.setSprinting(false);
        if (mc.player.isOnGround()) {
            savedY = (float) Math.floor(mc.player.getPosY() - 1.0);
        }

        blockCache = getBlockInfo();
        if (blockCache != null) {
            lastBlockCache = getBlockInfo();
        } else {
            return;
        }

        float[] rot = getRotations(blockCache.position, blockCache.facing);

        // Добавляем шум в повороты
        rot[0] += random.nextFloat() * 2 - 1; // случайное отклонение от -1 до +1
        rot[1] += random.nextFloat() * 1;     // чуть меньше для pitch

        rotation = new Vector2f(rot[0], rot[1]);

        e.setYaw(rotation.x);
        e.setPitch(rotation.y);

        mc.player.rotationYawHead = rotation.x;
        mc.player.renderYawOffset = rotation.x;
        mc.player.rotationPitchHead = rotation.y;

        if (blink.get()) {
            for (SlowPackets.TimedPacket timedPacket : packets) {
                if (System.currentTimeMillis() - timedPacket.getTime() >= 1000) {
                    mc.player.connection.getNetworkManager().sendPacketWithoutEvent(timedPacket.getPacket());
                    packets.remove(timedPacket);
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (blockCache == null || lastBlockCache == null) return;

        int block = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() instanceof BlockItem) {
                block = i;
                break;
            }
        }

        MoveUtils.setMotion(0.05);

        if (block == -1) {
            print("Не найдено блоков!");
            toggle();
            return;
        }

        if (rotation == null)
            return;

        RayTraceResult result = RayTraceUtils.rayTrace(3, rotation.x, rotation.y, mc.player);

        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND,
                new BlockRayTraceResult(getVector(lastBlockCache), lastBlockCache.getFacing(), lastBlockCache.getPosition(), false));
        mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
        blockCache = null;
        mc.player.inventory.currentItem = last;
    }

    public float[] getRotations(BlockPos blockPos, Direction enumFacing) {
        double d = (double) blockPos.getX() + 0.5 - mc.player.getPosX() + (double) enumFacing.getXOffset() * 0.25;
        double d2 = (double) blockPos.getZ() + 0.5 - mc.player.getPosZ() + (double) enumFacing.getZOffset() * 0.25;
        double d3 = mc.player.getPosY() + (double) mc.player.getEyeHeight() - blockPos.getY() - (double) enumFacing.getYOffset() * 0.25;
        double d4 = MathHelper.sqrt(d * d + d2 * d2);
        float f = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapDegrees(f), f2};
    }

    public class BlockCache {
        private final BlockPos position;
        private final Direction facing;

        public BlockCache(final BlockPos position, final Direction facing) {
            this.position = position;
            this.facing = facing;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public Direction getFacing() {
            return this.facing;
        }
    }

    public BlockCache getBlockInfo() {
        int y = (int) (mc.player.getPosY() - 1.0 >= savedY && Math.max(mc.player.getPosY(), savedY)
                - Math.min(mc.player.getPosY(), savedY) <= 3.0 && !mc.gameSettings.keyBindJump.isKeyDown()
                ? savedY
                : mc.player.getPosY() - 1.0);

        final BlockPos belowBlockPos = new BlockPos(mc.player.getPosX(), y - (mc.player.isSneaking() ? -1 : 0), mc.player.getPosZ());

        if (fromBack.get()) {
            // Только сзади игрока
            for (Direction direction : new Direction[]{Direction.NORTH, Direction.WEST}) {
                final BlockPos block = belowBlockPos.offset(direction);
                final Material material = mc.world.getBlockState(block).getBlock().getDefaultState().getMaterial();
                if (material.isSolid() && !material.isLiquid()) {
                    return new BlockCache(block, direction.getOpposite());
                }
            }
        } else {
            for (int x = 0; x < 1; x++) {
                for (int z = 0; z < 1; z++) {
                    for (int i = -1; i < 1; i += 1) {
                        final BlockPos blockPos = belowBlockPos.add(x * i, 0, z * i);
                        for (Direction direction : Direction.values()) {
                            final BlockPos block = blockPos.offset(direction);
                            final Material material = mc.world.getBlockState(block).getBlock().getDefaultState().getMaterial();
                            if (material.isSolid() && !material.isLiquid()) {
                                return new BlockCache(block, direction.getOpposite());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public Vector3d getVector(BlockCache data) {
        BlockPos pos = data.position;
        Direction face = data.facing;
        double x = (double) pos.getX() + 0.5, y = (double) pos.getY() + 0.5, z = (double) pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.5;
        } else {
            x += 0.3;
            z += 0.3;
        }
        if (face == Direction.WEST || face == Direction.EAST) {
            z += 0.15;
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += 0.15;
        }
        return new Vector3d(x, y, z);
    }
}