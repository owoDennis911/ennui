package me.earth.earthhack.impl.modules.movement.nobedstep;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.client.SimpleData;

public class NoBedStep extends Module {

    public NoBedStep() {
        super("NoBedStep", Category.Movement);
        SimpleData data = new SimpleData(this, "Doesn't step on beds to avoid getting stuck.");
        this.setData(data);
        this.listeners.add(new LambdaListener<>(TickEvent.class, this::onTick));
    }


    @Override
    public void onDisable() {
        if(mc.player != null){
            mc.player.stepHeight = 0.6f;
        }
    }

    public void onTick(TickEvent event) {
        if (event.isSafe()) {
            mc.player.stepHeight = 0.1f;
        }
    }
}
