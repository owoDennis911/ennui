package me.earth.earthhack.impl.modules.misc.autoez;

import me.earth.earthhack.api.module.data.DefaultData;


final class AutoEZData extends DefaultData<AutoEZ>
{
    public AutoEZData(AutoEZ module)
    {
        super(module);
        this.descriptions.put(module.totems,
                "Sends a message when a player pops a totem.");
        this.descriptions.put(module.deaths, "Sends a message when a player dies.");


    }

    @Override
    public int getColor()
    {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription()
    {
        return "Chat notifications for all sorts of stuff.";
    }

}