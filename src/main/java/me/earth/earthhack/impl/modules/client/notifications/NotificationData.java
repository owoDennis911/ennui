package me.earth.earthhack.impl.modules.client.notifications;

import me.earth.earthhack.api.module.data.DefaultData;

final class NotificationData extends DefaultData<Notifications>
{
    public NotificationData(Notifications module)
    {
        super(module);
        this.descriptions.put(module.modules,
                "Announces when modules get toggled.");
        this.descriptions.put(module.configure,
                "Configure the which modules should be announced.");
        this.descriptions.put(module.categories,
                "Click through the module categories.");
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
