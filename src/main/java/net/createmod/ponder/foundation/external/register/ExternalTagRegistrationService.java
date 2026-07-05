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
        return registerLoadedTagsResult(definitions, helper).registrationOutcome();
    }

    public static ExternalTagRegistrationResult registerLoadedTagsResult(ExternalDefinitionSet definitions,
        PonderTagRegistrationHelper<ResourceLocation> helper) {
        ExternalRegistrationDiagnostics diagnostics = new ExternalRegistrationDiagnostics();
        RegistrationOutcome outcome = registerTags(RegistrationContext.of(definitions), helper, diagnostics);
        return new ExternalTagRegistrationResult(outcome, diagnostics.report());
    }

    private static RegistrationOutcome registerTags(RegistrationContext ctx,
        PonderTagRegistrationHelper<ResourceLocation> helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        RegistrationOutcome tagResult = ExternalTagDefinitionRegistrar.register(ctx, helper, diagnosticSink);
        RegistrationOutcome assignmentResult = ExternalComponentTagRegistrar.register(ctx, helper, diagnosticSink);
        return tagResult.merge(assignmentResult);
    }
}
