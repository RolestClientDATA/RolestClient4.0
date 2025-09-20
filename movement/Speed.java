package minecraft.rolest.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.rolest.Rol;
import minecraft.rolest.events.EventMotion;
import minecraft.rolest.events.EventPacket;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.events.MovingEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import minecraft.rolest.modules.settings.impl.ModeSetting;
import minecraft.rolest.modules.settings.impl.SliderSetting;
import minecraft.rolest.utils.math.StopWatch;
import minecraft.rolest.utils.player.InventoryUtil;
import minecraft.rolest.utils.player.MoveUtils;
import minecraft.rolest.utils.player.StrafeMovement;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@ModuleRegister(name = "Speed", category = Category.Movement)
public class Speed extends Module {

	private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Vanilla", "Matrix", "Grim", "NCP", "Timer", "Vulcan", "Funtime","LuckyWorld", "AAC", "RAC");
	private BooleanSetting autoJump = new BooleanSetting("Auto Jump", false).setVisible(() -> mode.is("Matrix") || mode.is("NCP") || mode.is("Vanilla"));
	private BooleanSetting spoofJump = new BooleanSetting("Spoof", false).setVisible(() -> mode.is("NCP") && autoJump.get());

	// aac
	private BooleanSetting longjump_aac = new BooleanSetting("LongJump", false).setVisible(() -> mode.is("AAC"));
	private BooleanSetting onground_aac = new BooleanSetting("OnGround", false).setVisible(() -> mode.is("AAC"));

	// grim
	private BooleanSetting motionboost_matrix = new BooleanSetting("Motion", true).setVisible(() -> mode.is("Matrix"));
	private BooleanSetting airboost_matrix = new BooleanSetting("AirBoost", false).setVisible(() -> mode.is("Matrix"));
	private BooleanSetting timerboost_matrix = new BooleanSetting("Timer", false).setVisible(() -> mode.is("Matrix"));

	// matrix
	private BooleanSetting strafeMove = new BooleanSetting("Strafe", false).setVisible(() -> mode.is("Matrix") && motionboost_matrix.get());
	private BooleanSetting entityboost_grim = new BooleanSetting("EntityBoost", true).setVisible(() -> mode.is("Grim"));
	private BooleanSetting blockboost_grim = new BooleanSetting("BlockBoost", true).setVisible(() -> mode.is("Grim"));
	private BooleanSetting timerboost_grim = new BooleanSetting("Timer", false).setVisible(() -> mode.is("Grim"));
	private BooleanSetting svboost_grim = new BooleanSetting("Second bypass", false).setVisible(() -> (mode.is("Grim") && entityboost_grim.get()));

	// vanilla
	private SliderSetting speed = new SliderSetting("Скорость", 1, 0.1f, 5, 0.1f).setVisible(() -> mode.is("Vanilla"));

	private StrafeMovement strafe = new StrafeMovement();
	private boolean enabled = false;
	public static int stage;
	public double less, stair, moveSpeed;
	public boolean slowDownHop, wasJumping, boosting, restart;
	private int prevSlot = -1;
	public StopWatch stopWatch = new StopWatch();
	public StopWatch racTimer = new StopWatch();

	public Speed() {
		addSettings(mode, speed, autoJump, spoofJump, blockboost_grim, entityboost_grim, svboost_grim, timerboost_grim, motionboost_matrix, strafeMove, airboost_matrix, timerboost_matrix, longjump_aac, onground_aac);
	}

	@Override
	public void onDisable() {
		mc.timer.timerSpeed = 1;
		super.onDisable();
	}

