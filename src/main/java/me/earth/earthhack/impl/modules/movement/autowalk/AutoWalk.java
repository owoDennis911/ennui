package me.earth.earthhack.impl.modules.movement.autowalk;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;

import me.earth.earthhack.impl.event.events.misc.UpdateEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.client.SimpleData;
import net.minecraft.client.settings.KeyBinding;

public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", Category.Movement);
        SimpleData data = new SimpleData(this, "AutoWalk");
        this.listeners.add(new LambdaListener<>(UpdateEvent.class, this::onUpdate));
    }

    public void onUpdate(UpdateEvent event) {
        if (mc.player != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);


        }
    }
}



