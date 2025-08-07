package me.earth.earthhack.impl.modules.combat.pistonaura;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.ColorSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.gui.visibility.PageBuilder;
import me.earth.earthhack.impl.gui.visibility.Visibilities;
import me.earth.earthhack.impl.modules.combat.pistonaura.modes.paPages;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class PistonAura extends Module {

    protected final Setting<paPages> pages =
            register(new EnumSetting<>("Page", paPages.General));

    // General

    public final Setting<Float> range = register(new NumberSetting<>("range", 5.0f, 1.0f, 6.0f));
    public final Setting<Boolean> hit = register(new BooleanSetting("hit", true));
    public final Setting<Float> breakRange = register(new NumberSetting<>("breakRange", 5.0f, 1.0f, 6.0f));
    public final Setting<Boolean> onlyInHole = register(new BooleanSetting("holeFriendly", true));
    public final Setting<Boolean> allowInventory = register(new BooleanSetting("allowInventory", true));
    public final Setting<Boolean> fire = register(new BooleanSetting("fire", true));

    // Values
    public final Setting<Integer> placesPerTick = register(new NumberSetting<>("places", 4, 1, 4));
    public final Setting<Integer> cycleDelay = register(new NumberSetting<>("cycleDelay", 700, 0, 1000));
    public final Setting<Integer> pistonDelay = register(new NumberSetting<>("pistonDelay", 0, 0, 1000));
    public final Setting<Integer> fireDelay = register(new NumberSetting<>("fireDelay", 0, 0, 1000));
    public final Setting<Integer> crystalDelay = register(new NumberSetting<>("crystalDelay", 100, 0, 1000));
    public final Setting<Integer> redstoneDelay = register(new NumberSetting<>("redstoneDelay", 200, 0, 1000));
    public final Setting<Integer> hitDelay = register(new NumberSetting<>("hitDelay", 500, 0, 1000));

    // Render
    protected final Setting<Boolean> render = register(new BooleanSetting("Render", true));
    protected final Setting<Color> pistonColor = register(new ColorSetting("pistonBox", new Color(255, 170, 0, 30)));
    protected final Setting<Color> pistonOutLine = register(new ColorSetting("pistonOutline", new Color(255, 170, 0, 255)));
    protected final Setting<Double> pistonSlideTime = register(new NumberSetting<>("pistonSlide-Time", 100.0, 1.0, 5000.0));
    protected final Setting<Color> fireColor = register(new ColorSetting("fireBox", new Color(236, 0, 0, 30)));
    protected final Setting<Color> fireOutLine = register(new ColorSetting("fireOutline", new Color(255, 0, 0, 255)));
    protected final Setting<Double> fireSlideTime = register(new NumberSetting<>("fireSlide-Time", 100.0, 1.0, 5000.0));
    protected final Setting<Float> fireBoxHeight = register(new NumberSetting<>("fireBoxHeight", 0.1f, 0.0f, 2.0f));
    protected final Setting<Color> crystalColor = register(new ColorSetting("crystalBox", new Color(104, 0, 232, 30)));
    protected final Setting<Color> crystalOutLine = register(new ColorSetting("crystalOutline", new Color(104, 0, 232, 255)));
    protected final Setting<Double> crystalSlideTime = register(new NumberSetting<>("crystalSlide-Time", 100.0, 1.0, 5000.0));
    protected final Setting<Float> crystalBoxHeight = register(new NumberSetting<>("crystalBoxHeight", 0.3f, 0.0f, 2.0f));
    protected final Setting<Color> redstoneColor = register(new ColorSetting("redstoneBox", new Color(255, 255, 255, 120)));
    protected final Setting<Color> redstoneOutLine = register(new ColorSetting("redstoneOutline", new Color(255, 255, 255, 255)));
    protected final Setting<Double> redstoneSlideTime = register(new NumberSetting<>("redstoneSlide-Time", 100.0, 1.0, 5000.0));
    protected final Setting<Float> lineWidth = register(new NumberSetting<>("Line-Width", 0.5f, 0.0f, 5.0f));
    protected final Setting<Double> fadeTime = register(new NumberSetting<>("fade-Time", 100.0, 1.0, 5000.0));

    private String data;
    private BlockPos pistonPos;
    private BlockPos firePos;
    private BlockPos crystalPos;
    private BlockPos redstonePos;

    public PistonAura() {
        super("PistonAura", Category.Combat);
        this.setData(new PistonAuraData(this));
        this.listeners.add(new ListenerMotion(this));
        this.listeners.add(new ListenerRender(this));

        new PageBuilder<>(this, pages)
                .addPage(p -> p == paPages.General,onlyInHole, allowInventory, fire, hit, range, breakRange)
                .addPage(p -> p == paPages.Values,placesPerTick, cycleDelay, pistonDelay, fireDelay, crystalDelay, redstoneDelay, hitDelay)
                .addPage(p -> p == paPages.Render,render, pistonColor, pistonOutLine, pistonSlideTime, fireColor, fireOutLine,
                        fireSlideTime, fireBoxHeight, crystalColor, crystalOutLine, crystalSlideTime, crystalBoxHeight,
                        redstoneColor, redstoneOutLine, redstoneSlideTime, lineWidth, fadeTime)
                .register(Visibilities.VISIBILITY_MANAGER);
    }


    public void setPistonPos(BlockPos pos) {
        this.pistonPos = pos;
    }

    public BlockPos getPistonPos() {
        return pistonPos;
    }

    public void setFirePos(BlockPos pos) {
        this.firePos = pos;
    }

    public BlockPos getFirePos() {
        return firePos;
    }

    public void setCrystalPos(BlockPos pos) {
        this.crystalPos = pos;
    }

    public BlockPos getCrystalPos() {
        return crystalPos;
    }

    public void setRedstonePos(BlockPos pos) {
        this.redstonePos = pos;
    }

    public BlockPos getRedstonePos() {
        return redstonePos;
    }

    public void setNumber(int number, String data) {
        switch (number) {
            case 0:
                this.data = "Error: " + data;
                break;
            case 1:
                this.data = "Estimated damage output: " + data;
                break;
        }
    }

    @Override
    public String getDisplayInfo() {
        return data;
    }
}
