package me.earth.earthhack.impl.modules.misc.autoez;

import me.earth.earthhack.impl.event.events.misc.DeathEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.managers.Managers;
import net.minecraft.entity.player.EntityPlayer;

final class ListenerDeath extends ModuleListener<me.earth.earthhack.impl.modules.misc.autoez.AutoEZ, DeathEvent>
{
    public ListenerDeath(AutoEZ module)
    {
        super(module, DeathEvent.class);
    }

    @Override
    public void invoke(DeathEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            int pops = Managers.COMBAT.getPops(event.getEntity());
            if (pops > 0)
            {
                module.onDeath(event.getEntity(),
                        Managers.COMBAT.getPops(event.getEntity()));
            }
        }
    }

}
