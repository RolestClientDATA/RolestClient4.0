package minecraft.rolest.modules.impl.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "Velocity", category = Category.Combat,desc ="ахуй нада")
public class   Velocity extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Cancel", "Cancel", "Grim Skip", "Grim Cancel", "Grim Cancel 2", "Grim New", "Funtime");

    private int skip = 0;
    private boolean cancel;
    boolean damaged;
    private boolean flag;
    private int ccCooldown;
    boolean work = false;

    public Velocity() {
        addSettings(mode);
    }


    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.isReceive()) {
            if (e.getPacket() instanceof SEntityVelocityPacket p && p.getEntityID() != mc.player.getEntityId()) return;
            switch (mode.getIndex()) {
                case 0 -> { // Cancel
                    if (e.getPacket() instanceof SEntityVelocityPacket) {
                        e.cancel();
                    }
                }

                case 1 -> { // Grim Skip
                    if (e.getPacket() instanceof SEntityVelocityPacket) {
                        skip = 6;
                        e.cancel();
                    }

                    if (e.getPacket() instanceof CPlayerPacket) {
                        if (skip > 0) {
                            skip--;
                            e.cancel();
                        }
                    }
                }

                case 2 -> { // Grim Cancel
                    if (e.getPacket() instanceof SEntityVelocityPacket) {
                        e.cancel();
                        cancel = true;
                    }
                    if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                        skip = 3;
                    }

                    if (e.getPacket() instanceof CPlayerPacket) {
                        skip--;
                        if (cancel) {
                            if (skip <= 0) {
                                BlockPos blockPos = new BlockPos(mc.player.getPositionVec());
                                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                            }
                            cancel = false;
                        }
                    }
                }
            }

            if (mode.is("Grim Cancel 2")) {
                if (e.getPacket() instanceof SEntityVelocityPacket wrapper) {
                    if (wrapper.getEntityID() != mc.player.getEntityId() || skip < 0) return;
                    skip = 8;
                    e.cancel();
                }

                if (e.getPacket() instanceof SConfirmTransactionPacket) {
                    if (skip < 0) skip++;
                    else if (skip > 1) {
                        skip--;
                        e.cancel();
                    }
                }

                if (e.getPacket() instanceof SPlayerPositionLookPacket) skip = -8;

            }

            if (mode.is("Funtime")) {
                if (e.getPacket() instanceof SEntityVelocityPacket p) {
                    if (skip >= 2) {
                        return;
                    }
                    if (p.getEntityID() != mc.player.getEntityId()) {
                        return;
                    }
                    e.cancel();
                    damaged = true;
                }
                if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                    skip = 3;
                }
            }

            if (mode.is("Grim New")) {
                if (ccCooldown > 0) {
                    ccCooldown--;
                } else {
                    if (e.getPacket() instanceof SEntityVelocityPacket wrapper) {
                        if (wrapper.getEntityID() == mc.player.getEntityId()) {
                            e.cancel();
                            flag = true;
                        }
                    }
                    if (e.getPacket() instanceof SExplosionPacket) {
                        if (!Rol.getInstance().getModuleManager().getAntiPush().modes.getValueByName("Кристалы").get())
                            e.cancel();
                        flag = true;
                    }
                    if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                        ccCooldown = 5;
                    }
                }
            }
        }
    }


    BlockPos blockPos;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Funtime")) {
            skip--;
            if (damaged) {
                BlockPos blockPos = mc.player.getPosition();
                mc.player.connection.sendPacketWithoutEvent(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                damaged = false;
            }
        }

        if (mode.is("Grim New")) {
            if (flag) {
                if (ccCooldown <= 0) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                    mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, new BlockPos(
                            Math.floor(mc.player.getPositionVec().x),
                            Math.floor(mc.player.getPositionVec().y),
                            Math.floor(mc.player.getPositionVec().z)
                    ), Direction.UP));
                }
                flag = false;
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        skip = 0;
        cancel = false;
        damaged = false;
    }
}