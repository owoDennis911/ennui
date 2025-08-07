package me.earth.earthhack.impl.modules.dev.holesnap;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Complexity;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.ColorSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.core.ducks.world.IChunk;
import me.earth.earthhack.impl.event.events.movement.MoveEvent;
import me.earth.earthhack.impl.event.events.render.Render3DEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.modules.render.holeesp.invalidation.AirHoleFinder;
import me.earth.earthhack.impl.modules.render.holeesp.invalidation.Hole;
import me.earth.earthhack.impl.modules.render.holeesp.invalidation.HoleManager;
import me.earth.earthhack.impl.modules.render.holeesp.invalidation.SimpleHoleManager;
import me.earth.earthhack.impl.util.client.SimpleData;
import me.earth.earthhack.impl.util.render.mutables.BBRender;

import me.earth.earthhack.impl.util.render.mutables.MutableBB;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Comparator;

public class HoleSnap extends Module {
    // Ranges
    private final Setting<Integer> downRange = register(new NumberSetting<>("Down Range", 5, 1, 8));
    private final Setting<Integer> upRange = register(new NumberSetting<>("Up Range", 1, 1, 8));
    private final Setting<Integer> horizontalRange = register(new NumberSetting<>("H Range", 5, 1, 8));

    private final Setting<Mode> xzMode = register(new EnumSetting<>("XZ-Mode", Mode.Constant));
    private final Setting<Double> xz = register(new NumberSetting<>("XZ-Speed", 0.2, 0.0, 10.0));
    private final Setting<Double> yOffset = register(new NumberSetting<>("Y-Offset", 1.0, 0.0, 1.0));
    private final Setting<Integer> stuckTicksSetting = register(new NumberSetting<>("StuckTicks", 20, 5, 100));
    private final Setting<Double> minSpeed = register(new NumberSetting<>("MinSpeed", 0.05, 0.01, 0.1));
    private final Setting<Boolean> useTimer = register(new BooleanSetting("useTimer", true));
    private final Setting<Float> timer = register(new NumberSetting<>("timer", 2.5f, 0f, 10f));
    private final Setting<Integer> lagTime = register(new NumberSetting<>("Lag-Time", 1000, 0, 10_000));
    private final Setting<Boolean> sneaking = register(new BooleanSetting("Sneaking", false));
    private final Setting<Boolean> holeCheck = register(new BooleanSetting("HoleCheck", true)).setComplexity(Complexity.Expert);
    private final Setting<Color> holeColor = register(new ColorSetting("HoleColor", Color.cyan));
    private final Setting<Boolean> renderHole = register(new BooleanSetting("Render Hole", true)); // Toggle for rendering

    private final HoleManager holeManager = new SimpleHoleManager();
    private final AirHoleFinder holeFinder = new AirHoleFinder(holeManager);
    private int stuckTicks = 0;

