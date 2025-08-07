package me.earth.earthhack.impl.modules.combat.bedaura;

import java.awt.Color;
import me.earth.earthhack.impl.event.events.render.Render3DEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.managers.render.TextRenderer;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.renderAnim;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.renderM;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.render.Interpolation;
import me.earth.earthhack.impl.util.render.mutables.BBRender;
import me.earth.earthhack.impl.util.render.mutables.MutableBB;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.BlockPos;

public final class ListenerRender extends ModuleListener<BedAura, Render3DEvent>
{
    private final MutableBB bb = new MutableBB();
    private final StopWatch slideTimer = new StopWatch();

    private BlockPos lastFeetPos = null;
    private BlockPos lastHeadPos = null;
    private BlockPos prevFeetPos = null;
    private BlockPos prevHeadPos = null;

    public ListenerRender(BedAura module)
    {
        super(module, Render3DEvent.class);
    }

    @Override
    public void invoke(Render3DEvent event)
    {
        if (module.render.getValue())
        {
            BlockPos feetPos = module.getFeetPos();
            BlockPos headPos = module.getHeadPos();

            if (feetPos != null && headPos != null)
            {
                if (module.renderAnimation.getValue() == renderAnim.Slide)
                {
                    if (!feetPos.equals(lastFeetPos))
                    {
                        prevFeetPos = (lastFeetPos != null) ? lastFeetPos : feetPos;
                        lastFeetPos = feetPos;
                        slideTimer.reset();
                    }
                    if (!headPos.equals(lastHeadPos))
                    {
                        prevHeadPos = (lastHeadPos != null) ? lastHeadPos : headPos;
                        lastHeadPos = headPos;
                        slideTimer.reset();
                    }
                }
                else
                {
                    if (!feetPos.equals(lastFeetPos) || !headPos.equals(lastHeadPos))
                    {
                        lastFeetPos = feetPos;
                        lastHeadPos = headPos;
                    }
                }
                renderBedPlacement();
            }

            if (module.renderDamage.getValue())
            {
                double bedX, bedY, bedZ;
                if (feetPos != null && headPos != null)
                {
                    if (module.renderMode.getValue() == renderM.Bed)
                    {
                        bedX = (feetPos.getX() + headPos.getX() + 1) / 2.0;
                        bedY = feetPos.getY();
                        bedZ = (feetPos.getZ() + headPos.getZ() + 1) / 2.0;
                    }
                    else
                    {
                        bedX = feetPos.getX() + 0.5;
                        bedY = feetPos.getY();
                        bedZ = feetPos.getZ() + 0.5;
                    }
                    renderDamage(bedX, bedY, bedZ);
                }
            }
        }
    }

    private void renderBedPlacement()
    {
        BlockPos feetPos = module.getFeetPos();
        BlockPos headPos = module.getHeadPos();

        if (module.renderMode.getValue() == renderM.Block)
        {
            if (module.renderHead.getValue() && headPos != null)
            {
                renderSeparateBlock(headPos);
            }
            if (module.renderFeet.getValue() && feetPos != null)
            {
                renderSeparateBlock(feetPos);
            }
        }
        else if (module.renderMode.getValue() == renderM.Bed)
        {
            renderBedBlock();
        }
    }

