package net.createmod.ponder.foundation.external;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.parse.ExternalPonderSceneParser;
import net.createmod.ponder.foundation.external.register.ExternalPonderRegistrationService;
import net.createmod.ponder.foundation.external.register.ExternalSharedTextRegistrationService;
import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.minecraft.util.ResourceLocation;

public class ExternalPonderPlugin implements PonderPlugin {

    private ExternalDefinitionSet cachedDefinitions;

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        logOutcome("scenes", ExternalPonderRegistrationService.registerLoadedScenes(loadDefinitions(), helper));
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        logOutcome("tags", ExternalPonderRegistrationService.registerLoadedTags(loadDefinitions(), helper));
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        try {
            logOutcome("shared text",
                ExternalSharedTextRegistrationService.registerLoadedSharedText(loadDefinitions(), helper));
        } finally {
            cachedDefinitions = null;
        }
    }

    private ExternalDefinitionSet loadDefinitions() {
        if (cachedDefinitions == null) {
            cachedDefinitions = ExternalPonderSceneParser.loadDefinitions();
        }
        return cachedDefinitions;
    }

    private void logOutcome(String channel, RegistrationOutcome outcome) {
        if (outcome == null) {
            return;
        }
        Ponder.LOGGER.info("External ponder {} registration: {} registered, {} skipped, {} failed",
            channel, Integer.valueOf(outcome.registered()), Integer.valueOf(outcome.skipped()),
            Integer.valueOf(outcome.failed()));
    }
}
