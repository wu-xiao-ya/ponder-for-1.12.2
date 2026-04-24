package net.createmod.ponder;

import java.util.Map;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
    acceptableRemoteVersions = "*")
public class PonderBackportMod {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Ponder.init();
        Ponder.LOGGER.info("Loaded Ponder Forge server shim. Client-side Ponder UI is supplied by the client jar.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PonderIndex.reload();
    }

    @NetworkCheckHandler
    public boolean checkRemote(Map<String, String> remoteVersions, Side side) {
        return true;
    }
}