    private void renderSeparateBlock(BlockPos pos)
    {
        if (pos == null || module == null || mc.player == null)
        {
            return;
        }

        else if (module.renderAnimation.getValue() == renderAnim.Slide)
        {
            BlockPos startPos = pos;
            if (pos.equals(lastFeetPos))
            {
                startPos = (prevFeetPos != null) ? prevFeetPos : pos;
            }
            else if (pos.equals(lastHeadPos))
            {
                startPos = (prevHeadPos != null) ? prevHeadPos : pos;
            }
            double factor = Math.min(1.0, slideTimer.getTime() / Math.max(1.0, module.slideTime.getValue()));
            double x = startPos.getX() + (pos.getX() - startPos.getX()) * factor;
            double y = startPos.getY() + (pos.getY() - startPos.getY()) * factor;
            double z = startPos.getZ() + (pos.getZ() - startPos.getZ()) * factor;
            bb.setBB(x, y, z, x + 1, y + 1, z + 1);
            Interpolation.interpolateMutable(bb);
        }
        else
        {
            bb.setFromBlockPos(pos);
            bb.maxY = bb.minY + module.boxHeight.getValue();
            Interpolation.interpolateMutable(bb);
        }

        Color baseColor = module.boxColor.getValue();
        Color finalColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha());
        BBRender.renderBox(bb, finalColor, module.outLine.getValue(), module.lineWidth.getValue());
    }

    private void renderBedBlock() {
        BlockPos feetPos = module.getFeetPos();
        BlockPos headPos = module.getHeadPos();
        if (feetPos == null || headPos == null) {
            return;
        }
        int minX = Math.min(feetPos.getX(), headPos.getX());
        int minY = feetPos.getY();
        int minZ = Math.min(feetPos.getZ(), headPos.getZ());
        int maxX = Math.max(feetPos.getX(), headPos.getX()) + 1;
        int maxZ = Math.max(feetPos.getZ(), headPos.getZ()) + 1;

        bb.setBB(minX, minY, minZ, maxX, minY + module.boxHeight.getValue(), maxZ);


        if (module.renderAnimation.getValue() == renderAnim.Slide) {
            if (prevFeetPos != null && prevHeadPos != null) {
                int prevMinX = Math.min(prevFeetPos.getX(), prevHeadPos.getX());
                int prevMinZ = Math.min(prevFeetPos.getZ(), prevHeadPos.getZ());
                int prevMaxX = Math.max(prevFeetPos.getX(), prevHeadPos.getX()) + 1;
                int prevMaxZ = Math.max(prevFeetPos.getZ(), prevHeadPos.getZ()) + 1;
                int prevMinY = prevFeetPos.getY();
                float prevMaxY = prevMinY + module.boxHeight.getValue();

                double factor = Math.min(1.0, slideTimer.getTime() / Math.max(1.0, module.slideTime.getValue()));
                double iMinX = prevMinX + (minX - prevMinX) * factor;
                double iMinZ = prevMinZ + (minZ - prevMinZ) * factor;
                double iMaxX = prevMaxX + (maxX - prevMaxX) * factor;
                double iMaxZ = prevMaxZ + (maxZ - prevMaxZ) * factor;
                double iMinY = prevMinY + (minY - prevMinY) * factor;
                double iMaxY = prevMaxY + ((minY + module.boxHeight.getValue()) - prevMaxY) * factor;

                bb.setBB(iMinX, iMinY, iMinZ, iMaxX, iMaxY, iMaxZ);
                Interpolation.interpolateMutable(bb);
            } else {
                Interpolation.interpolateMutable(bb);
            }

            Color baseColor = module.boxColor.getValue();
            Color finalColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), baseColor.getAlpha());
            BBRender.renderBox(bb, finalColor, module.outLine.getValue(), module.lineWidth.getValue());
        }
    }

    private void renderDamage(double x, double yIn, double z)
    {
        double xPos = x + module.damageX.getValue();
        double yPos = yIn + module.damageY.getValue();
        double zPos = z + module.damageZ.getValue();
        String damage = module.getDamage();

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        float scale = 0.016666668f * 1.3f;
        GlStateManager.translate(xPos - Interpolation.getRenderPosX(),
                yPos - Interpolation.getRenderPosY(),
                zPos - Interpolation.getRenderPosZ());

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.player.rotationPitch,
                mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f,
                0.0f,
                0.0f);
        GlStateManager.scale(-scale, -scale, scale);

        int distance = (int) mc.player.getDistance(xPos, yPos, zPos);
        float scaleD = (distance / 2.0f) / (2.0f + (2.0f - 1));
        if (scaleD < 1.0f)
        {
            scaleD = 1;
        }
        GlStateManager.scale(scaleD, scaleD, scaleD);

        TextRenderer m = Managers.TEXT;
        GlStateManager.translate(-(m.getStringWidth(damage) / 2.0), 0, 0);
        mc.fontRenderer.drawStringWithShadow(damage, 0, 0, module.damageColor.getValue().getRGB());

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
