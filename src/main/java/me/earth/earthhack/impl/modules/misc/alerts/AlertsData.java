package me.earth.earthhack.impl.modules.misc.alerts;

import me.earth.earthhack.api.module.data.DefaultData;

final class AlertsData extends DefaultData<Alerts>
{
    public AlertsData(Alerts module)
    {
        super(module);
        this.descriptions.put(module.totems,
                "Announces when a player pops a totem.");
        this.descriptions.put(module.deaths, "Announces kills.");
        this.descriptions.put(module.visualrange, "Announces when a players enters or leaves visualrange.");

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