package me.earth.earthhack.impl.modules.dev.bedbaseplace;

import me.earth.earthhack.api.module.data.DefaultData;

final class BedBasePlaceData extends DefaultData<BedBasePlace>
{
    public BedBasePlaceData(BedBasePlace module)
    {
        super(module);
        register(module.placeRange, "Range at which you can place blocks.");
        register(module.targetRange, "Range at which you search for targets.");
        register(module.maxSelfDamage, "Maximum damage you can deal to yourself.");
        register(module.minEnemyDamage, "Minimum damage you can deal to an enemy for the position to be valid.");
        register(module.async, "Run the calculations in a parallel thread.");

        register(module.blocks, "Number of blocks to place.");
        register(module.obbyDelay, "Delay between each block placement.");
        register(module.calcDelay, "Delay between each block calculation.");
        register(module.helpingDepth, "Defines how many helping blocks are allowed for optimal placement.");

        register(module.render, "Render the blocks.");
        register(module.fade, "Fade out the blocks.");
        register(module.zoomTime, "The time it takes to adjust from zoomOffset to 1.0.");
        register(module.zoomOffset, "The offset of the zoom block render.");
        register(module.boxColor, "The color of the box.");
        register(module.outLine, "The color of the outline.");
        register(module.lineWidth, "The width of the outline.");
    }

    @Override
    public String getDescription()
    {
        return "Place support blocks for your bed to deal maximum possible damage.";
    }
}