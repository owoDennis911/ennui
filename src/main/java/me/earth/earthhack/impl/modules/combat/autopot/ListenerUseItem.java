package me.earth.earthhack.impl.modules.combat.autopot;

import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import me.earth.earthhack.impl.util.math.rotation.RotationUtil;
import me.earth.earthhack.impl.util.network.NetworkUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

public final class ListenerUseItem extends ModuleListener<AutoPot, PacketEvent.Send<CPacketPlayerTryUseItem>> {
    private boolean sending = false;

    public ListenerUseItem(AutoPot module) {
        super(module, PacketEvent.Send.class, CPacketPlayerTryUseItem.class);
    }

    @Override
    public void invoke(PacketEvent.Send<CPacketPlayerTryUseItem> event) {
        CPacketPlayerTryUseItem p = event.getPacket();
        if (sending || event.isCancelled()) {
            return;
        }

        EntityPlayerSP player = mc.player;

        if (player != null && player.isHandActive()) {
            player.getActiveHand();
            ItemStack heldItemStack = player.getHeldItem(player.getActiveHand());

            if (heldItemStack.getItem() == Items.SPLASH_POTION) {
                event.setCancelled(true);
                module.justCancelled = true;

                if (module.health.getValue() > player.getHealth()
                        || mc.world.getEntitiesWithinAABB(EntityItem.class, RotationUtil.getRotationPlayer().getEntityBoundingBox()).isEmpty()) {
                    sending = true;
                    NetworkUtil.send(new CPacketPlayerTryUseItem(p.getHand()));
                    sending = false;
                }
            }
        }
    }
}
