package me.earth.earthhack.impl.modules.dev.bouncebegone;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.event.events.Stage;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BounceBeGone extends Module {
    private BlockPos lastBedPos = null;

    public final Setting<Boolean> breakBeds =
            register(new BooleanSetting("breakBedsOnFeet", true));

    public BounceBeGone() {
        super("BounceBeGone", Category.Dev);

        this.listeners.add(new ModuleListener<BounceBeGone, MotionUpdateEvent>(this, MotionUpdateEvent.class) {
            @Override
            public void invoke(MotionUpdateEvent event) {
                if (event.getStage() != Stage.PRE)
                    return;

                if (mc.player == null || mc.world == null)
                    return;

                World world = mc.world;
                EntityPlayerSP player = mc.player;
                BlockPos currentPos = new BlockPos(player.posX, player.posY, player.posZ);
                Block block = world.getBlockState(currentPos).getBlock();


                if (breakBeds.getValue() && block instanceof BlockBed) {
                    if (lastBedPos == null || !lastBedPos.equals(currentPos)) {
                        interactWithBed(world, currentPos);
                        lastBedPos = currentPos;
                    }
                } else {
                    lastBedPos = null;
                }
            }
        });
    }

    private void interactWithBed(World world, BlockPos pos) {
        if (!(world.getBlockState(pos).getBlock() instanceof BlockBed))
            return;
        mc.playerController.clickBlock(pos, EnumFacing.UP);
    }
}
