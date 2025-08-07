package me.earth.earthhack.impl.modules.combat.autoregear;

import me.earth.earthhack.impl.event.events.keyboard.KeyboardEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;

final class ListenerKeyPress extends ModuleListener<AutoRegear, KeyboardEvent> {

    public ListenerKeyPress(AutoRegear module) {
        super(module, KeyboardEvent.class);
    }

    @Override
    public void invoke(KeyboardEvent event) {
        if (event.getKey() == module.placeShulkerKey.getValue().getKey() && module.cooldownTimer.passed(250)) {
                module.attemptRegear();
                module.cooldownTimer.reset();
            }
        }
    }

