package net.createmod.ponder.foundation.content;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

public class ThermalExpansionPonderPlugin implements PonderPlugin {

    private static final String MOD_ID = "thermalexpansion";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        if (!Loader.isModLoaded(MOD_ID)) {
            return;
        }
        ThermalExpansionScenes.registerAll(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        if (!Loader.isModLoaded(MOD_ID)) {
            return;
        }

        helper.registerTag(ThermalExpansionScenes.THERMAL_MACHINES_TAG)
            .title("热力膨胀机器")
            .description("基于真实 GUI 的热力膨胀机器交互演示。")
            .item(Blocks.FURNACE)
            .addToIndex()
            .register();

        for (ResourceLocation machineId : ThermalExpansionScenes.getAllMachineIds()) {
            helper.addTagToComponent(machineId, ThermalExpansionScenes.THERMAL_MACHINES_TAG);
        }
    }
}
