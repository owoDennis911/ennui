package me.earth.earthhack.impl.modules.misc.backup;

import me.earth.earthhack.impl.event.events.keyboard.KeyboardEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;

import static me.earth.earthhack.impl.modules.misc.backup.BackUp.ATLAS;

final class ListenerKeyPress
        extends ModuleListener<BackUp, KeyboardEvent>
{
    public ListenerKeyPress(BackUp module)
    {
        super(module, KeyboardEvent.class);
    }

    @Override
    public void invoke(KeyboardEvent event)
    {
        if (event.getKey() == module.sendChat.getValue().getKey() && module.cooldownTimer.passed(module.cooldown.getValue() * 1000))
        {
            String coordsMessage = module.getCoordsMessage();
            mc.player.sendChatMessage(coordsMessage);
            module.cooldownTimer.reset();
        }
        if (event.getKey() == module.sendAtlas.getValue().getKey() && module.cooldownTimer.passed(module.cooldown.getValue() * 1000))
        {
            String coordsMessage = module.getCoordsMessage();
            ATLAS.get().sendMessage(coordsMessage);
            module.cooldownTimer.reset();
        }
        if (event.getKey() == module.sendWebHook.getValue().getKey() && module.cooldownTimer.passed(module.cooldown.getValue() * 1000))
        {
            module.cooldownTimer.reset();
        }
    }
}
