package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;



final class ListenerTick extends ModuleListener<AutoPot, TickEvent> {
    public ListenerTick(AutoPot module) {
        super(module, TickEvent.class);
    }

    @Override
    public void invoke(TickEvent event) {
        if (event.isSafe()) {
            module.calcGround();
            module.calcThrows();
        }
    }
}
