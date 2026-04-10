package net.createmod.ponder.foundation.external;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.util.ResourceLocation;

public class ExternalPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ExternalPonderScenes.registerLoadedScenes(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ExternalPonderScenes.registerLoadedTags(helper);
    }
}
