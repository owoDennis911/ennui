package me.earth.earthhack.impl.modules.misc.alerts;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.impl.event.events.misc.TickEvent;
import me.earth.earthhack.impl.event.listeners.LambdaListener;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.text.ChatIDs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class Alerts extends Module {
    protected final List<EntityPlayer> inRangePlayers = new ArrayList<>();

    protected final Setting<Boolean> totems = register(new BooleanSetting("PopCounter", true));
    protected final Setting<Boolean> deaths = register(new BooleanSetting("Kills", true));
    protected final Setting<Boolean> visualrange = register(new BooleanSetting("VisualRange", true));

    public Alerts() {
        super("Alerts", Category.Misc);
        this.listeners.add(new ListenerTotems(this));
        this.listeners.add(new ListenerDeath(this));
        this.listeners.add(new LambdaListener<>(TickEvent.class, this::onTick));
        this.setData(new AlertsData(this));
    }

    public void onPop(Entity player, int totemPops) {
        if (totems.getValue()) {
            String message = ChatFormatting.WHITE +
                    player.getName() + ChatFormatting.GRAY
                    + " popped "
                    + ChatFormatting.WHITE
                    + totemPops
                    + ChatFormatting.GRAY
                    + " totem"
                    + (totemPops == 1 ? "." : "s.");

            Managers.CHAT.sendDeleteMessage(message,
                    player.getName(),
                    ChatIDs.TOTEM_POPS);
        }
    }

    public void onDeath(Entity player, int totemPops) {
        if (deaths.getValue()) {
            String message = ChatFormatting.WHITE +
                    player.getName() + ChatFormatting.GRAY
                    + " died after popping "
                    + ChatFormatting.WHITE
                    + totemPops
                    + ChatFormatting.GRAY
                    + " totem"
                    + (totemPops == 1 ? "." : "s.");

            Managers.CHAT.sendDeleteMessage(message,
                    player.getName(),
                    ChatIDs.TOTEM_POPS);
        }
    }
    public void onTick(TickEvent event) {
        if (!event.isSafe() && visualrange.getValue())
            return;

        List<EntityPlayer> mclist = new ArrayList(mc.world.playerEntities);

        for (EntityPlayer player : mclist) {
            if (player != mc.player) {
                if (!inRangePlayers.contains(player)) {
                    inRangePlayers.add(player);
                    String message = (ChatFormatting.LIGHT_PURPLE + player.getName() + ChatFormatting.GRAY + " has entered your visual range.");
                    Managers.CHAT.sendDeleteMessage(message,
                            player.getName(),
                            ChatIDs.MODULE);

                }
            }
        }

        List<EntityPlayer> toRemove = new ArrayList<>();

        for (EntityPlayer player : inRangePlayers) {
            if (player != mc.player) {
                if (!mclist.contains(player)) {
                    toRemove.add(player);
                    String message = (ChatFormatting.LIGHT_PURPLE + player.getName() + ChatFormatting.GRAY + " has left your visual range.");
                    Managers.CHAT.sendDeleteMessage(message,
                            player.getName(),
                            ChatIDs.MODULE);

                }
            }
        }

        inRangePlayers.removeAll(toRemove);
    }
}

