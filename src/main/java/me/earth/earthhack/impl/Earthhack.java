package me.earth.earthhack.impl;

import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.core.ducks.IMinecraft;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.managers.thread.GlobalExecutor;
import me.earth.earthhack.impl.modules.client.commands.Commands;
import me.earth.earthhack.impl.util.math.geocache.Sphere;
import me.earth.earthhack.impl.util.render.SplashScreenHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

public class Earthhack implements Globals
{
    private static final Logger LOGGER = LogManager.getLogger("ennui");
    public static final String NAME = "ennui";
    public static final String VERSION = "0.0.1";

    public static void preInit()
    {
        GlobalExecutor.EXECUTOR.submit(() -> Sphere.cacheSphere(LOGGER));
    }

    public static void init()
    {
        LOGGER.info("\n\nInitializing Ennui.");
        SplashScreenHelper.setSplashScreen("Initializing Ennui", 7);
        Display.setTitle(NAME + " - " + VERSION);
        Managers.load();
        LOGGER.info("Prefix is " + Commands.getPrefix());
        SplashScreenHelper.clear();
        LOGGER.info("\nEnnui initialized.\n");
    }

    public static void postInit()
    {
        // For Plugins if they need it.
    }
    
    public static Logger getLogger()
    {
        return LOGGER;
    }

    public static boolean isRunning()
    {
        return ((IMinecraft) mc).isEarthhackRunning();
    }

}