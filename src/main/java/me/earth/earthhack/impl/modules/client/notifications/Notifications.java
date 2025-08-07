package me.earth.earthhack.impl.modules.client.notifications;

import me.earth.earthhack.api.event.bus.EventListener;
import me.earth.earthhack.api.event.bus.instance.Bus;
import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.event.events.client.PostInitEvent;
import me.earth.earthhack.impl.gui.visibility.Visibilities;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.text.ChatIDs;
import me.earth.earthhack.impl.util.text.TextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notifications extends Module
{
    protected final Setting<Boolean> modules =
            register(new BooleanSetting("Modules", true));
    protected final Setting<Boolean> configure =
            register(new BooleanSetting("Show-Modules", true));
    protected final Setting<Category.CategoryEnum> categories =
            register(new EnumSetting<>("Categories", Category.CategoryEnum.Combat));


    protected final Setting<NotifMode> notifMode =
            register(new EnumSetting<>("NotifMode", NotifMode.CHAT));
    protected final Setting<Integer> notifPosX =
            register(new NumberSetting<>("NotifPosX", 10, 0, 1920));
    protected final Setting<Integer> notifPosY =
            register(new NumberSetting<>("NotifPosY", 10, 0, 1080));
    protected final Setting<Integer> notifSlideOffset =
            register(new NumberSetting<>("SlideOffset", 20, 0, 100));
    protected final Setting<Integer> notifDisplayTime =
            register(new NumberSetting<>("NotifDisplayTime", 3000, 500, 10000));

    protected final Map<Module, Setting<Boolean>> announceMap = new HashMap<>();
    private final List<HudNotification> hudNotifications = new ArrayList<>();

    public Notifications()
    {
        super("Notifications", Category.Client);
        this.setData(new NotificationData(this));
        Bus.EVENT_BUS.register(
                new EventListener<PostInitEvent>(PostInitEvent.class)
                {
                    @Override
                    public void invoke(PostInitEvent event)
                    {
                        createSettings();
                    }
                }
        );
        Bus.EVENT_BUS.register(new ListenerRender(this));
    }

    private void createSettings()
    {
        announceMap.clear();
        Visibilities.VISIBILITY_MANAGER
                .registerVisibility(categories, configure::getValue);

        for (Module module : Managers.MODULES.getRegistered())
        {
            Setting<Boolean> enabled =
                    module.getSetting("Enabled", BooleanSetting.class);

            if (enabled == null)
            {
                continue;
            }
            enabled.addObserver(event ->
            {
                if (isEnabled()
                        && !event.isCancelled()
                        && modules.getValue()
                        && announceMap.get(module).getValue())
                {
                    onToggleModule(
                            (Module) event.getSetting().getContainer(),
                            event.getValue());
                }
            });
            String name = module.getName();
            if (this.getSetting(name) != null)
            {
                name = "Show" + name;
            }
            Setting<Boolean> setting = register(new BooleanSetting(name, false));
            announceMap.put(module, setting);
            Visibilities.VISIBILITY_MANAGER.registerVisibility(setting, () ->
                    configure.getValue()
                            && categories.getValue().toValue() == module.getCategory());

            this.getData().settingDescriptions().put(
                    setting,
                    "Announce Toggling of " + name + "?");
        }
    }

    protected void onToggleModule(Module module, boolean enabled)
    {
        Setting<Boolean> setting = announceMap.get(module);
        if (setting != null && setting.getValue())
        {
            String message = TextColor.WHITE
                    + module.getDisplayName()
                    + (enabled
                    ? TextColor.GREEN + " enabled."
                    : TextColor.RED + " disabled.");

            NotifMode mode = notifMode.getValue();
            if (mode == NotifMode.CHAT || mode == NotifMode.BOTH)
            {
                mc.addScheduledTask(() ->
                        Managers.CHAT.sendDeleteMessage(
                                message, module.getName(), ChatIDs.MODULE));
            }
            if (mode == NotifMode.HUD || mode == NotifMode.BOTH)
            {
                addHudNotification(module, message);
            }
        }
    }

    public void addHudNotification(Module module, String message)
    {
        for (HudNotification notif : hudNotifications)
        {
            if (notif.moduleName.equals(module.getName()))
            {
                notif.message = message;
                return;
            }
        }
        hudNotifications.add(new HudNotification(module.getName(), message));
    }

    public List<HudNotification> getHudNotifications()
    {
        long now = System.currentTimeMillis();
        hudNotifications.removeIf(
                notif -> now - notif.creationTime > notifDisplayTime.getValue());
        return new ArrayList<>(hudNotifications);
    }

    public static class HudNotification
    {
        public final String moduleName;
        public String message;
        public long creationTime;

        public HudNotification(String moduleName, String message)
        {
            this.moduleName = moduleName;
            this.message = message;
            this.creationTime = System.currentTimeMillis();
        }
    }
    public enum NotifMode
    {
        CHAT,
        HUD,
        BOTH,
        NONE
    }
}
