package net.createmod.ponder;

import net.createmod.ponder.Tags;
import net.createmod.ponder.command.PonderCommand;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.SidedProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class PonderBackportMod {

    @SidedProxy(clientSide = "net.createmod.ponder.client.ClientProxy",
        serverSide = "net.createmod.ponder.CommonProxy")
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Ponder.init();
        proxy.preInit();
        LOGGER.info("Bootstrapped {} with {} plugins", Tags.MOD_NAME, Integer.valueOf(PonderIndex.getPluginCount()));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new PonderCommand());
        LOGGER.info("Registered /ponder server command");
    }
}
