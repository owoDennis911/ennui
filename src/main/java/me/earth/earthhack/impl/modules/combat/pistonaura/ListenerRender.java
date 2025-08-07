package me.earth.earthhack.impl.modules.combat.pistonaura;

import java.awt.Color;
import me.earth.earthhack.impl.event.events.render.Render3DEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.render.Interpolation;
import me.earth.earthhack.impl.util.render.mutables.BBRender;
import me.earth.earthhack.impl.util.render.mutables.MutableBB;
import net.minecraft.util.math.BlockPos;

public final class ListenerRender extends ModuleListener<PistonAura, Render3DEvent>
{
    private final StopWatch pistonSlideTimer = new StopWatch();
    private final StopWatch fireSlideTimer = new StopWatch();
    private final StopWatch crystalSlideTimer = new StopWatch();
    private final StopWatch redstoneSlideTimer = new StopWatch();

    private final MutableBB bb = new MutableBB();
    private BlockPos lastPistonPos = null;
    private BlockPos prevPistonPos = null;
    private BlockPos lastFirePos = null;
    private BlockPos prevFirePos = null;
    private BlockPos lastCrystalPos = null;
    private BlockPos prevCrystalPos = null;
    private BlockPos lastRedstonePos = null;
    private BlockPos prevRedstonePos = null;

    public ListenerRender(PistonAura module)
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

        BlockPos pistonPos = module.getPistonPos();
        BlockPos firePos = module.getFirePos();
        BlockPos crystalPos = module.getCrystalPos();
        BlockPos redstonePos = module.getRedstonePos();

        if (pistonPos != null && !pistonPos.equals(lastPistonPos)) {
            prevPistonPos = lastPistonPos;
            lastPistonPos = pistonPos;
            pistonSlideTimer.reset();
        }
        if (pistonPos != null) {
            renderBox(pistonPos, module.pistonColor.getValue(), module.pistonOutLine.getValue(),
                    pistonSlideTimer, module.pistonSlideTime.getValue(), 1.0f);
        }
        if (prevPistonPos != null && pistonSlideTimer.getTime() < module.fadeTime.getValue()) {
            renderBox(prevPistonPos, module.pistonColor.getValue(), module.pistonOutLine.getValue(),
                    pistonSlideTimer, module.fadeTime.getValue(), 1.0f, true);
        }

        if (firePos != null && !firePos.equals(lastFirePos)) {
            prevFirePos = lastFirePos;
            lastFirePos = firePos;
            fireSlideTimer.reset();
        }
        if (firePos != null) {
            renderBox(firePos, module.fireColor.getValue(), module.fireOutLine.getValue(),
                    fireSlideTimer, module.fireSlideTime.getValue(), module.fireBoxHeight.getValue());
        }
        if (prevFirePos != null && fireSlideTimer.getTime() < module.fadeTime.getValue()) {
            renderBox(prevFirePos, module.fireColor.getValue(), module.fireOutLine.getValue(),
                    fireSlideTimer, module.fadeTime.getValue(), module.fireBoxHeight.getValue(), true);
        }

        if (crystalPos != null && !crystalPos.equals(lastCrystalPos)) {
            prevCrystalPos = lastCrystalPos;
            lastCrystalPos = crystalPos;
            crystalSlideTimer.reset();
        }
        if (crystalPos != null) {
            renderBox(crystalPos, module.crystalColor.getValue(), module.crystalOutLine.getValue(),
                    crystalSlideTimer, module.crystalSlideTime.getValue(), module.crystalBoxHeight.getValue());
        }
        if (prevCrystalPos != null && crystalSlideTimer.getTime() < module.fadeTime.getValue()) {
            renderBox(prevCrystalPos, module.crystalColor.getValue(), module.crystalOutLine.getValue(),
                    crystalSlideTimer, module.fadeTime.getValue(), module.crystalBoxHeight.getValue(), true);
        }

        if (redstonePos != null && !redstonePos.equals(lastRedstonePos)) {
            prevRedstonePos = lastRedstonePos;
            lastRedstonePos = redstonePos;
            redstoneSlideTimer.reset();
        }
        if (redstonePos != null) {
            renderBox(redstonePos, module.redstoneColor.getValue(), module.redstoneOutLine.getValue(),
                    redstoneSlideTimer, module.redstoneSlideTime.getValue(), 1.0f);
        }
        if (prevRedstonePos != null && redstoneSlideTimer.getTime() < module.fadeTime.getValue()) {
            renderBox(prevRedstonePos, module.redstoneColor.getValue(), module.redstoneOutLine.getValue(),
                    redstoneSlideTimer, module.fadeTime.getValue(), 1.0f, true);
        }
    }

    private void renderBox(BlockPos pos, Color boxColor, Color outline, StopWatch timer, double slideTime, float boxHeight, boolean isFading)
    {
        bb.setFromBlockPos(pos);
        bb.maxY = bb.minY + boxHeight;
        Interpolation.interpolateMutable(bb);

        double progress = Math.min(1.0, timer.getTime() / slideTime);
        double startScale = isFading ? 1.0 : 1.1;
        double endScale = isFading ? 0.6 : 1.0;
        double scale = startScale + (endScale - startScale) * progress;

        double centerX = (bb.minX + bb.maxX) / 2.0;
        double centerZ = (bb.minZ + bb.maxZ) / 2.0;
        double halfWidth = (bb.maxX - bb.minX) / 2.0 * scale;
        double halfDepth = (bb.maxZ - bb.minZ) / 2.0 * scale;
        bb.minX = centerX - halfWidth;
        bb.maxX = centerX + halfWidth;
        bb.minZ = centerZ - halfDepth;
        bb.maxZ = centerZ + halfDepth;

        float alpha = isFading ? (float) (1.0 - progress) : 1.0f;
        Color finalBoxColor = new Color(
                boxColor.getRed() / 255.0f,
                boxColor.getGreen() / 255.0f,
                boxColor.getBlue() / 255.0f,
                boxColor.getAlpha() / 255.0f * alpha
        );
        Color finalOutlineColor = new Color(
                outline.getRed() / 255.0f,
                outline.getGreen() / 255.0f,
                outline.getBlue() / 255.0f,
                outline.getAlpha() / 255.0f * alpha
        );

        BBRender.renderBox(bb, finalBoxColor, finalOutlineColor, module.lineWidth.getValue());
    }

    private void renderBox(BlockPos pos, Color boxColor, Color outline, StopWatch timer, double slideTime, float boxHeight)
    {
        renderBox(pos, boxColor, outline, timer, slideTime, boxHeight, false);
    }
}