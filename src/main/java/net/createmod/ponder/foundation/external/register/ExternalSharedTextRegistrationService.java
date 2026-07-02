package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.Set;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SharedTextDefinition;

public final class ExternalSharedTextRegistrationService {

    private ExternalSharedTextRegistrationService() {
    }

    public static RegistrationOutcome registerLoadedSharedText(ExternalDefinitionSet definitions,
        SharedTextRegistrationHelper helper) {
        return registerSharedTexts(RegistrationContext.of(definitions), helper);
    }

    private static RegistrationOutcome registerSharedTexts(RegistrationContext ctx,
        SharedTextRegistrationHelper helper) {
        Set<String> registeredKeys = new LinkedHashSet<String>();
        int count = 0;
        int skipped = 0;
        int failed = 0;

        for (SharedTextDefinition definition : ctx.definitions().sharedTexts()) {
            if (!registeredKeys.add(definition.key())) {
                Ponder.LOGGER.warn("Skipping duplicate external ponder shared text definition '{}'", definition.key());
                skipped++;
                continue;
            }

            try {
                helper.registerSharedText(definition.key(), definition.text());
                count++;
            } catch (RuntimeException exception) {
                Ponder.LOGGER.error("Failed to register external ponder shared text '{}' from {}", definition.key(),
                    ctx.sourcePath(definition.source()), exception);
                failed++;
            }
        }

        return new RegistrationOutcome(count, skipped, failed);
    }
}
