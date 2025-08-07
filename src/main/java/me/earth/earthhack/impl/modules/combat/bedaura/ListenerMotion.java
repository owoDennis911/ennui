package me.earth.earthhack.impl.modules.combat.bedaura;

import me.earth.earthhack.api.event.events.Stage;
import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.refill;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.DamageUtil;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import me.earth.earthhack.impl.util.minecraft.KeyBoardUtil;
import me.earth.earthhack.impl.util.minecraft.blocks.SpecialBlocks;
import me.earth.earthhack.impl.util.minecraft.entity.EntityUtil;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiInventory;
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

import static me.earth.earthhack.impl.util.minecraft.blocks.BlockUtil.getSpherePositions;

final class ListenerMotion extends ModuleListener<BedAura, MotionUpdateEvent> {
    private final StopWatch breakTimer = new StopWatch();
    private final StopWatch placeTimer = new StopWatch();
    private final StopWatch updateTimer = new StopWatch();

    private EntityPlayer target = null;
    private Candidate bestCandidate = null;
    private BlockPos placePos = null;
    private boolean slow = false;

    private static class Candidate {
        public final BlockPos feet;
        public final EnumFacing facing;
        public final float enemyDamage;
        public final float selfDamage;

        public Candidate(BlockPos feet, EnumFacing facing, float enemyDamage, float selfDamage) {
            this.feet = feet;
            this.facing = facing;
            this.enemyDamage = enemyDamage;
            this.selfDamage = selfDamage;
        }
    }

    public ListenerMotion(BedAura module) {
        super(module, MotionUpdateEvent.class, 1000);
    }

