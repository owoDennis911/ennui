package me.earth.earthhack.impl.modules.combat.bedaura;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.ColorSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.api.setting.settings.BindSetting;
import me.earth.earthhack.api.util.bind.Bind;
import me.earth.earthhack.impl.gui.visibility.PageBuilder;
import me.earth.earthhack.impl.gui.visibility.Visibilities;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.bedauraPages;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.refill;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.renderAnim;
import me.earth.earthhack.impl.modules.combat.bedaura.modes.renderM;
import java.awt.Color;
import net.minecraft.util.math.BlockPos;

public class BedAura extends Module {

    protected final Setting<bedauraPages> pages =
            register(new EnumSetting<>("Page", bedauraPages.General));

    // General
    protected final Setting<Integer> bedSlot = register(new NumberSetting<>("bedSlot", 1, 1, 9));
    protected final Setting<refill> refillMode = register(new EnumSetting<>("refillMode", refill.PickUp));
    protected final Setting<Boolean> hardPitch = register(new BooleanSetting("setPitch", true));
    protected final Setting<Integer> toPitch = register(new NumberSetting<>("pitch", 90, -90, 90));


    // Place
    protected final Setting<Integer> placeDelay = register(new NumberSetting<>("placeDelay", 100, 0, 500));
    protected final Setting<Integer> breakDelay = register(new NumberSetting<>("breakDelay", 100, 0, 500));
    protected final Setting<Integer> slowPlaceDelay = register(new NumberSetting<>("slowPlaceDelay", 300, 0, 500));
    protected final Setting<Integer> slowBreakDelay = register(new NumberSetting<>("slowBreakDelay", 0, 0, 500));
    protected final Setting<Boolean> dynDelay = register(new BooleanSetting("dynamic", true));
    protected final Setting<Integer> dynamicDelay = register(new NumberSetting<>("dynamicDelay", 100, 0, 500));
    protected final Setting<Integer> updateDelay = register(new NumberSetting<>("updateDelay", 100, 0, 500));

    // Calc
    protected final Setting<Float> enemyRange = register(new NumberSetting<>("Range", 10.0f, 1.0f, 12.0f));
    protected final Setting<Float> placeRange = register(new NumberSetting<>("placeRange", 6.0f, 1.0f, 6.0f));
    protected final Setting<Float> yRange = register(new NumberSetting<>("yRange", 2.5f, 0.0f, 6.0f));
    protected final Setting<Float> minEnemyDamage = register(new NumberSetting<>("minDamage", 10.0f, 1.0f, 20.0f));
    protected final Setting<Float> maxSelfDamage = register(new NumberSetting<>("maxSelfDamage", 10.0f, 1.0f, 20.0f));
    protected final Setting<Float> slowMinDmg = register(new NumberSetting<>("slowMinDmg", 4.0f, 0.0f, 20.0f));
    protected final Setting<Integer> noSuicide = register(new NumberSetting<>("noSuicide", 10, 1, 20));

    // SelfBed
    protected final Setting<Float> selfBedMinDmg = register(new NumberSetting<>("selfBedMinDmg", 10.0f, 1.0f, 20.0f));
    protected final Setting<Float> selfBedMaxDmg = register(new NumberSetting<>("selfBedMaxDmg", 10.0f, 1.0f, 20.0f));
    protected final Setting<Bind> selfBedBind = register(new BindSetting("selfBed", Bind.none()));

    // Render
    protected final Setting<Boolean> render = register(new BooleanSetting("Render", true));
    protected final Setting<renderM> renderMode = register(new EnumSetting<>("renderMode", renderM.Bed));
    protected final Setting<Boolean> renderHead = register(new BooleanSetting("Head", true));
    protected final Setting<Boolean> renderFeet = register(new BooleanSetting("Feet", true));
    protected final Setting<Float> boxHeight = register(new NumberSetting<>("Height", 1.0f, 0.0f, 2.0f));
    protected final Setting<Color> boxColor = register(new ColorSetting("Box", new Color(255, 255, 255, 120)));
    protected final Setting<Color> outLine = register(new ColorSetting("Outline", new Color(255, 255, 255, 255)));
    protected final Setting<Float> lineWidth = register(new NumberSetting<>("Line-Width", 1.0f, 0.0f, 5.0f));
    protected final Setting<renderAnim> renderAnimation = register(new EnumSetting<>("renderAnimation", renderAnim.Slide));
    protected final Setting<Double> slideTime = register(new NumberSetting<>("Slide-Time", 100.0, 1.0, 1000.0));
    protected final Setting<Boolean> renderDamage = register(new BooleanSetting("Render-Damage", false));
    protected final Setting<Float> damageX = register(new NumberSetting<>("Damage-X", 0.0f, -5.0f, 5.0f));
    protected final Setting<Float> damageY = register(new NumberSetting<>("Damage-Y", 0.0f, -5.0f, 5.0f));
    protected final Setting<Float> damageZ = register(new NumberSetting<>("Damage-Z", 0.0f, -5.0f, 5.0f));
    protected final Setting<Color> damageColor = register(new ColorSetting("damageColor", new Color(255, 255, 255, 255)));

    private BlockPos feetPos;
    private BlockPos headPos;
    private String damage;

    public BedAura() {
        super("BedAura", Category.Combat);
        this.setData(new BedAuraData(this));
        this.listeners.add(new ListenerMotion(this));
        this.listeners.add(new ListenerRender(this));

        new PageBuilder<>(this, pages)
                .addPage(p -> p == bedauraPages.General, bedSlot, refillMode, hardPitch, toPitch)
                .addPage(p -> p == bedauraPages.Place, placeDelay, breakDelay, slowPlaceDelay, slowBreakDelay, updateDelay, dynDelay, dynamicDelay)
                .addPage(p -> p == bedauraPages.Calc, enemyRange, placeRange, yRange, minEnemyDamage, maxSelfDamage, slowMinDmg, noSuicide)
                .addPage(p -> p == bedauraPages.Render,
                        render, boxColor, outLine, lineWidth, renderHead,
                        renderFeet, boxHeight, renderMode, renderAnimation,
                        slideTime, renderDamage, damageX, damageY, damageZ, damageColor)
                .addPage(p -> p == bedauraPages.SelfBed, selfBedBind, selfBedMinDmg, selfBedMaxDmg)
                .register(Visibilities.VISIBILITY_MANAGER);
    }

    public BlockPos getFeetPos() {
        return feetPos;
    }

    public void setFeetPos(BlockPos pos) {
        this.feetPos = pos;
    }

    public BlockPos getHeadPos() {
        return headPos;
    }

    public void setHeadPos(BlockPos pos) {
        this.headPos = pos;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }
}
