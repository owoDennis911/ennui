package me.earth.earthhack.impl.modules.dev.bedbaseplace;

import me.earth.earthhack.api.event.events.Stage;
import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.math.path.astar.AStarPathfinder;
import me.earth.earthhack.impl.util.minecraft.DamageUtil;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import me.earth.earthhack.impl.util.minecraft.blocks.SpecialBlocks;
import me.earth.earthhack.impl.util.minecraft.entity.EntityUtil;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static me.earth.earthhack.impl.util.minecraft.blocks.BlockUtil.getSpherePositions;

class ListenerMotion extends ModuleListener<BedBasePlace, MotionUpdateEvent> {

    private final StopWatch placeTimer = new StopWatch();
    private final StopWatch calcTimer = new StopWatch();
    private final StopWatch threadTimer = new StopWatch();
    private EntityPlayer target = null;

    private Candidate bestCandidate = null;
    private BlockPos mainPos = null;
    private List<BlockPos> supportBlocks = Collections.emptyList();
    private final AStarPathfinder pathfinder = new AStarPathfinder();
    private final AtomicReference<List<BlockPos>> asyncResult = new AtomicReference<>(Collections.emptyList());

    private static class Candidate {
        public final BlockPos pos;
        public final float score;

        public Candidate(BlockPos pos, float score) {
            this.pos = pos;
            this.score = score;
        }
    }

    public ListenerMotion(BedBasePlace module) {
        super(module, MotionUpdateEvent.class, 1000);
    }

    @Override
    public void invoke(MotionUpdateEvent event) {
        if (event.getStage() == Stage.PRE) {
            target = EntityUtil.getClosestEnemy(mc.world.playerEntities);
            if (target == null || mc.player.getDistance(target) > module.targetRange.getValue()) {
                return;
            }
        }
        if (event.getStage() == Stage.POST) {
            update();
        }
    }

    private void update() {
        try {
            if (module.async.getValue() && threadTimer.passed(module.calcDelay.getValue())) {
                List<BlockPos> result = asyncResult.get();
                if (result != null && !result.isEmpty()) {
                    supportBlocks = result;
                    asyncResult.set(Collections.emptyList());
                }
            }

            if (calcTimer.passed(module.calcDelay.getValue())) {
                BlockPos center = mc.player.getPosition();
                bestCandidate = null;

                for (BlockPos pos : getSpherePositions(center, module.placeRange.getValue(), module.placeRange.getValue(), false)) {
                    if (checkValid(pos)) {
                        calcDmg(pos);
                    }
                }

                if (bestCandidate != null) {
                    mainPos = bestCandidate.pos.down();
                    module.definePos(mainPos);
                    module.setDamage(String.format("%.1f", bestCandidate.score));

                    calculateSupportBlocks();
                }

                if (!supportBlocks.isEmpty()) {
                    placeSupportBlocks();
                }

                calcTimer.reset();
            }
        } catch (Exception e) {
            module.setDamage("Error: " + e.getMessage());
        }
    }

    private void calculateSupportBlocks() {
        execute(new PathCalculation(mainPos));
    }

    private void placeSupportBlocks() {
        int placed = 0;
        List<BlockPos> allPlacePositions = new ArrayList<>(supportBlocks);

        if (mainPos != null && mc.world.getBlockState(mainPos).getBlock() == Blocks.AIR) {
            allPlacePositions.add(mainPos);

            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos adjacent = mainPos.offset(facing);
                if (mc.world.getBlockState(adjacent).getBlock() == Blocks.AIR) {
                    allPlacePositions.add(adjacent);
                    break;
                }
            }
        }

