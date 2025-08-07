package me.earth.earthhack.impl.modules.combat.pistonaura.helpers;

import me.earth.earthhack.impl.modules.combat.pistonaura.ListenerMotion;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static me.earth.earthhack.api.util.interfaces.Globals.mc;

public class patronHelper {
    public List<ListenerMotion.Structure> structuresCandidate(BlockPos pos) {
        ArrayList<ListenerMotion.Structure> structures = new ArrayList<>();

        /*
        pistonPos
        firePos
        crystalPos
        redstonePos[]
         */

        //Lines
        structures.add(new ListenerMotion.Structure(pos.west(2), pos.east(), pos.west(), new BlockPos[]{pos.west(3)}));
        structures.add(new ListenerMotion.Structure(pos.east(2), pos.west(), pos.east(), new BlockPos[]{pos.east(3)}));
        structures.add(new ListenerMotion.Structure(pos.north(2), pos.south(), pos.north(), new BlockPos[]{pos.north(3)}));
        structures.add(new ListenerMotion.Structure(pos.south(2), pos.north(), pos.south(), new BlockPos[]{pos.south(3)}));


        //Small 1*1 around target
        structures.add(new ListenerMotion.Structure(pos.north(), pos.south(), pos.west(),  new BlockPos[]{pos.north().east()}));
        structures.add(new ListenerMotion.Structure(pos.north(), pos.south(), pos.east(),  new BlockPos[]{pos.north().west()}));
        structures.add(new ListenerMotion.Structure(pos.south(), pos.north(), pos.west(),  new BlockPos[]{pos.south().east()}));
        structures.add(new ListenerMotion.Structure(pos.south(), pos.north(), pos.east(),  new BlockPos[]{pos.south().west()}));

        structures.add(new ListenerMotion.Structure(pos.east(), pos.west(), pos.north(),  new BlockPos[]{pos.east().south()}));
        structures.add(new ListenerMotion.Structure(pos.east(), pos.west(), pos.south(),  new BlockPos[]{pos.east().north()}));
        structures.add(new ListenerMotion.Structure(pos.west(), pos.east(), pos.north(), new BlockPos[]{ pos.west().south()}));
        structures.add(new ListenerMotion.Structure(pos.west(), pos.east(), pos.south(), new BlockPos[]{ pos.west().north()}));

        return structures;
    }

    private BlockPos[] findAvailableFacing(BlockPos pos, EnumFacing excludedFacing) {
        for(EnumFacing facing : EnumFacing.values()) {
            if(facing != excludedFacing && isAir(pos, facing)) {
                pos = pos.offset(facing);
            }
        }
        return new BlockPos[]{pos};
    }

    private boolean isAir(BlockPos pos,EnumFacing facing) {
        return mc.world.getBlockState(pos.offset(facing)).getBlock() == Blocks.AIR;
    }
}

