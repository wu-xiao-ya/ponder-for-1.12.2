package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.minecraft.util.ResourceLocation;

public final class ExternalPonderRegistrationService {

    private ExternalPonderRegistrationService() {
    }

    // ---- Public API (stable signatures) ----

    public static RegistrationOutcome registerLoadedScenes(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return ExternalSceneRegistrationService.registerLoadedScenes(definitions, helper);
    }

    public static RegistrationOutcome registerLoadedTags(ExternalDefinitionSet definitions,
        PonderTagRegistrationHelper<ResourceLocation> helper) {
        return ExternalTagRegistrationService.registerLoadedTags(definitions, helper);
    }

}