	@Subscribe
	public void onPacket(EventPacket e) {
		if (mode.is("Grim") && timerboost_grim.get()) {
			if (e.getPacket() instanceof CConfirmTransactionPacket p) {
				e.cancel();
			}
			if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
				mc.player.setPacketCoordinates(p.getX(), p.getY(), p.getZ());
				mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
				toggle();
			}
		}
	}

	@Subscribe
	public void onUpdate(EventUpdate e) {
		switch (mode.get()) {
			case "Matrix" -> {
				if (mc.player.isOnGround() && autoJump.get() && !mc.player.isInLava() && !mc.player.isInWater() && !airboost_matrix.get()) {
					mc.player.jump();
				}
			}

			case "Vanilla" -> {
				MoveUtils.setMotion(speed.get());

				if (autoJump.get() && mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) mc.player.jump();
			}

			case "RAC" -> {
				if (racTimer.isReached(10)) {
					if (mc.player.onGround && !mc.player.isJumping) {
						MoveUtils.setSpeed((float) MathHelper.clamp(MoveUtils.getSpeed() * (mc.player.rayGround ? 1.8 : 0.8), 0.2, MoveUtils.w() && mc.player.isSprinting() ? 1.715499997138977 : 1.7450000047683716));
						mc.player.rayGround = mc.player.onGround;
					} else {
						mc.player.serverSprintState = true;
						MoveUtils.setSpeed((float) MathHelper.clamp(MoveUtils.getSpeed() * (!mc.player.onGround && !mc.player.rayGround ? 1.2 : 1.0), 0.195, 1.823585033416748), 0.12F);
						mc.player.rayGround = mc.player.onGround;
					}

					racTimer.reset();
				}
			}

			case "Funtime", "LuckyWorld"-> {
				AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
				int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
				boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
				if (canBoost && !mc.player.isOnGround()) {
					mc.player.jumpMovementFactor = armorstans > 1 ? 1.0f / (float) armorstans : 0.16f;
				}
			}

			case "Grim" -> {
				if (timerboost_grim.get()) {
					if (stopWatch.isReached(1150)) {
						boosting = true;
					}
					if (stopWatch.isReached(7000)) {
						boosting = false;
						stopWatch.reset();
					}
					if (boosting) {
						if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						mc.timer.timerSpeed = (mc.player.ticksExisted % 2 == 0 ? 1.5f : 1.2f);
					} else {
						mc.timer.timerSpeed = (0.05f);
					}
				}

				if (blockboost_grim.get()) {
					int block = InventoryUtil.findBlockInHotbar();
					if (block == -1 || mc.player.isInWater()) {
						return;
					}
					if (mc.player.isOnGround()) {
						if (!wasJumping) {
							wasJumping = true;
							placeBlock();
							mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
						}
					} else {
						wasJumping = false;
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
					}
					if (mc.player.isOnGround()) {
						mc.player.jump();
					}
				}
			}

			case "Timer" -> {
				float timerValue = mc.player.fallDistance <= 0.25f ? 2.2f : (float) (mc.player.fallDistance != Math.ceil(mc.player.fallDistance) ? 0.4f : 1f);
				if (MoveUtils.isMoving()) {
					mc.timer.timerSpeed = timerValue;
					if (mc.player.onGround) {
						mc.player.jump();
					}
				} else {
					mc.timer.timerSpeed = 1.0f;
				}
			}


			case "Vulcan" -> {
				mc.player.jumpMovementFactor = 0.025f;
				if (mc.player.isOnGround() && MoveUtils.isMoving()) {
					if (mc.player.collidedHorizontally && mc.gameSettings.keyBindJump.pressed) {
						if (!mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						return;
					}
					mc.player.jump();
					mc.player.motion.y = 0.1f;
				}
			}

			case "AAC" -> {
				boolean longHop = longjump_aac.get() && (mc.player.isJumping || mc.player.fallDistance != 0.0F);
				boolean onGround = onground_aac.get() && !mc.player.isJumping && mc.player.onGround && mc.player.collidedVertically && MoveUtils.getSpeed() < 0.9;
				mc.timer.timerSpeed = 1.2f;
				if (longHop) {
					mc.player.jumpMovementFactor = 0.17F;
					mc.player.multiplyMotionXZ(1.005F);
				}

				if (onGround) {
					mc.player.multiplyMotionXZ(1.212F);
				}
			}
		}
	}

	@Subscribe
	public void onMotion(EventMotion move) {
		if (mode.is("NCP")) {
			if (!autoJump.get() && !mc.gameSettings.keyBindJump.isKeyDown()) {
				return;
			}

			mc.player.jumpMovementFactor = (float) ((double) mc.player.jumpMovementFactor * 1.04);
			boolean collided = mc.player.collidedHorizontally;

			if (collided) {
				stage = -1;
			}
			if (this.stair > 0.0) {
				this.stair -= 0.3;
			}
			this.less -= this.less > 1.0 ? 0.24 : 0.17;
			if (this.less < 0.0) {
				this.less = 0.0;
			}

			if (!mc.player.isInWater() && mc.player.onGround) {
				collided = mc.player.collidedHorizontally;
				if (stage >= 0 || collided) {
					stage = 0;
					float motY = 0.42f;
					if (spoofJump.get())
						mc.player.motion.y = motY;
					else
						mc.player.jump();
					this.less += 1.0;
					this.slowDownHop = this.less > 1.0 && !this.slowDownHop;
					if (this.less > 1.15) {
						this.less = 1.15;
					}
				}
			}
			this.moveSpeed = this.getCurrentSpeed(stage) + 0.0335;
			this.moveSpeed *= 0.85;
			if (this.stair > 0.0) {
				this.moveSpeed *= 1.0;
			}
			if (this.slowDownHop) {
				this.moveSpeed *= 0.8575;
			}
			if (mc.player.isInWater()) {
				this.moveSpeed = 0.351;
			}
			if (MoveUtils.isMoving()) {
				MoveUtils.setSpeed((float)moveSpeed);
			}
			++stage;
		}

		if (mode.is("Matrix") && timerboost_matrix.get()) {
			if (mc.player.isOnGround()) {
				mc.timer.timerSpeed = (1.1f);
			}
			if (mc.player.fallDistance > 0.1 && mc.player.fallDistance < 1) {
				mc.timer.timerSpeed = (1 + (1F - Math.floorMod((long) 2.520, (long) 2.600)));
			}
			if (mc.player.fallDistance >= 1) {
				mc.timer.timerSpeed = (0.978F);
			}
		}

		if (mode.is("Matrix") && airboost_matrix.get()) {
			if (mc.player.isOnGround()) {
				enabled = true;
			} else if (mc.player.fallDistance > 0f) {
				enabled = false;
			}

			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
				if (!motionboost_matrix.get() && !autoJump.get()) {
					mc.player.fallDistance = 0;
					move.setOnGround(true);
					mc.player.onGround = true;
				}
				if (enabled && !mc.player.movementInput.jump && autoJump.get()) mc.player.jump();
				mc.player.jumpMovementFactor = 0.026523f;
			}
		}
	}

	@Subscribe
	public void onMove(MovingEvent e) {
		if (mode.is("Matrix") && motionboost_matrix.get()) {
			if (!mc.player.isOnGround() && mc.player.fallDistance >= 0.5f && e.toGround) {
				double speed = 2;
				if (strafeMove.get()) {
					double[] newSpeed = MoveUtils.getSpeed((Math.hypot(mc.player.motion.x, mc.player.motion.z) - 0.0001) * speed);
					e.motion.x = newSpeed[0];
					e.motion.z = newSpeed[1];
					mc.player.motion.x = e.motion.x;
					mc.player.motion.z = e.motion.z;
					return;
				}
				mc.player.motion.x *= speed;
				mc.player.motion.z *= speed;
				strafe.setOldSpeed(speed);
			}
		}

		if (mode.is("Grim") && entityboost_grim.get()) {
			if (!svboost_grim.get()) {
				for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
					if (player != mc.player && mc.player.getDistance(player) <= 2.25) {
						float p = mc.world.getBlockState(new BlockPos(0, 0, 0)).getBlockId();
						float f = (mc.player.isOnGround() ? p * 0.91f : 0.91f);
						float f2 = 0.99f;
						mc.player.setVelocity(mc.player.getMotion().getX() / f * f2, mc.player.getMotion().getY(), mc.player.getMotion().getZ() / f * f2);
					}
				}
			} else {
				for (Entity ent : mc.world.getAllEntities()) {
					int collisions = 0;
					if (ent != mc.player && (ent instanceof LivingEntity || ent instanceof BoatEntity) && mc.player.getBoundingBox().expand(0, 1.0, 0).intersects(ent.getBoundingBox())) collisions++;
					double[] motion = MoveUtils.forward(0.08 * collisions);
					mc.player.addVelocity(motion[0], 0.0, motion[1]);
				}
			}
		}
	}

	public void placeBlock() {
		if (Rol.getInstance().getModuleManager().getHitAura().isState() && Rol.getInstance().getModuleManager().getHitAura().getTarget() != null) {
			return;
		}
		BlockPos blockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.6, mc.player.getPosZ());
		if (mc.world.getBlockState(blockPos).isAir()) {
			return;
		}
		int block = InventoryUtil.findBlockInHotbar();
		if (block == -1) {
			return;
		}
		mc.player.connection.sendPacket(new CHeldItemChangePacket(block));
		mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
		Vector3d blockCenter = new Vector3d((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5);
		mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(blockCenter, Direction.UP, blockPos, false)));
		mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
		mc.world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
		prevSlot = mc.player.inventory.currentItem;
	}

	public double getCurrentSpeed(int stage) {
		double speed = MoveUtils.getBaseSpeed() + 0.028 * (double) MoveUtils.getSpeedEffect() + (double) MoveUtils.getSpeedEffect() / 15.0;
		double initSpeed = 0.4145 + (double) MoveUtils.getSpeedEffect() / 12.5;
		double decrease = (double) stage / 500.0 * 1.87;
		if (stage == 0) {
			speed = 0.64 + ((double) MoveUtils.getSpeedEffect() + 0.028 * (double) MoveUtils.getSpeedEffect()) * 0.134;
		} else if (stage == 1) {
			speed = initSpeed;
		} else if (stage >= 2) {
			speed = initSpeed - decrease;
		}
		return Math.max(speed, this.slowDownHop ? speed : MoveUtils.getBaseSpeed() + 0.028 * (double) MoveUtils.getSpeedEffect());
	}
}
