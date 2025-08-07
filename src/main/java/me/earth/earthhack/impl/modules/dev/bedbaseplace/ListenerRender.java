package me.earth.earthhack.impl.modules.dev.bedbaseplace;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.earth.earthhack.impl.event.events.render.Render3DEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.render.Interpolation;
import me.earth.earthhack.impl.util.render.mutables.BBRender;
import me.earth.earthhack.impl.util.render.mutables.MutableBB;
import net.minecraft.util.math.BlockPos;

public final class ListenerRender extends ModuleListener<BedBasePlace, Render3DEvent>
{
    private static class RenderBox {
        private final BlockPos pos;
        private final StopWatch timer;

        public RenderBox(BlockPos pos) {
            this.pos = pos;
            this.timer = new StopWatch();
            this.timer.reset();
        }

        public BlockPos getPos() {
            return pos;
        }

        public StopWatch getTimer() {
            return timer;
        }
    }

    private final List<RenderBox> renderBoxes = new ArrayList<>();
    private final MutableBB bb = new MutableBB();
    private BlockPos lastRenderedPos = null;

    public ListenerRender(BedBasePlace module)
    {
        super(module, Render3DEvent.class);
    }

    @Override
    public void invoke(Render3DEvent event)
    {
        if (!module.render.getValue())
        {
            renderBoxes.clear();
            lastRenderedPos = null;
            return;
        }

        BlockPos currentPos = module.getCurrentPos();

        if (currentPos != null) {
            boolean found = false;
            for (RenderBox box : renderBoxes) {
                if (box.getPos().equals(currentPos)) {
                    found = true;
                    break;
                }
            }
            if (!found && (lastRenderedPos == null || !lastRenderedPos.equals(currentPos))) {
                if (!renderBoxes.isEmpty()) {
                    renderBoxes.clear();
                }
                renderBoxes.add(new RenderBox(currentPos));
            }
        }

        Iterator<RenderBox> iterator = renderBoxes.iterator();
        while (iterator.hasNext()) {
            RenderBox box = iterator.next();
            double progress = Math.min(1.0, (double) box.getTimer().getTime() / module.zoomTime.getValue());

            if (progress >= 1.0) {
                lastRenderedPos = box.getPos();
                iterator.remove();
            } else {
                renderBox(box.getPos(), box.getTimer());
            }
        }
    }

    private void renderBox(BlockPos pos, StopWatch timer)
    {
        bb.setFromBlockPos(pos);
        bb.maxY = bb.minY + 1.0;
        Interpolation.interpolateMutable(bb);

        double progress = Math.min(1.0, (double) timer.getTime() / module.zoomTime.getValue());

        double startScale = 1.0 + module.zoomOffset.getValue();
        double scale = startScale + (1.0 - startScale) * progress;

        double centerX = (bb.minX + bb.maxX) / 2.0;
        double centerZ = (bb.minZ + bb.maxZ) / 2.0;
        double halfWidth = (bb.maxX - bb.minX) / 2.0 * scale;
        double halfDepth = (bb.maxZ - bb.minZ) / 2.0 * scale;

        bb.minX = centerX - halfWidth;
        bb.maxX = centerX + halfWidth;
        bb.minZ = centerZ - halfDepth;
        bb.maxZ = centerZ + halfDepth;

        Color boxColor = module.boxColor.getValue();
        if (module.fade.getValue()) {
            int alpha = boxColor.getAlpha();
            int newAlpha = (int)(alpha * (1.0 - progress));
            boxColor = new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), newAlpha);
        }

        BBRender.renderBox(bb, boxColor, module.outLine.getValue(), module.lineWidth.getValue());
    }
}