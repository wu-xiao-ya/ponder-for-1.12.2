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
        RegistrationOutcome tagResult = ExternalTagDefinitionRegistrar.register(ctx, helper, diagnosticSink);
        int assignmentCount = ExternalComponentTagRegistrar.register(ctx.definitions(), helper);
        return tagResult.merge(new RegistrationOutcome(assignmentCount, 0, 0));
    }
}
