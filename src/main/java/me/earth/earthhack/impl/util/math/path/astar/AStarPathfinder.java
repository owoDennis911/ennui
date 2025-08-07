package me.earth.earthhack.impl.util.math.path.astar;

import me.earth.earthhack.impl.util.minecraft.blocks.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;

/**
 * A* pathfinding algorithm implementation to find optimal placement positions
 * for support blocks to reach a target position.
 */
public class AStarPathfinder {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Vec3i[] NEIGHBORS = Arrays.stream(EnumFacing.values())
            .map(EnumFacing::getDirectionVec)
            .toArray(Vec3i[]::new);

    static class Node implements Comparable<Node> {
        private final BlockPos pos;
        private final Node parent;
        private final double gCost; // Cost from  node
        private final double hCost; // Heuristic cost
        private final double fCost; // Total cost (g + h)

        public Node(BlockPos pos, Node parent, double gCost, double hCost) {
            this.pos = pos;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            if (this.fCost != other.fCost) {
                return Double.compare(this.fCost, other.fCost);
            }
            return Double.compare(this.hCost, other.hCost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return pos.equals(node.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }

    /**
     * Finds a path of valid positions to place support blocks to reach the target position.
     *
     * @param targetPos The target position to support with blocks
     * @param maxRange Maximum range from player to search
     * @param maxDepth Maximum number of blocks to place
     * @return A list of BlockPos for support blocks, in placement order
     */
    public List<BlockPos> findSupportPath(BlockPos targetPos, float maxRange, int maxDepth) {
        if (targetPos == null || mc.player == null || mc.world == null) {
            return Collections.emptyList();
        }

        List<BlockPos> validStartPositions = findValidStartPositions(targetPos, maxRange);
        if (validStartPositions.isEmpty()) {
            return Collections.emptyList();
        }

        List<BlockPos> bestPath = null;
        double bestCost = Double.MAX_VALUE;

        for (BlockPos startPos : validStartPositions) {
            List<BlockPos> path = findPath(startPos, targetPos, maxRange, maxDepth);

            if (!path.isEmpty()) {
                double pathCost = calculatePathCost(path);
                if (bestPath == null || pathCost < bestCost) {
                    bestPath = path;
                    bestCost = pathCost;
                }
            }
        }

        return bestPath != null ? bestPath : Collections.emptyList();
    }

    private double calculatePathCost(List<BlockPos> path) {
        if (path.isEmpty()) return Double.MAX_VALUE;

        double cost = path.size();

        BlockPos firstBlock = path.get(0);
        cost += mc.player.getDistance(
                firstBlock.getX() + 0.5,
                firstBlock.getY() + 0.5,
                firstBlock.getZ() + 0.5);

        return cost;
    }

    private List<BlockPos> findValidStartPositions(BlockPos targetPos, float maxRange) {
        BlockPos playerPos = new BlockPos(mc.player);
        List<BlockPos> validPositions = new ArrayList<>();

        for (BlockPos pos : BlockUtil.getSpherePositions(playerPos, maxRange, maxRange, false)) {
            if (isValidPlacementPosition(pos) && hasAdjacentSolid(pos) &&
                    !pos.equals(targetPos) && isWithinRange(pos, maxRange)) {
                validPositions.add(pos);
            }
        }

        validPositions.sort(Comparator.comparingDouble(pos ->
                pos.distanceSq(targetPos.getX(), targetPos.getY(), targetPos.getZ())));

        return validPositions;
    }

    private boolean hasAdjacentSolid(BlockPos pos) {
        for (Vec3i offset : NEIGHBORS) {
            BlockPos adjacent = pos.add(offset);
            Block block = mc.world.getBlockState(adjacent).getBlock();
            if (block != Blocks.AIR && isEntityBlocking(adjacent)) {
                return true;
            }
        }
        return false;
    }

    private List<BlockPos> findPath(BlockPos startPos, BlockPos targetPos, double maxRange, int maxDepth) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();

        Node startNode = new Node(startPos, null, 0, heuristic(startPos, targetPos));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (isAdjacentTo(current.pos, targetPos)) {
                return reconstructPath(current, maxDepth);
            }

            closedSet.add(current.pos);

            if (getPathDepth(current) >= maxDepth) {
                continue;
            }

            for (Vec3i offset : NEIGHBORS) {
                BlockPos neighborPos = current.pos.add(offset);

                if (closedSet.contains(neighborPos) || !isValidPlacementPosition(neighborPos) ||
                        !isWithinRange(neighborPos, maxRange) || neighborPos.equals(targetPos)) {
                    continue;
                }

                double gCost = current.gCost + 1;

                Node neighborNode = new Node(
                        neighborPos,
                        current,
                        gCost,
                        heuristic(neighborPos, targetPos)
                );

                boolean inOpenSet = false;
                for (Node openNode : openSet) {
                    if (openNode.pos.equals(neighborPos)) {
                        inOpenSet = true;
                        if (gCost < openNode.gCost) {
                            openSet.remove(openNode);
                            openSet.add(neighborNode);
                        }
                        break;
                    }
                }

                if (!inOpenSet) {
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean isAdjacentTo(BlockPos pos, BlockPos target) {
        for (Vec3i offset : NEIGHBORS) {
            if (pos.add(offset).equals(target)) {
                return true;
            }
        }
        return false;
    }

    private int getPathDepth(Node node) {
        int depth = 0;
        Node current = node;

        while (current.parent != null) {
            depth++;
            current = current.parent;
        }

        return depth;
    }


    private List<BlockPos> reconstructPath(Node endNode, int maxDepth) {
        List<BlockPos> path = new ArrayList<>();
        Node current = endNode;

        while (current.parent != null && path.size() < maxDepth) {
            path.add(current.pos);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    private double heuristic(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());

        return dx + dy * 1.5 + dz;
    }


    private boolean isWithinRange(BlockPos pos, double range) {
        return mc.player.getDistanceSq(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5) <= range * range;
    }

    private boolean isValidPlacementPosition(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR &&
                isEntityBlocking(pos);
    }


    private boolean isEntityBlocking(BlockPos pos) {
        return mc.world.getEntitiesWithinAABB(
                net.minecraft.entity.Entity.class,
                new net.minecraft.util.math.AxisAlignedBB(pos)
        ).isEmpty();
    }
}

