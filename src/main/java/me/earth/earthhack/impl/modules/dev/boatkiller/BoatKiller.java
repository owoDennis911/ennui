package me.earth.earthhack.impl.modules.dev.boatkiller;

import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.util.helpers.disabling.DisablingModule;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.util.math.Vec3d;

public class BoatKiller extends DisablingModule {
    public final Setting<Boolean> ytp = register(new BooleanSetting("Y packet", true));
    public final Setting<Boolean> xtp = register(new BooleanSetting("X packet", false));
    public final Setting<Boolean> ztp = register(new BooleanSetting("Z packet", false));
    protected final Setting<Integer> loop = register(new NumberSetting<>("Loop", 4, 1, 20));
    protected final Setting<Float> y = register(new NumberSetting<>("Y1", 0.15f, -9.0f, 9.0f));
    protected final Setting<Float> y2 = register(new NumberSetting<>("Y2", 65.0f, -400f, 400f));
    protected final Setting<Float> x = register(new NumberSetting<>("X1", 9.0f, -400f, 400f));
    protected final Setting<Float> z = register(new NumberSetting<>("Z1", 9.0f, -400f, 400f));
    public BoatKiller() {

        super("BoatKiller", Category.Movement);
        setData(new BoatKillerData(this));
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        int loopCountMax = loop.getValue();
        for (int loopCount = 0; loopCount < loopCountMax; loopCount++) {
            if (ytp.getValue() && mc.player.getRidingEntity() instanceof EntityBoat) {
                EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                Vec3d originalPos = boat.getPositionVector();
                boat.setPosition(boat.posX, boat.posY + y.getValue(), boat.posZ);
                CPacketVehicleMove groundPacket = new CPacketVehicleMove(boat);
                boat.setPosition(boat.posX, boat.posY + y2.getValue(), boat.posZ);
                CPacketVehicleMove skyPacket = new CPacketVehicleMove(boat);

                for (int i = 0; i < 100; i++) {
                    mc.player.connection.sendPacket(skyPacket);
                    mc.player.connection.sendPacket(groundPacket);
                    mc.player.connection.sendPacket(new CPacketVehicleMove(boat));
                }

                boat.setPosition(originalPos.x, originalPos.y, originalPos.z);
            }

            if (xtp.getValue() && mc.player.getRidingEntity() instanceof EntityBoat) {
                EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                Vec3d originalPos = boat.getPositionVector();
                boat.setPosition(boat.posX + x.getValue(), boat.posY, boat.posZ);
                CPacketVehicleMove groundPacket = new CPacketVehicleMove(boat);
                boat.setPosition(originalPos.x, originalPos.y, originalPos.z);
                for (int i = 0; i < 100; i++) {
                    mc.player.connection.sendPacket(groundPacket);
                    mc.player.connection.sendPacket(new CPacketVehicleMove(boat));
                }
            }

            if (ztp.getValue() && mc.player.getRidingEntity() instanceof EntityBoat) {
                EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                Vec3d originalPos = boat.getPositionVector();
                boat.setPosition(boat.posX, boat.posY, boat.posZ + z.getValue());
                CPacketVehicleMove groundPacket = new CPacketVehicleMove(boat);
                boat.setPosition(originalPos.x, originalPos.y, originalPos.z);
                for (int i = 0; i < 100; i++) {
                    mc.player.connection.sendPacket(groundPacket);
                }
                mc.player.connection.sendPacket(new CPacketVehicleMove(boat));
            }
        }

        disable();
    }
}
