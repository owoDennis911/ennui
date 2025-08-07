package me.earth.earthhack.impl.modules.combat.pistonaura;

import me.earth.earthhack.api.event.events.Stage;
import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.modules.combat.pistonaura.helpers.patronHelper;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.DamageUtil;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import me.earth.earthhack.impl.util.minecraft.blocks.SpecialBlocks;
import me.earth.earthhack.impl.util.minecraft.entity.EntityUtil;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;

import static me.earth.earthhack.impl.util.minecraft.PlayerUtil.isInHole;

public final class ListenerMotion extends ModuleListener<PistonAura, MotionUpdateEvent> {

    EntityPlayer target = null;
    private BlockPos pistonPosCandidate = null;
    private EnumFacing pistonFacingCandidate = null;
    private BlockPos redstonePosCandidate = null;
    private BlockPos crystalPos = null;
    private BlockPos firePos = null;
    private int stage = -1; // -1 = No cycle, 0 = Piston, 1 = Fire, 2 = Crystal, 3 = Redstone, 4 = Hit

    private final StopWatch cycleTimer = new StopWatch();
    private final StopWatch pistonTimer = new StopWatch();
    private final StopWatch fireDelay = new StopWatch();
    private final StopWatch crystalDelay = new StopWatch();
    private final StopWatch redstoneDelay = new StopWatch();
    private final StopWatch hitDelay = new StopWatch();

    public ListenerMotion(PistonAura module) {
        super(module, MotionUpdateEvent.class);
    }

    @Override
    public void invoke(MotionUpdateEvent event) {
        if (event.getStage() == Stage.PRE) {
            target = EntityUtil.getClosestEnemy(mc.world.playerEntities);
            if (target == null || target.getDistance(mc.player) > module.range.getValue() || (module.onlyInHole.getValue() && !isInHole(target))) {
                module.setNumber(0, stage + "");
                stage = -1;
                return;
            }

            BlockPos targetHeadPos = new BlockPos(target.posX, target.posY + 1, target.posZ);

            if (stage == -1 && cycleTimer.passed(module.cycleDelay.getValue())) {
                List<Structure> structures = new patronHelper().structuresCandidate(targetHeadPos);
                for (Structure structure : structures) {
                    if (isValidStructure(structure)) {
                        pistonPosCandidate = structure.pistonPos;

                        float diffX = targetHeadPos.getX() - pistonPosCandidate.getX();
                        float diffY = targetHeadPos.getY() - pistonPosCandidate.getY();
                        float diffZ = targetHeadPos.getZ() - pistonPosCandidate.getZ();
                        pistonFacingCandidate = EnumFacing.getFacingFromVector(diffX, diffY, diffZ);

                        redstonePosCandidate = null;
                        for (BlockPos redstonePos : structure.redstonePos) {
                            if (isValidPosition(redstonePos) && findBase(redstonePos) != null && isPositionClear(redstonePos)) {
                                redstonePosCandidate = redstonePos;
                                break;
                            }
                        }
                        if (redstonePosCandidate != null) {
                            crystalPos = structure.crystalPos;
                            firePos = structure.firePos;
                            stage = 0;
                            pistonTimer.reset();
                            break;
                        }
                    }
                }
            }

            if (pistonFacingCandidate != null && stage != 4 || stage != 3) {
                event.setYaw(pistonFacingCandidate.getHorizontalAngle() - 180);
            }

            if (stage == 4 || stage == 3) {
                double yawX = targetHeadPos.getX() - crystalPos.getX();
                double yawZ = targetHeadPos.getZ() - crystalPos.getZ();
                double diffYForPitch = targetHeadPos.getY() - (crystalPos.getY() + 1.0);

                float yaw = (float)(Math.toDegrees(Math.atan2(yawZ, yawX)) - 90.0);

                float horizontalDist = (float)Math.sqrt(yawX * yawX + yawZ * yawZ);

                float pitch = (float)(-Math.toDegrees(Math.atan2(diffYForPitch, horizontalDist)));

                event.setYaw(yaw);
                event.setPitch(pitch);
            }
        }

        if (event.getStage() == Stage.POST) {
            if (stage >= 0) {
                int placesThisTick = 0;
                while (placesThisTick < module.placesPerTick.getValue() && stage >= 0) {
                    boolean actionTaken = false;
                    switch (stage) {
                        case 0:
                            if (pistonTimer.passed(module.pistonDelay.getValue())) {
                                if (doPiston()) {
                                    stage = module.fire.getValue() ? 1 : 2;
                                    if (module.fire.getValue()) fireDelay.reset(); else crystalDelay.reset();
                                    actionTaken = true;
                                } else {
                                    stage = -1;
                                }
                            }
                            break;
                        case 1:
                            if (fireDelay.passed(module.fireDelay.getValue())) {
                                if (doFire(firePos)) {
                                    stage = 2;
                                    crystalDelay.reset();
                                    actionTaken = true;
                                } else if (module.hit.getValue()) {
                                    stage = 2;
                                    crystalDelay.reset();
                                    actionTaken = true;
                                } else {
                                    stage = -1;
                                }
                            }
                            break;
                        case 2:
                            if (crystalDelay.passed(module.crystalDelay.getValue())) {
                                if (doCrystal()) {
                                    setDamage();
                                    stage = 3;
                                    redstoneDelay.reset();
                                    actionTaken = true;
                                } else {
                                    stage = -1;
                                }
                            }
                            break;
                        case 3:
                            if (redstoneDelay.passed(module.redstoneDelay.getValue())) {
                                if (doRedstone()) {
                                    stage = module.hit.getValue() ? 4 : -1;
                                    hitDelay.reset();
                                    actionTaken = true;
                                } else {
                                    stage = -1;
                                }
                            }
                            break;
                        case 4:
                            if (hitDelay.passed(module.hitDelay.getValue())) {
                                doHit();
                                stage = -1;
                                cycleTimer.reset();
                                actionTaken = true;
                            }
                            break;
                    }
                    if (actionTaken) placesThisTick++;
                    else break;
                }
            }
        }
    }