        for (BlockPos pos : allPlacePositions) {
            if (placeTimer.passed(module.obbyDelay.getValue())) {
                if (placed >= module.blocks.getValue()) {
                    break;
                }

                EnumFacing facing = getFacing(pos);
                if (facing != null) {
                    placeObby(pos, facing);
                    placed++;
                }
                placeTimer.reset();
            }
        }
    }

    private class PathCalculation extends AbstractCalculation<List<BlockPos>> {
        private final BlockPos targetPos;
        private List<BlockPos> result = Collections.emptyList();

        public PathCalculation(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public void run() {
            try {
                result = pathfinder.findSupportPath(
                        targetPos,
                        module.placeRange.getValue(),
                        module.helpingDepth.getValue()
                );
            } catch (Exception e) {
                result = Collections.emptyList();
            }
        }

        @Override
        public List<BlockPos> getResult() {
            return result;
        }
    }

    private abstract static class AbstractCalculation<T> implements Runnable {
        public abstract T getResult();
    }

    private void execute(AbstractCalculation<List<BlockPos>> calculation) {
        if (module.async.getValue()) {
            Managers.THREAD.submitRunnable(() -> {
                try {
                    calculation.run();
                    asyncResult.set(calculation.getResult());
                } catch (Exception e) {
                    asyncResult.set(Collections.emptyList());
                }
            });

            threadTimer.reset();
        } else {
            try {
                calculation.run();
                supportBlocks = calculation.getResult();
            } catch (Exception e) {
                supportBlocks = Collections.emptyList();
            }
        }
    }

    private void calcDmg(BlockPos pos) {
        try {
            double boomX = pos.getX() + 0.5;
            double boomY = pos.getY() + 0.5;
            double boomZ = pos.getZ() + 0.5;
            AxisAlignedBB targetBB = target.getEntityBoundingBox();
            AxisAlignedBB selfBB = mc.player.getEntityBoundingBox();

            float enemyDamage = DamageUtil.calculate(boomX, boomY, boomZ, targetBB, target, true);
            float selfDamage = DamageUtil.calculate(boomX, boomY, boomZ, selfBB, mc.player, true);

            if (selfDamage >= module.maxSelfDamage.getValue() || enemyDamage <= module.minEnemyDamage.getValue()) {
                return;
            }

            float score = enemyDamage - selfDamage;

            if (bestCandidate == null || score > bestCandidate.score) {
                bestCandidate = new Candidate(pos, score);
            }
        } catch (Exception ignored) {
        }
    }

    private EnumFacing getFacing(BlockPos pos) {
        if (pos == null) return null;

        try {
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos offset = pos.offset(facing);
                Block support = mc.world.getBlockState(offset).getBlock();
                if (!SpecialBlocks.NO_BED_BLOCKS.contains(support)) {
                    return facing;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BlockPos checkCollision(BlockPos pos) {
        try {
            if (pos == null) return null;

            AxisAlignedBB blockBB = new AxisAlignedBB(pos);
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, blockBB)) {
                if (entity != null && !entity.isDead) {
                    return null;
                }
            }
            return pos;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkValid(BlockPos pos) {
        try {
            if (pos == null) return false;

            boolean hasAirAdjacent = false;

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos offset = pos.offset(facing);

                if (mc.world.getBlockState(offset).getBlock() == Blocks.AIR) {
                    hasAirAdjacent = true;
                }
            }

            return hasAirAdjacent && mc.world.getBlockState(pos).getBlock() == Blocks.AIR && checkCollision(pos) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void placeObby(BlockPos pos, EnumFacing facing) {
        try {
            if (pos == null || target == null || facing == null) {
                return;
            }

            boolean isOff = mc.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN);
            int obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);

            if (isOff || obbySlot != -1) {
                EnumHand hand = isOff ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem;
                    if (!isOff) {
                        InventoryUtil.switchTo(obbySlot);
                    }
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos.offset(facing), facing.getOpposite(), hand, 0.5f, 0.5f, 0.5f));
                    mc.player.connection.sendPacket(new CPacketAnimation(hand));
                    if (!isOff) {
                        InventoryUtil.switchTo(lastSlot);
                    }
                });
            }
            placeTimer.reset();
        } catch (Exception ignored) {
        }
    }
}