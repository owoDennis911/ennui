package me.earth.earthhack.impl.modules.movement.airjump;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.client.SimpleData;

public class AirJump extends Module {


    public AirJump() {
        super("AirJump", Category.Movement);
        SimpleData data = new SimpleData(
                this, "AirJump");
        this.listeners.add(new LambdaListener<>(TickEvent.class, this::onTick));
    }

    public void onTick(TickEvent event) {
        if (event.isSafe() && mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.jump(); }
    }
}