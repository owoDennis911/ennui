package me.earth.earthhack.impl.modules.misc.backup;

import me.earth.earthhack.api.cache.ModuleCache;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BindSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.api.util.bind.Bind;
import me.earth.earthhack.impl.modules.Caches;
import me.earth.earthhack.impl.util.client.SimpleData;
import me.earth.earthhack.impl.modules.client.atlas.Atlas;
import me.earth.earthhack.impl.util.math.StopWatch;
import net.minecraft.util.math.BlockPos;

public class BackUp extends Module {
    protected final Setting<Bind> sendChat =
            register(new BindSetting("backupOnChat", Bind.none()));
    protected final Setting<Bind> sendAtlas =
            register(new BindSetting("backupOnAtlas", Bind.none()));
    protected final Setting<Bind> sendWebHook =
            register(new BindSetting("backupOnWebHook", Bind.none()));
    protected final Setting<Integer> cooldown =
            register(new NumberSetting<>("Cooldown", 5, 5, 60));

    protected final StopWatch cooldownTimer = new StopWatch();

    public BackUp() {
        super("BackUp", Category.Misc);
        this.listeners.add(new ListenerKeyPress(this));
        SimpleData data = new SimpleData(this, "Calls for backup by sending your coordinates in chat.");
        this.setData(data);
    }

    protected static final ModuleCache<Atlas> ATLAS = Caches.getModule(Atlas.class);

    protected String getCoordsMessage() {
        if (mc.player != null) {
            BlockPos pos = mc.player.getPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            String dimension = getDimensionName();
            return String.format("My coords are: (%d, %d, %d) in the %s.", x, y, z, dimension);
        }
        return "Unknown location.";
    }

    private String getDimensionName() {
        if (mc.player != null) {
            int dimensionId = mc.player.dimension;
            switch (dimensionId) {
                case 0:
                    return "Overworld";
                case -1:
                    return "Nether";
                case 1:
                    return "End";
                default:
                    return "Unknown dimension";
            }
        }
        return "Unknown dimension";
    }
}
