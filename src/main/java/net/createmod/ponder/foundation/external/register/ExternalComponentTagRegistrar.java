package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.minecraft.util.ResourceLocation;

final class ExternalComponentTagRegistrar {

    private ExternalComponentTagRegistrar() {
    }

    static RegistrationOutcome register(RegistrationContext ctx, PonderTagRegistrationHelper<ResourceLocation> helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        Consumer<ExternalRegistrationDiagnostic> sink = diagnosticSink != null ? diagnosticSink
            : ExternalRegistrationDiagnostics.logger();
        Set<String> assignments = new LinkedHashSet<String>();
        int assignmentCount = 0;
        int failed = 0;
        for (SceneDefinition definition : ctx.definitions().scenes()) {
            for (ComponentDefinition component : definition.components()) {
                for (ResourceLocation tag : definition.componentTags()) {
                    String assignmentKey = component.componentId() + "|" + tag;
                    if (!assignments.add(assignmentKey)) {
                        continue;
                    }
                    try {
                        helper.addTagToComponent(component.componentId(), tag);
                        assignmentCount++;
                    } catch (RuntimeException exception) {
                        sink.accept(ExternalRegistrationDiagnostic.error("component tag", assignmentKey,
                            ctx.source(definition.source()), "component tag assignment failed", exception));
                        failed++;
                    }
                }
            }
        }
        return new RegistrationOutcome(assignmentCount, 0, failed);
    }
}