    public HoleSnap() {
        super("HoleSnap", Category.Dev);

        this.listeners.add(new LambdaListener<>(Render3DEvent.class, e -> onRender3D()));
        this.listeners.add(new LambdaListener<>(MoveEvent.class, event -> {
            if (mc.player.isSpectator()
                    || !Managers.NCP.passed(lagTime.getValue())
                    || !sneaking.getValue() && mc.player.isSneaking()) {
                return;
            }



            holeManager.reset();
            BlockPos pos = mc.player.getPosition();
            holeFinder.setChunk((IChunk) mc.world.getChunk(pos));
            holeFinder.setMaxX(pos.getX() + horizontalRange.getValue());
            holeFinder.setMinX(pos.getX() - horizontalRange.getValue());
            holeFinder.setMaxY(pos.getY() + upRange.getValue());
            holeFinder.setMinY(pos.getY() - downRange.getValue());
            holeFinder.setMaxZ(pos.getZ() + horizontalRange.getValue());
            holeFinder.setMinZ(pos.getZ() - horizontalRange.getValue());
            holeFinder.calcHoles();

            Hole hole = holeManager.getHoles()
                    .values()
                    .stream()
                    .min(Comparator.comparingDouble(this::getDistance))
                    .orElse(null);

            if (hole == null) {
                return;
            }


            double x = (hole.getX() + (hole.getMaxX() - hole.getX()) / 2.0) - mc.player.posX;
            double z = (hole.getZ() + (hole.getMaxZ() - hole.getZ()) / 2.0) - mc.player.posZ;
            double distance = Math.sqrt(x * x + z * z);
            if (distance == 0.0) {
                return;
            }
            if (useTimer.getValue()) {
                Managers.TIMER.setTimer(timer.getValue());
            }

            double pull_factor = xz.getValue() / distance;
            event.setX(modify(xzMode.getValue(), event.getX(), x * pull_factor));
            event.setZ(modify(xzMode.getValue(), event.getZ(), z * pull_factor));

            double speed = Managers.SPEED.getSpeedBpS();

            if (speed < minSpeed.getValue()) {
                stuckTicks++;
            } else {
                stuckTicks = 0;
            }

            if (stuckTicks >= stuckTicksSetting.getValue()) {
                Managers.TIMER.setTimer(1);
                this.disable();
            }

            if (holeCheck.getValue() && isInHole()) {
                Managers.TIMER.setTimer(1);
                this.disable();
            }


        }));


        SimpleData data = new SimpleData(this, "Pulls you into holes.");
        data.register(xzMode, "-Factor multiplies speed with XZ-Speed." +
                "\n-Constant sets horizontal speed to XZ-Speed." +
                "\n-Add increases speed by XZ-Speed." +
                "\n-Off disables changes.");
        data.register(xz, "Speed in horizontal movement.");
        data.register(yOffset, "Offset to the bottom of the hole when calculating distance.");
        this.setData(data);
    }

    @Override
    protected void onDisable() {
        Managers.TIMER.setTimer(1);
    }



    private boolean isInHole() {
        return holeManager
                .getHoles()
                .values()
                .stream()
                .anyMatch(h -> h.contains(mc.player.posX, mc.player.posY, mc.player.posZ));
    }


    private double modify(Mode mode, double value, double setting) {
        switch (mode) {
            case Factor:
                return value * setting;
            case Constant:
                return setting;
            case Off:
            default:
                return value;
        }
    }

    private double getDistance(Hole hole) {
        double holeX = hole.getX() + (hole.getMaxX() - hole.getX()) / 2.0;
        double holeY = hole.getY() + yOffset.getValue() / 2.0;
        double holeZ = hole.getZ() + (hole.getMaxZ() - hole.getZ()) / 2.0;
        return mc.player.getDistanceSq(holeX, holeY, holeZ);
    }


    public void onRender3D() {
        if (!renderHole.getValue()) {
            return;
        }

        Hole hole = getTargetHole();
        if (hole == null) {
            return;
        }

        // Extracted method for cleaner code
        MutableBB bb = getHoleBoundingBox(hole);

        // Render the hole with red color
        Color boxColor = holeColor.getValue();
        BBRender.renderBox(bb, boxColor, 1.5f);
    }
    private Hole getTargetHole() {
        return holeManager.getHoles()
                .values()
                .stream()
                .min(Comparator.comparingDouble(this::getDistance))
                .orElse(null);
    }

    private MutableBB getHoleBoundingBox(Hole hole) {
        double minX = hole.getX() - mc.getRenderManager().viewerPosX;
        double minY = hole.getY() - mc.getRenderManager().viewerPosY;
        double minZ = hole.getZ() - mc.getRenderManager().viewerPosZ;
        double maxX = hole.getMaxX() - mc.getRenderManager().viewerPosX;
        double maxY = hole.getY() + 1.0 - mc.getRenderManager().viewerPosY;
        double maxZ = hole.getMaxZ() - mc.getRenderManager().viewerPosZ;

        return new MutableBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public enum Mode {
        Factor,
        Constant,
        Add,
        Off
    }


}
