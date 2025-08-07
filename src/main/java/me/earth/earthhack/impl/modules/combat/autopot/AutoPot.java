package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Complexity;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.gui.visibility.PageBuilder;
import me.earth.earthhack.impl.gui.visibility.Visibilities;
import me.earth.earthhack.impl.modules.combat.autopot.modes.*;
import me.earth.earthhack.impl.util.client.SimpleData;
import me.earth.earthhack.impl.util.math.StopWatch;
import me.earth.earthhack.impl.util.minecraft.InventoryUtil;

import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class AutoPot extends Module {

    protected final Setting<apPages> pages =
            register(new EnumSetting<>("Page", apPages.Main));

    protected final Setting<rotationIQ> rotIQ =
            register(new EnumSetting<>("rotIQ", rotationIQ.noIQ));

    protected final Setting<rotationMode> rotMode =
            register(new EnumSetting<>("rotMode", rotationMode.Event));




    protected final Setting<Boolean> heal =
            register(new BooleanSetting("Heal", false));
    protected final Setting<Integer> health =
            register(new NumberSetting<>("health", 10, 1, 20));
    protected final Setting<Integer> healSlot =
            register(new NumberSetting<>("healSlot", 8, 0, 8));
    protected final Setting<Integer> healDelay =
            register(new NumberSetting<>("healDelay", 100, 1, 1000));
    protected final Setting<Boolean> speed =
            register(new BooleanSetting("Speed", false));
    protected final Setting<Integer> speedSlot =
            register(new NumberSetting<>("speedSlot", 8, 0, 8));
    protected final Setting<Integer> speedDelay =
            register(new NumberSetting<>("speedDelay", 100, 1, 1000));
    protected final Setting<refill> refillType =
            register(new EnumSetting<>("refillMode", refill.Swap));
    protected final Setting<throwType> throwMode =
            register(new EnumSetting<>("throwMode", throwType.INSTANT));
    protected final Setting<Integer> maxInstantThrows =
            register(new NumberSetting<>("instantThrows", 3, 0, 5));
    protected final Setting<resetCounterOn> resetCounter =
            register(new EnumSetting<>("resetCounter", resetCounterOn.hpAboveTwenty));
    protected final Setting<stageThrow> stageForThrow =
            register(new EnumSetting<>("throwStage", stageThrow.PRE)).setComplexity(Complexity.Dev);


    protected final Setting<Integer> minGroundTime =
            register(new NumberSetting<>("groundTime", 100, 0, 1000));

    protected final Setting<Boolean> silentSwitch =
            register(new BooleanSetting("Silent", true));
    protected final Setting<Boolean> whileEating =
            register(new BooleanSetting("whileEating", true));


    protected final StopWatch groundTime = new StopWatch();
    protected boolean justCancelled;
    protected boolean needSpeed;
    protected boolean needHeal;
    protected int lastSlot = -1;
    protected int throwCounter = 0;

    public AutoPot() {
        super("AutoPot", Category.Combat);
        this.listeners.add(new ListenerMotion(this));
        this.listeners.add(new ListenerTick(this));
        this.listeners.add(new ListenerUseItem(this));

        SimpleData data = new SimpleData(this,
                "AutoPot");
        data.register(rotMode, "Both: Uses event rotations midair and packet on ground.\n" +
                "Packet: Uses packets rotations and only works on ground.\n" +
                "Event: Uses events rotations midair and on ground.");
        data.register(stageForThrow, "PRE: It will throw the potions in the PRE stage which" +
                "will cause issues for event rotations but fixes comp issues \n" +
                "POST: Throws on POST stage but has compatibility issues");
        data.register(refillType,
                "SWAP: Swaps item in inventory with the hotbar item.\n" +
                        "QUICKMOVE: Moves the potion to the hotbar by right click shift but \n"
                        + "cant choose the desired slot to be refilled\n"
                        + "PICKUP: Clicks the item in the inventory then clicks again to move it to the hotbar");
        data.register(resetCounter,
                "hpAboveTwenty: If hp is higher than 20 it will reset the counter.\n" +
                        "delayPassed: Counter will reset after time since last throws is passed");
        data.register(healDelay, "Delay between healing potions.");
        data.register(speedDelay, "Delay between speed potions.");

        data.register(minGroundTime, "Minimum onGround time to use packet rotations.");


        this.setData(data);

        new PageBuilder<>(this, pages)
                .addPage(p -> p == apPages.Main, heal, health, healSlot, healDelay, speed, speedSlot, speedDelay,
                        refillType, throwMode, maxInstantThrows, resetCounter, stageForThrow, minGroundTime,
                        silentSwitch, whileEating)
                .addPage(p -> p == apPages.Rots, rotIQ, rotMode)
                .register(Visibilities.VISIBILITY_MANAGER);

    }

    @Override
    protected void onEnable() {
        needHeal = false;
        justCancelled = false;
        needSpeed = false;
        lastSlot = -1;

    }

    @Override
    protected void onDisable() {
        if (lastSlot != -1) {
            Locks.acquire(Locks.PLACE_SWITCH_LOCK, () ->
                    InventoryUtil.switchTo(lastSlot));
            lastSlot = -1;
        }
    }

    public String getDisplayInfo() {
        return "" + InventoryUtil.getCount(Items.SPLASH_POTION);
    }


    public boolean needHeal() {
        return mc.player.getHealth() <= health.getValue();
    }

    public boolean needSpeed() {
        PotionEffect speedEffect = mc.player.getActivePotionEffect(MobEffects.SPEED);
        return speedEffect == null;
    }

    public void calcGround() {
        if (!mc.player.onGround) {
            groundTime.reset();
        }
    }
    public void calcThrows() {
        if (resetCounter.getValue() == resetCounterOn.hpAboveTwenty && mc.player.getHealth() >= health.getValue()) {
            throwCounter = 0;
        }
    }
    public boolean groundedEnough () {
        return groundTime.passed(minGroundTime.getValue());
    }
}




