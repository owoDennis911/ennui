package me.earth.earthhack.impl.modules.combat.autoregear;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BindSetting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.ColorSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.api.util.bind.Bind;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
import me.earth.earthhack.impl.util.minecraft.entity.EntityUtil;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import me.earth.earthhack.impl.util.minecraft.blocks.BlockUtil;

public class AutoRegear extends Module {

    // Settings
    protected final Setting<Bind> placeShulkerKey = register(new BindSetting("regearKey", Bind.none()));
    protected final Setting<Float> placeRange = register(new NumberSetting<>("xzPlaceRange", 4.0f, 1.0f, 6.0f));
    protected final Setting<Float> yPlaceRange = register(new NumberSetting<>("yPlaceRange", 4.0f, 1.0f, 6.0f));
    protected final Setting<Float> miningRange = register(new NumberSetting<>("enemyMiningRange", 4.0f, 1.0f, 6.0f));
    protected final Setting<Float> MinDistance = register(new NumberSetting<>("MinDistance", 4.0f, 1.0f, 6.0f));


    protected final Setting<Boolean> render = register(new BooleanSetting("Render", true));
    protected final Setting<Integer> zoomTime = register(new NumberSetting<>("ZoomTime", 500, 100, 5000));
    protected final Setting<Double> zoomOffset = register(new NumberSetting<>("ZoomOffset", 0.5, -1.0, 1.0));
    protected final Setting<Color> boxColor = register(new ColorSetting("BoxColor", new Color(255, 255, 255, 255)));
    protected final Setting<Color> outLine = register(new ColorSetting("outlineColor", new Color(255, 255, 255, 255)));
    protected final Setting<Float> lineWidth = register(new NumberSetting<>("LineWidth", 1.0f, 0.1f, 5.0f));


    protected BlockPos shulkerPos = null;
    protected final StopWatch cooldownTimer = new StopWatch();


    public AutoRegear() {
        super("AutoRegear", Category.Combat);
        this.listeners.add(new ListenerKeyPress(this));
        this.listeners.add(new ListenerRender(this));
    }


    protected void attemptRegear() {
        BlockPos playerPos = mc.player.getPosition();
        List<BlockPos> sphere = BlockUtil.getSpherePositions(playerPos, placeRange.getValue(), yPlaceRange.getValue(), false);
        List<PlacementCandidate> candidates = new ArrayList<>();
        EntityPlayer enemy = EntityUtil.getClosestEnemy(mc.world.playerEntities);
        for (BlockPos basePos : sphere) {
            if (mc.world.getBlockState(basePos).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            for (EnumFacing face : EnumFacing.VALUES) {
                BlockPos placementPos = basePos.offset(face);
                if (!mc.world.getBlockState(placementPos).getBlock().equals(Blocks.AIR)) {
                    continue;
                }
                if (!mc.world.getBlockState(placementPos.offset(face)).getBlock().equals(Blocks.AIR)) {
                    continue;
                }
                if (placementPos.getY() == -1) {
                    continue;
                }
                double ownDistance;
                ownDistance = mc.player.getDistance(placementPos.getX(), placementPos.getY(), placementPos.getZ());
                if (ownDistance < MinDistance.getValue()) {
                    continue;
                }

                double distance = Double.MAX_VALUE;
                boolean enemySafe = true;
                if (enemy != null) {
                    distance = enemy.getDistance(placementPos.getX(), placementPos.getY(), placementPos.getZ());
                    enemySafe = distance >= miningRange.getValue();
                }
                candidates.add(new PlacementCandidate(basePos, face, placementPos, distance, enemySafe));
            }
        }

        PlacementCandidate bestCandidate = null;
        List<PlacementCandidate> safeCandidates = candidates.stream()
                .filter(c -> c.enemySafe)
                .collect(Collectors.toList());
        if (!safeCandidates.isEmpty()) {
            bestCandidate = safeCandidates.stream()
                    .max(Comparator.comparingDouble(c -> c.enemyDistance))
                    .orElse(null);
        } else if (!candidates.isEmpty()) {
            bestCandidate = candidates.stream()
                    .max(Comparator.comparingDouble(c -> c.enemyDistance))
                    .orElse(null);
        }

        if (bestCandidate != null) {
            placeShulker(bestCandidate.basePos, bestCandidate.placementPos, bestCandidate.face);
        }
    }

    private void placeShulker(BlockPos basePos, BlockPos shulkerPos, EnumFacing face) {
        this.shulkerPos = shulkerPos;
        int shulkerSlot = InventoryUtil.findInInventory(stack -> stack.getItem() instanceof ItemShulkerBox, false);
        if (shulkerSlot != -1) {
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem;
                    mc.playerController.windowClick(0, shulkerSlot, lastSlot, ClickType.SWAP, mc.player);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                            basePos, face, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
                    mc.playerController.windowClick(0, shulkerSlot, lastSlot, ClickType.SWAP, mc.player);
                });
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                        shulkerPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
        }
    }



    private static class PlacementCandidate {
        private final BlockPos basePos;
        private final EnumFacing face;
        private final BlockPos placementPos;
        private final double enemyDistance;
        private final boolean enemySafe;

        protected PlacementCandidate(BlockPos basePos, EnumFacing face, BlockPos placementPos,
                                  double enemyDistance, boolean enemySafe) {
            this.basePos = basePos;
            this.face = face;
            this.placementPos = placementPos;
            this.enemyDistance = enemyDistance;
            this.enemySafe = enemySafe;
        }
    }

    protected BlockPos getShulkerPos() {
        return shulkerPos;
    }

    protected void clearShulkerPos() {
        this.shulkerPos = null;
    }
}