package me.earth.earthhack.impl.modules.movement.headhitter;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.misc.UpdateEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.util.client.SimpleData;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.blocks.SpecialBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;

public class HeadHitter extends Module {
    private final Setting<Integer> waitTimer =
            register(new NumberSetting<>("waitTimer", 100, 0, 1000));
    private final Setting<Integer> jumps =
            register(new NumberSetting<>("Jumps", 4, 1, 10));
    private final Setting<Integer> jumpDelay =
            register(new NumberSetting<>("jumpDelay", 100, 0, 1000));
    private final Setting<Boolean> whileSneaking =
            register(new BooleanSetting("WhileSneaking", false));
    private final Setting<Boolean> limitSpeed =
            register(new BooleanSetting("limitSpeed", false));
    protected final Setting<Float> maxSpeed =
            register(new NumberSetting<>("MaxSpeed", 27.2f, 1.0f, 40.0f));
    private final Setting<Boolean> maxSpeedFactorCalcs =
            register(new BooleanSetting("EffectsCalcs", false));

    private final StopWatch jumpTimer = new StopWatch();
    private int jumpsPerformed = 0;
    private double lastPosX;
    private double lastPosZ;

    public HeadHitter() {
        super("HeadHitter", Category.Movement);
        SimpleData data = new SimpleData(this, "Makes you faster on hypixel.");
        data.register(waitTimer, "Delay in milliseconds to wait after having done all the jumps.");
        data.register(jumps, "Number of jumps to perform before waiting.");
        data.register(jumpDelay, "Delay in milliseconds between jumps.");
        data.register(whileSneaking, "If it should work while sneaking.");
        data.register(limitSpeed, "If it should return whenever the speed is too high.");
        data.register(maxSpeed, "Max speed in km/h allowed (default: 27.2).");
        data.register(maxSpeedFactorCalcs, "If it should include speed effect calculations to modify dynamically the max speed.");
        this.setData(data);



        this.listeners.add(new LambdaListener<>(UpdateEvent.class, e -> {
            if(mc.player == null) return;
            lastPosX = mc.player.posX;
            lastPosZ = mc.player.posZ;
            double currentPosX = mc.player.posX;
            double currentPosZ = mc.player.posZ;

            double deltaX = currentPosX - lastPosX;
            double deltaZ = currentPosZ - lastPosZ;
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double speedMPS = horizontalDistance * 20;
            double speedKPH = speedMPS * 3.6;

            double effectiveMaxSpeed = maxSpeed.getValue();
            if (maxSpeedFactorCalcs.getValue() && mc.player.isPotionActive(MobEffects.SPEED)) {
                PotionEffect effect = mc.player.getActivePotionEffect(MobEffects.SPEED);
                if (effect != null) {
                    effectiveMaxSpeed *= (1 + 0.2 * (effect.getAmplifier() + 1));
                }
            }

            lastPosX = currentPosX;
            lastPosZ = currentPosZ;

            if (limitSpeed.getValue() && speedKPH > effectiveMaxSpeed) {
                return;
            }


            if (!mc.player.onGround || mc.player.moveForward <= 0 || !checkForHead()) {
                return;
            }

            if (!whileSneaking.getValue() && mc.player.isSneaking()) {
                return;
            }

            if (!jumpTimer.passed(jumpDelay.getValue())) {
                return;
            }

            if (jumpsPerformed >= jumps.getValue()) {
                if (!jumpTimer.passed(waitTimer.getValue())) {
                    return;
                }
                jumpsPerformed = 0;
            }


            mc.player.jump();
            jumpsPerformed++;
            jumpTimer.reset();

        }));
    }


    private boolean checkForHead() {
        BlockPos pos = mc.player.getPosition().up(2);
        Block block = mc.world.getBlockState(pos).getBlock();

        return !SpecialBlocks.BOOST.contains(block);
    }
}
