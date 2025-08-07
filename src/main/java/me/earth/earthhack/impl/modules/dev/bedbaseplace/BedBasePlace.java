package me.earth.earthhack.impl.modules.dev.bedbaseplace;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.*;
import java.awt.Color;

import me.earth.earthhack.impl.gui.visibility.PageBuilder;
import me.earth.earthhack.impl.gui.visibility.Visibilities;
import me.earth.earthhack.impl.modules.dev.bedbaseplace.modes.bedBasePPages;
import net.minecraft.util.math.BlockPos;

public class BedBasePlace extends Module {

    protected final Setting<bedBasePPages> pages =
            register(new EnumSetting<>("Page", bedBasePPages.General));

    //General
    protected final Setting<Float> placeRange = register(new NumberSetting<>("placeRange", 5.2f, 1.0f, 6.0f));
    protected final Setting<Float> targetRange = register(new NumberSetting<>("targetRange", 5.2f, 1.0f, 12.0f));
    protected final Setting<Float> maxSelfDamage = register(new NumberSetting<>("maxSelfDmg", 10.0f, 1.0f, 20.0f));
    protected final Setting<Float> minEnemyDamage = register(new NumberSetting<>("minDmg", 6.0f, 1.0f, 20.0f));
    protected final Setting<Boolean> async = register(new BooleanSetting("Async", true));

    //Place
    protected final Setting<Integer> blocks = register(new NumberSetting<>("Blocks", 1, 1, 10));
    protected final Setting<Integer> obbyDelay = register(new NumberSetting<>("ObbyDelay", 500, 0, 5000));
    protected final Setting<Integer> calcDelay = register(new NumberSetting<>("calcDelay", 500, 0, 5000));
    protected final Setting<Integer> helpingDepth = register(new NumberSetting<>("Depth", 3, 0, 10));

    //Render
    protected final Setting<Boolean> render = register(new BooleanSetting("Render", true));
    protected final Setting<Boolean> fade = register(new BooleanSetting("Fade", true));
    protected final Setting<Integer> zoomTime = register(new NumberSetting<>("ZoomTime", 500, 100, 5000));
    protected final Setting<Double> zoomOffset = register(new NumberSetting<>("ZoomOffset", 0.5, -1.0, 1.0));
    protected final Setting<Color> boxColor = register(new ColorSetting("Box", new Color(255, 255, 255, 120)));
    protected final Setting<Color> outLine = register(new ColorSetting("Outline", new Color(255, 255, 255, 255)));
    protected final Setting<Float> lineWidth = register(new NumberSetting<>("Line-Width", 1.0f, 0.0f, 5.0f));


    private String damage;
    private BlockPos pos;

    public BedBasePlace() {
        super("BedBasePlace", Category.Dev);
        this.setData(new BedBasePlaceData(this));
        this.listeners.add(new ListenerMotion(this));
        this.listeners.add(new ListenerRender(this));

        new PageBuilder<>(this, pages)
                .addPage(p -> p == bedBasePPages.General, placeRange,targetRange, maxSelfDamage, minEnemyDamage, async)
                .addPage(p -> p == bedBasePPages.Place, blocks, obbyDelay, calcDelay, helpingDepth)
                .addPage(p -> p == bedBasePPages.Render, render,fade, zoomTime, zoomOffset, boxColor, outLine, lineWidth)
                .register(Visibilities.VISIBILITY_MANAGER);
    }

    @Override
    public String getDisplayInfo() {
        return "Damage: " + damage;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public void definePos(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getCurrentPos() {
        return pos;
    }
}
