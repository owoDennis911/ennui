package me.earth.earthhack.impl.modules.combat.autoregear;

import java.awt.Color;
import me.earth.earthhack.impl.event.events.render.Render3DEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.render.Interpolation;
import me.earth.earthhack.impl.util.render.mutables.BBRender;
import me.earth.earthhack.impl.util.render.mutables.MutableBB;
import net.minecraft.util.math.BlockPos;

public final class ListenerRender extends ModuleListener<AutoRegear, Render3DEvent>
{
    private final StopWatch zoomTimer = new StopWatch();
    private final MutableBB bb = new MutableBB();
    private BlockPos lastPos = null;

    public ListenerRender(AutoRegear module)
    {
        super(module, Render3DEvent.class);
    }

    @Override
    public void invoke(Render3DEvent event)
    {
        if (!module.render.getValue())
        {
            return;
        }

        BlockPos pos = module.getShulkerPos();
        if (pos == null)
        {
            return;
        }

        if (lastPos == null || !lastPos.equals(pos))
        {
            zoomTimer.reset();
            lastPos = pos;
        }

        if (zoomTimer.getTime() >= module.zoomTime.getValue())
        {
            module.clearShulkerPos();
            lastPos = null;
            return;
        }

        renderShulkerZoom(pos);
    }

    private void renderShulkerZoom(BlockPos pos)
    {
        bb.setFromBlockPos(pos);
        bb.maxY = bb.minY + 1.0;
        Interpolation.interpolateMutable(bb);


        double progress = Math.min(1.0, (double) zoomTimer.getTime() / module.zoomTime.getValue());

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

        Color finalColor = module.boxColor.getValue();
        BBRender.renderBox(bb, finalColor, module.outLine.getValue(), module.lineWidth.getValue());
    }
}
