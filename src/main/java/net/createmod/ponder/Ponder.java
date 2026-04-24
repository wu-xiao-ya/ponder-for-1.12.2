package net.createmod.ponder;

import java.util.Random;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.content.BasePonderPlugin;
import net.createmod.ponder.foundation.content.DebugPonderPlugin;
import net.createmod.ponder.foundation.content.ThermalExpansionPonderPlugin;
import net.createmod.ponder.foundation.external.ExternalPonderPlugin;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Ponder {

    public static final String MOD_ID = Reference.MOD_ID;
    public static final String MOD_NAME = Reference.MOD_NAME;
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final Random RANDOM = new Random();

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

        PonderIndex.addPlugin(new BasePonderPlugin());
        PonderIndex.addPlugin(new DebugPonderPlugin());
        PonderIndex.addPlugin(new ThermalExpansionPonderPlugin());
        PonderIndex.addPlugin(new ExternalPonderPlugin());
        PonderIndex.reload();
        initialized = true;
    }
}
