package net.createmod.ponder;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Ponder {

    public static final String MOD_ID = Reference.MOD_ID;
    public static final String MOD_NAME = Reference.MOD_NAME;
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME + "-forge-server-shim");

    private static boolean initialized;

    private Ponder() {
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("Initialized Forge dedicated-server compatibility shim");
    }
}
