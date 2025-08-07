package me.earth.earthhack.impl.modules.combat.bedaura;

import me.earth.earthhack.api.module.data.DefaultData;

final class BedAuraData extends DefaultData<BedAura>
{
    public BedAuraData(BedAura module)
    {
        super(module);
        register(module.placeDelay, "How much to wait before placing the bed.");
        register(module.breakDelay, "How much to wait before exploding the bed.");
        register(module.dynDelay, "Toggle the use of dynamicDelay.");
        register(module.dynamicDelay, "New break delay if player is moving vertically.");
        register(module.updateDelay, "Delay before performing a new calculation to find the optimal damage from a position.");

        register(module.bedSlot, "The slot of the bed to be refilled.");
        register(module.refillMode, "Swap: Swaps item in inventory with the hotbar item. \n" +
                "QuickMove: Moves the potion to the hotbar by right click shift but cant choose the desired slot to be refilled\n" +
                "Pickup: Clicks the item in the inventory then clicks again to move it to the hotbar.\n");

        register(module.enemyRange, "Range to search for enemies.");
        register(module.placeRange, "Range to place the bed.");
        register(module.yRange, "Vertical range to check for bed placement.");
        register(module.minEnemyDamage, "Must do at least this amount of damage to the ennemy to place the bed.");
        register(module.maxSelfDamage, "Maximum damage you are allowed to do to yourself.");
        register(module.noSuicide, "Prevent popping yourself by finding positions that do less damage to you without killing you.");

        register(module.render, "Render the bed position.");
        register(module.boxColor, "The fill color of the bed.");
        register(module.outLine, "The outline color of the bed.");
        register(module.lineWidth, "The width of the outline.");
        register(module.renderHead, "If the head of the bed should be rendered.");
        register(module.renderFeet, "If the feet of the bed should be rendered.");
        register(module.boxHeight, "The height of the render bed (normally 0.5).");
    }

    @Override
    public String getDescription()
    {
        return "Place and Explode bed in nether or end dimensions to obliterate your enemies.";
    }
}