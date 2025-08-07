package me.earth.earthhack.impl.modules.combat.pistonaura;

import me.earth.earthhack.api.module.data.DefaultData;

final class PistonAuraData extends DefaultData<PistonAura>
{
    public PistonAuraData(PistonAura module)
    {
        super(module);
        register(module.range, "Range at which you can place blocks and detect target.");
        register(module.hit, "If you should attack crystal by hand too.");
        register(module.breakRange, "Range at which you can break crystal.");
        register(module.onlyInHole, "If pa should only work when target is in hole.");
        register(module.allowInventory, "Make you able to place even if item is in inventory.");
        register(module.fire, "Exploit a crystal technique to make crystal explode immediately when pushed.");

        register(module.placesPerTick, "Number of actions to place per tick.");
        register(module.cycleDelay, "Delay between each new cycle.");
        register(module.pistonDelay, "Delay between each piston action.");
        register(module.fireDelay, "Delay between each fire action.");
        register(module.crystalDelay, "Delay to wait before placing crystal.");
        register(module.redstoneDelay, "Delay to wait before triggering piston and pushing crystal.");
        register(module.hitDelay, "Delay to wait before hitting crystal if hit boolean is enabled.");

        register(module.render, "If it should render each block.");
        register(module.pistonColor, "Color of the piston.");
        register(module.pistonOutLine, "Color of pîston outline.");
        register(module.pistonSlideTime, "Time it takes for the piston to slide to another position.");
        register(module.fireColor, "Color of the fire.");
        register(module.fireOutLine, "Color of fire outline.");
        register(module.fireSlideTime, "Time it takes for the fire to slide to another position.");
        register(module.fireBoxHeight, "At which height the fire box should be.");
        register(module.crystalColor, "Color of the crystal.");
        register(module.crystalOutLine, "Color of the crystal outline.");
        register(module.crystalSlideTime, "Time it takes for the crystal to slide to another position.");
        register(module.redstoneColor, "Color of the redstone.");
        register(module.redstoneOutLine, "Color of the redstone outline.");
        register(module.redstoneSlideTime, "Time it takes for the redstone to slide to another position.");
        register(module.lineWidth, "Thickness of the outline of each box.");
        register(module.fadeTime, "Time it takes for each block to fade out.");
    }

    @Override
    public String getDescription()
    {
        return "Uses piston to push crystal into player head to deal damage.";
    }
}