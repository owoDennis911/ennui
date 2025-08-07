package me.earth.earthhack.impl.modules.dev.bettereat;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.impl.event.events.keyboard.MouseEvent;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import net.minecraft.item.ItemFood;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BetterEat extends Module {
    private final Setting<Boolean> toggleEat = register(new BooleanSetting("Toggle Eat", false));
    private boolean toggled = false;

    public BetterEat() {
        super("BetterEat", Category.Dev);


        this.listeners.add(new LambdaListener<>(MouseEvent.class, event -> {
            if (toggleEat.getValue() && event.getButton() == 1 && !event.getState()
                    && mc.player.getActiveItemStack().getItem() instanceof ItemFood) {
                toggled = !toggled;
            }
        }));
    this.listeners.add(new LambdaListener<>(TickEvent.class, event -> {
        if (toggled) {
            if (mc.player != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemFood) {
                if (!mc.player.isHandActive()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                }
            } else {
                toggled = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
        }
    }));
    }

    @Override
    public void onDisable() {
        toggled = false;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }
}
