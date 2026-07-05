package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.function.Consumer;
import java.util.Set;

import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SharedTextDefinition;

public final class ExternalSharedTextRegistrationService {

    private ExternalSharedTextRegistrationService() {
    }

    public static RegistrationOutcome registerLoadedSharedText(ExternalDefinitionSet definitions,
        SharedTextRegistrationHelper helper) {
        return registerLoadedSharedTextResult(definitions, helper).registrationOutcome();
    }

    public static ExternalSharedTextRegistrationResult registerLoadedSharedTextResult(ExternalDefinitionSet definitions,
        SharedTextRegistrationHelper helper) {
        ExternalRegistrationDiagnostics diagnostics = new ExternalRegistrationDiagnostics();
        RegistrationOutcome outcome = registerSharedTexts(RegistrationContext.of(definitions), helper, diagnostics);
        return new ExternalSharedTextRegistrationResult(outcome, diagnostics.report());
    }

    private static RegistrationOutcome registerSharedTexts(RegistrationContext ctx,
        SharedTextRegistrationHelper helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        Consumer<ExternalRegistrationDiagnostic> sink = diagnosticSink != null ? diagnosticSink
            : ExternalRegistrationDiagnostics.logger();
        Set<String> registeredKeys = new LinkedHashSet<String>();
        int count = 0;
        int skipped = 0;
        int failed = 0;

        for (SharedTextDefinition definition : ctx.definitions().sharedTexts()) {
            if (!registeredKeys.add(definition.key())) {
                sink.accept(ExternalRegistrationDiagnostic.warn("shared text", definition.key(),
                    ctx.source(definition.source()), "duplicate definition skipped"));
                skipped++;
                continue;
            }

            try {
                helper.registerSharedText(definition.key(), definition.text());
                count++;
            } catch (RuntimeException exception) {
                sink.accept(ExternalRegistrationDiagnostic.error("shared text", definition.key(),
                    ctx.source(definition.source()), "shared text registration failed", exception));
                failed++;
            }
        }

        return new RegistrationOutcome(count, skipped, failed);
    }
}