    private int findSlot(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            if (predicate.test(mc.player.inventory.getStackInSlot(i))) {
                return 36 + i;
            }
        }
        if (module.allowInventory.getValue()) {
            for (int i = 9; i <= 35; i++) {
                if (predicate.test(mc.player.inventory.mainInventory.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean performAction(Predicate<ItemStack> predicate, BlockPos pos, EnumFacing face, float hitY) {
        int slot = findSlot(predicate);
        if (slot == -1) return false;
        int lastSlot = mc.player.inventory.currentItem;
        if (slot >= 36 && slot <= 44) {
            int hotbarSlot = slot - 36;
            Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                InventoryUtil.switchTo(hotbarSlot);
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, face, EnumHand.MAIN_HAND, 0.5f, hitY, 0.5f));
                InventoryUtil.switchTo(lastSlot);
            });
        } else {
            Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                mc.playerController.windowClick(0, slot, lastSlot, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, face, EnumHand.MAIN_HAND, 0.5f, hitY, 0.5f));
                mc.playerController.windowClick(0, slot, lastSlot, ClickType.SWAP, mc.player);
            });
        }
        return true;
    }

    private boolean doPiston() {
        PlacementBase base = findBase(pistonPosCandidate);
        if (base == null) return false;
        module.setPistonPos(pistonPosCandidate);
        return performAction(stack -> Block.getBlockFromItem(stack.getItem()) == Blocks.PISTON, base.pos, base.face, 0.5f);
    }

    private boolean doRedstone() {
        PlacementBase base = findBase(redstonePosCandidate);
        if (base == null) return false;
        module.setRedstonePos(redstonePosCandidate);
        return performAction(stack -> Block.getBlockFromItem(stack.getItem()) == Blocks.REDSTONE_BLOCK, base.pos, base.face, 0.5f);
    }
    private boolean doCrystal() {
        module.setCrystalPos(crystalPos);
        return performAction(stack -> stack.getItem() == Items.END_CRYSTAL, crystalPos.down(), EnumFacing.UP, 1.0f);
    }

    private boolean doFire(BlockPos pos) {
        pos = pos.down();
        module.setFirePos(pos);
        return performAction(stack -> stack.getItem() == Items.FLINT_AND_STEEL, pos, EnumFacing.UP, 1.0f);
    }

    private void doHit() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal && entity.getDistance(mc.player) <= module.breakRange.getValue()) {
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                break;
            }
        }
    }

    private void setDamage() {
        crystalPos = crystalPos.offset(pistonFacingCandidate);

        double explosionX = crystalPos.getX() + 0.5;
        double explosionY = crystalPos.getY() + 0.5;
        double explosionZ = crystalPos.getZ() + 0.5;

        AxisAlignedBB targetBB = target.getEntityBoundingBox();
        AxisAlignedBB selfBB = mc.player.getEntityBoundingBox();

        float enemyDamage = DamageUtil.calculate(explosionX, explosionY, explosionZ, targetBB, target, true);
        float selfDamage = DamageUtil.calculate(explosionX, explosionY, explosionZ, selfBB, mc.player, true);
        module.setNumber(1, (int) enemyDamage + "/" + (int) selfDamage);
    }


    private PlacementBase findBase(BlockPos pos) {
        if (pos == null) return null;
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos basePos = pos.offset(face);
            if (!mc.world.getBlockState(basePos).getBlock().isReplaceable(mc.world, basePos)) {
                return new PlacementBase(basePos, face.getOpposite());
            }
        }
        return null;
    }

    private boolean isValidStructure(Structure structure) {
        if (!isValidPosition(structure.pistonPos) || findBase(structure.pistonPos) == null || !isPositionClear(structure.pistonPos)) return false;

        if (mc.world.getBlockState(structure.crystalPos).getBlock() != Blocks.AIR || !isPositionClear(structure.crystalPos)) return false;

        Block blockBelowCrystal = mc.world.getBlockState(structure.crystalPos.down()).getBlock();
        Block blockAboveCrystal = mc.world.getBlockState(structure.crystalPos.up()).getBlock();
        if (blockBelowCrystal != Blocks.OBSIDIAN && blockBelowCrystal != Blocks.BEDROCK) return false;
        if (blockAboveCrystal != Blocks.AIR) return false;

        return (mc.world.getBlockState(structure.firePos.down()).getBlock() != SpecialBlocks.NO_BED_BLOCKS);
    }

    private boolean isValidPosition(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos);
    }

    private boolean isPositionClear(BlockPos pos) {
        AxisAlignedBB box = new AxisAlignedBB(pos.getX(),pos.getY(),pos.getZ(),pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, box).isEmpty();
    }

    public static class Structure {
        public final BlockPos pistonPos;
        public final BlockPos firePos;
        public final BlockPos crystalPos;
        public final BlockPos[] redstonePos;

        public Structure(BlockPos pistonPos, BlockPos firePos, BlockPos crystalPos, BlockPos[] redstonePos) {
            this.pistonPos = pistonPos;
            this.firePos = firePos;
            this.crystalPos = crystalPos;
            this.redstonePos = redstonePos;
        }
    }

    public static class PlacementBase {
        public final BlockPos pos;
        public final EnumFacing face;

        public PlacementBase(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
        }
    }
}
