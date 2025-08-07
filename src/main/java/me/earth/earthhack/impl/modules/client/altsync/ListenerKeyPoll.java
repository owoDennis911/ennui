package me.earth.earthhack.impl.modules.client.altsync;

import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.minecraft.KeyBoardUtil;

final class ListenerKeyPoll extends ModuleListener<AltSync, MotionUpdateEvent> {
    private final boolean[] prevStates = new boolean[256];

    public ListenerKeyPoll(AltSync module) {
        super(module, MotionUpdateEvent.class);
    }

    @Override
    public void invoke(MotionUpdateEvent event) {
        if (module.mode.getValue() == AltSync.Mode.Sender) {
            for (int key = 0; key < 256; key++) {
                boolean currentState = KeyBoardUtil.isKeyDown(key);
                if (currentState != prevStates[key]) {
                    prevStates[key] = currentState;
                    module.sendKey(key + ":" + currentState);
                }
            }
        }
    }
}
