package tech.mania;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.mania.core.types.event.EventManager;
import tech.mania.core.types.module.ModuleManager;

public class Mania {

    private static final Mania instance = new Mania();

    public static final Logger LOGGER = LoggerFactory.getLogger("Mania-Client");

    private Mania() {
    }

    private EventManager eventManager;
    private ModuleManager moduleManager;

    public static void init() {
        LOGGER.info("Starting mania client");
        instance.eventManager = new EventManager();
        instance.moduleManager = new ModuleManager();
    }

    public static void shutdown() {

    }

    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    public static ModuleManager getModuleManager() {
        return instance.moduleManager;
    }
}