    public void invoke(MotionUpdateEvent event) {
        if (event.getStage() == Stage.PRE) {
            if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory) {
                bedToHotbar();
            }

            if (mc.player.dimension == 0) {
                bestCandidate = null;
                placePos = null;
                return;
            }
            if (!KeyBoardUtil.isKeyDown(module.selfBedBind.getValue())) {
                target = EntityUtil.getClosestEnemy(mc.world.playerEntities);
                if (target == null || mc.player.getDistance(target) > module.enemyRange.getValue()) {
                    bestCandidate = null;
                    placePos = null;
                    return;
                }
            } else {
                target = mc.player;
            }

            if (updateTimer.passed(module.updateDelay.getValue())) {
                update();
                updateTimer.reset();
            }

            if (bestCandidate != null) {
                event.setYaw(bestCandidate.facing.getHorizontalAngle());
                if (module.hardPitch.getValue()) {
                    event.setPitch(module.toPitch.getValue());
                }
            }
        } else if (event.getStage() == Stage.POST) {
            if (placePos != null && mc.player.getHealth() > module.noSuicide.getValue()) {
                placeAndBreakBed(placePos);
            }
        }
    }

    private void update() {
        Candidate best = null;
        float bestEnemyDamage = -1.0f;
        BlockPos center = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (BlockPos feetPos : getSpherePositions(center, module.placeRange.getValue(), module.yRange.getValue(), false)) {
            if (isPlaceable(feetPos)) {
                continue;
            }
            for (EnumFacing face : EnumFacing.HORIZONTALS) {
                BlockPos headPos = feetPos.offset(face);
                if (isPlaceable(headPos)) {
                    continue;
                }
                if (mc.player.getDistance(feetPos.getX(), feetPos.getY(), feetPos.getZ()) > module.placeRange.getValue()) {
                    continue;
                }

                double explosionX = headPos.getX() + 0.5;
                double explosionY = headPos.getY() + 0.5;
                double explosionZ = headPos.getZ() + 0.5;

                AxisAlignedBB targetBB = target.getEntityBoundingBox();
                AxisAlignedBB selfBB = mc.player.getEntityBoundingBox();

                float enemyDamage = DamageUtil.calculate(explosionX, explosionY, explosionZ, targetBB, target, true);
                float selfDamage = DamageUtil.calculate(explosionX, explosionY, explosionZ, selfBB, mc.player, true);

                if (!KeyBoardUtil.isKeyDown(module.selfBedBind.getValue())) {
                    if (selfDamage > module.maxSelfDamage.getValue() || enemyDamage < module.minEnemyDamage.getValue()) {
                        continue;
                    }
                } else if (selfDamage > module.selfBedMaxDmg.getValue() || enemyDamage < module.selfBedMinDmg.getValue()) {
                    continue;
                }

                if (enemyDamage > bestEnemyDamage) {
                    bestEnemyDamage = enemyDamage;
                    best = new Candidate(feetPos, face, enemyDamage, selfDamage);
                }
            }
        }
        bestCandidate = best;
        placePos = best != null ? best.feet : null;

        if (placePos != null) {
            slow = bestCandidate.enemyDamage < module.slowMinDmg.getValue();
            module.setFeetPos(bestCandidate.feet);
            module.setHeadPos(bestCandidate.feet.offset(bestCandidate.facing));
            module.setDamage(String.format("%.1f", bestCandidate.enemyDamage));
        }
    }

    private void bedToHotbar() {
        int bedSlot = module.bedSlot.getValue() - 1;
        int hotbarSlot = 36 + bedSlot;
        ItemStack currentStack = mc.player.openContainer.getSlot(hotbarSlot).getStack();
        if (!currentStack.isEmpty() && currentStack.getItem() == Items.BED) {
            return;
        }
        refill mode = module.refillMode.getValue();
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.openContainer.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == Items.BED) {
                switch (mode) {
                    case PickUp:
                        int slotIndex = i;
                        Locks.acquire(Locks.WINDOW_CLICK_LOCK, () -> {
                            if (mc.player.inventory.getItemStack().getItem() != Items.BED) {
                                InventoryUtil.click(slotIndex);
                                InventoryUtil.click(hotbarSlot);
                            } else {
                                InventoryUtil.click(hotbarSlot);
                            }
                        });
                        break;
                    case Swap:
                        mc.playerController.windowClick(0, i, bedSlot, ClickType.SWAP, mc.player);
                        break;
                    case QuickMove:
                        mc.playerController.windowClick(0, i, 0, ClickType.QUICK_MOVE, mc.player);
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }

    private void placeAndBreakBed(BlockPos feetPos) {
        if (feetPos == null || target == null) {
            return;
        }
        int bedSlot = findBedInHotbar();
        int placeDelay = slow ? module.slowPlaceDelay.getValue() : module.placeDelay.getValue();
        if (placeTimer.passed(placeDelay)) {
            if (bedSlot == -1 && mc.player.getHeldItemOffhand().getItem() == Items.BED) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                        feetPos.down(), EnumFacing.UP, EnumHand.OFF_HAND, 0.5f, 1.0f, 0.5f));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
            } else if (bedSlot != -1) {
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem;
                    InventoryUtil.switchTo(bedSlot);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                            feetPos.down(), EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
                    InventoryUtil.switchTo(lastSlot);
                });
            }
            placeTimer.reset();
        }
        if (module.dynDelay.getValue() && target.motionY != 0 && !KeyBoardUtil.isKeyDown(module.selfBedBind.getValue())) {
            if (breakTimer.passed(module.dynamicDelay.getValue())) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                        feetPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                breakTimer.reset();
            }
        } else {
            int breakDelay = slow ? module.slowBreakDelay.getValue() : module.breakDelay.getValue();
            if (breakTimer.passed(breakDelay)) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                        feetPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                breakTimer.reset();
            }
        }
    }

    private boolean isPlaceable(BlockPos pos) {
        Block blockAtPos = mc.world.getBlockState(pos).getBlock();
        Block blockBelowPos = mc.world.getBlockState(pos.down()).getBlock();
        return (blockAtPos != Blocks.AIR || SpecialBlocks.NO_BED_BLOCKS.contains(blockBelowPos))
                && (blockAtPos != Blocks.BED || SpecialBlocks.NO_BED_BLOCKS.contains(blockBelowPos));
    }

    private int findBedInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == Items.BED) {
                return i;
            }
        }
        return -1;
    }
}
