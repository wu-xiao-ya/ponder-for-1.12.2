package net.createmod.ponder.foundation.external.register;

import java.util.function.Consumer;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.minecraft.util.ResourceLocation;

public final class ExternalTagRegistrationService {

    private ExternalTagRegistrationService() {
    }

    public static RegistrationOutcome registerLoadedTags(ExternalDefinitionSet definitions,
        PonderTagRegistrationHelper<ResourceLocation> helper) {
        return registerTags(RegistrationContext.of(definitions), helper, ExternalRegistrationDiagnostics.logger());
    }

    private static RegistrationOutcome registerTags(RegistrationContext ctx,
        PonderTagRegistrationHelper<ResourceLocation> helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        ExternalTagDefinitionRegistrar.Result tagResult = ExternalTagDefinitionRegistrar.register(ctx, helper,
            diagnosticSink);
        int assignmentCount = ExternalComponentTagRegistrar.register(ctx.definitions(), helper);
        return new RegistrationOutcome(tagResult.registered() + assignmentCount, tagResult.skipped(),
            tagResult.failed());
    }
}
