package me.earth.earthhack.impl.modules.client.altsync;

import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import net.minecraft.client.Minecraft;

final class ListenerMouse extends ModuleListener<AltSync, MotionUpdateEvent> {
    private float prevYaw = -Float.MAX_VALUE;
    private float prevPitch = -Float.MAX_VALUE;

    public ListenerMouse(AltSync module) {
        super(module, MotionUpdateEvent.class);
    }

    @Override
    public void invoke(MotionUpdateEvent event) {
        float currentYaw = Minecraft.getMinecraft().player.rotationYaw;
        float currentPitch = Minecraft.getMinecraft().player.rotationPitch;

        if (currentYaw != prevYaw || currentPitch != prevPitch) {
            prevYaw = currentYaw;
            prevPitch = currentPitch;
            module.sendKey("mouse:" + currentYaw + ":" + currentPitch);
        }
    }
}
