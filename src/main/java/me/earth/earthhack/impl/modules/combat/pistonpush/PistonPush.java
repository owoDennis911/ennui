package me.earth.earthhack.impl.modules.combat.pistonpush;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;

public class PistonPush extends Module {

    public final Setting<Integer> placeDelay = register(new NumberSetting<>("PlaceDelay", 100, 0, 500));
    public final Setting<Float> enemyRange = register(new NumberSetting<>("EnemyRange", 10.0f, 1.0f, 20.0f));
    public final Setting<Float> placeRange = register(new NumberSetting<>("placeRange", 5.0f, 1.0f, 6.0f));
    public final Setting<Boolean> clipCheck = register(new BooleanSetting("forceOnClip", true));
    public final Setting<Boolean> checkPush = register(new BooleanSetting("checkPush", true));
    public final Setting<Boolean> checkTrap = register(new BooleanSetting("checkTrap", true));


    public PistonPush() {
        super("PistonPush", Category.Combat);
        this.listeners.add(new ListenerMotion(this));
    }
}
