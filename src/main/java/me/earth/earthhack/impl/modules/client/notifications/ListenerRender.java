package me.earth.earthhack.impl.modules.client.notifications;

import me.earth.earthhack.impl.event.events.render.Render2DEvent;
import me.earth.earthhack.impl.event.listeners.ModuleListener;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class ListenerRender extends ModuleListener<Notifications, Render2DEvent>
{
    private static final long SLIDE_IN_TIME = 200;
    private static final int SPACING = 10;

    public ListenerRender(Notifications module)
    {
        super(module, Render2DEvent.class);
    }

    @Override
    public void invoke(Render2DEvent event)
    {
        if (module.notifMode.getValue() == Notifications.NotifMode.HUD ||
                module.notifMode.getValue() == Notifications.NotifMode.BOTH)
        {
            GL11.glPushMatrix();
            int baseX = module.notifPosX.getValue();
            int baseY = module.notifPosY.getValue();
            int slideOffset = module.notifSlideOffset.getValue();

            int index = 0;
            for (Notifications.HudNotification notif : module.getHudNotifications())
            {
                int finalY = baseY + (index * SPACING);
                long elapsed = System.currentTimeMillis() - notif.creationTime;
                int y;
                if (elapsed < SLIDE_IN_TIME)
                {
                    float progress = elapsed / (float) SLIDE_IN_TIME;
                    int startY = baseY - slideOffset;
                    y = (int)(startY + (finalY - startY) * progress);
                }
                else
                {
                    y = finalY;
                }
                Minecraft.getMinecraft().fontRenderer.drawString(notif.message, baseX, y, 0xFFFFFF);
                index++;
            }

            GL11.glPopMatrix();
        }
    }
}