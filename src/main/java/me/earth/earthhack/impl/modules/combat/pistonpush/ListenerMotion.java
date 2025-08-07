package me.earth.earthhack.impl.modules.combat.pistonpush;

import java.util.List;
import me.earth.earthhack.api.event.events.Stage;
import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import me.earth.earthhack.impl.util.minecraft.entity.EntityUtil;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public final class ListenerMotion extends ModuleListener<PistonPush, MotionUpdateEvent> {
    private BlockPos pistonPosCandidate = null;
    private EnumFacing pistonFacingCandidate = null;
    private BlockPos redstonePosCandidate = null;
    private EnumFacing redstoneFacing = null;

    private final StopWatch placeTimer = new StopWatch();

    public ListenerMotion(PistonPush module) {
        super(module, MotionUpdateEvent.class);
    }

    @Override
    public void invoke(MotionUpdateEvent event) {
        if (event.getStage() == Stage.PRE) {
            pistonPosCandidate = null;
            pistonFacingCandidate = null;
            redstonePosCandidate = null;

            EntityPlayer enemy = EntityUtil.getClosestEnemy(mc.world.playerEntities);
            if (enemy == null) {
                return;
            }
            if (enemy.getDistance(mc.player) > module.enemyRange.getValue()) {
                return;
            }
            BlockPos enemyHeadPos = new BlockPos(enemy.posX, enemy.posY + 1, enemy.posZ);

            for (EnumFacing face : EnumFacing.HORIZONTALS) {
                BlockPos candidate = enemyHeadPos.offset(face);
                IBlockState state = mc.world.getBlockState(candidate);
                if (state.getBlock() == Blocks.AIR && isAreaClear(candidate)) {
                    pistonPosCandidate = candidate;
                    float diffX = enemyHeadPos.getX() - candidate.getX();
                    float diffY = enemyHeadPos.getY() - candidate.getY();
                    float diffZ = enemyHeadPos.getZ() - candidate.getZ();
                    pistonFacingCandidate = EnumFacing.getFacingFromVector(diffX, diffY, diffZ);
                    break;
                }
            }

            if (pistonPosCandidate != null && pistonFacingCandidate != null) {
                event.setYaw(pistonFacingCandidate.getHorizontalAngle() - 180);
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (face == pistonFacingCandidate) {
                        continue;
                    }
                    BlockPos candidate = pistonPosCandidate.offset(face);
                    IBlockState state = mc.world.getBlockState(candidate);
                    if (state.getBlock() == Blocks.AIR && isAreaClear(candidate)) {
                        redstonePosCandidate = candidate;
                        redstoneFacing = face;
                        break;
                    }
                }
            }
        } else if (event.getStage() == Stage.POST) {
            if (!placeTimer.passed(module.placeDelay.getValue())) {
                return;
            }
            if (pistonPosCandidate != null && pistonFacingCandidate != null && redstonePosCandidate != null) {
                place();
            }
        }
    }

    private boolean isAreaClear(BlockPos pos) {
        AxisAlignedBB box = new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );
        List<Entity> entities = mc.world.getEntitiesWithinAABB(Entity.class, box);
        return entities.isEmpty();
    }

    private void place() {
        if (pistonPosCandidate != null && pistonFacingCandidate != null) {
            final PlacementBase pistonBase = findBase(pistonPosCandidate);
            if (pistonBase != null && redstonePosCandidate != null) {
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem;
                    int pistonSlot = InventoryUtil.findHotbarBlock(Blocks.PISTON);
                    if (pistonSlot == -1) {
                        return;
                    }
                    InventoryUtil.switchTo(pistonSlot);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                            pistonBase.basePos, pistonBase.hitFace, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                    InventoryUtil.switchTo(lastSlot);
                });
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem;
                    int redstoneSlot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
                    if (redstoneSlot == -1) {
                        return;
                    }
                    InventoryUtil.switchTo(redstoneSlot);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                            pistonPosCandidate, redstoneFacing, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                    InventoryUtil.switchTo(lastSlot);
                });
                placeTimer.reset();
            }
        }
    }

    private PlacementBase findBase(BlockPos poor) {
        if (poor != null) {
            for (EnumFacing face : EnumFacing.values()) {
                BlockPos brain = poor.offset(face);
                IBlockState state = mc.world.getBlockState(brain);
                if (state.getBlock() != Blocks.AIR) {
                    return new PlacementBase(brain, face.getOpposite());
                }
            }
        }
        return null;
    }

    private static class PlacementBase {
        public final BlockPos basePos;
        public final EnumFacing hitFace;

        public PlacementBase(BlockPos basePos, EnumFacing hitFace) {
            this.basePos = basePos;
            this.hitFace = hitFace;
        }
    }
}
