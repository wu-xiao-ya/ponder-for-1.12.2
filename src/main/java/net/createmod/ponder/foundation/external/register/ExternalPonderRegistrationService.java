package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.minecraft.util.ResourceLocation;

public final class ExternalPonderRegistrationService {

    private ExternalPonderRegistrationService() {
    }

    // ---- Public API (stable signatures) ----

    public static RegistrationOutcome registerLoadedScenes(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return registerLoadedScenesResult(definitions, helper).registrationOutcome();
    }

    public static ExternalSceneRegistrationResult registerLoadedScenesResult(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return ExternalSceneRegistrationService.registerLoadedScenesResult(definitions, helper);
    }

    public static RegistrationOutcome registerLoadedTags(ExternalDefinitionSet definitions,
        PonderTagRegistrationHelper<ResourceLocation> helper) {
        return registerLoadedTagsResult(definitions, helper).registrationOutcome();
    }

    public static ExternalTagRegistrationResult registerLoadedTagsResult(ExternalDefinitionSet definitions,
        PonderTagRegistrationHelper<ResourceLocation> helper) {
        return ExternalTagRegistrationService.registerLoadedTagsResult(definitions, helper);
    }

    public static RegistrationOutcome registerLoadedSharedText(ExternalDefinitionSet definitions,
        SharedTextRegistrationHelper helper) {
        return registerLoadedSharedTextResult(definitions, helper).registrationOutcome();
    }

    public static ExternalSharedTextRegistrationResult registerLoadedSharedTextResult(ExternalDefinitionSet definitions,
        SharedTextRegistrationHelper helper) {
        return ExternalSharedTextRegistrationService.registerLoadedSharedTextResult(definitions, helper);
    }

}
