package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.function.Consumer;
import java.util.Set;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.foundation.external.definition.TagDefinition;
import net.minecraft.util.ResourceLocation;

final class ExternalTagDefinitionRegistrar {

    private ExternalTagDefinitionRegistrar() {
    }

    static RegistrationOutcome register(RegistrationContext ctx, PonderTagRegistrationHelper<ResourceLocation> helper) {
        return register(ctx, helper, ExternalRegistrationDiagnostics.logger());
    }

    static RegistrationOutcome register(RegistrationContext ctx, PonderTagRegistrationHelper<ResourceLocation> helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        Consumer<ExternalRegistrationDiagnostic> sink = diagnosticSink != null ? diagnosticSink
            : ExternalRegistrationDiagnostics.logger();
        Set<ResourceLocation> registeredTags = new LinkedHashSet<ResourceLocation>();
        int registered = 0;
        int skipped = 0;
        int failed = 0;

        for (TagDefinition definition : ctx.definitions().tags()) {
            if (!registeredTags.add(definition.id())) {
                sink.accept(ExternalRegistrationDiagnostic.warn("tag", definition.id().toString(),
                    ctx.source(definition.source()), "duplicate definition skipped"));
                skipped++;
                continue;
            }

            try {
                registerOne(helper, definition);
                registered++;
            } catch (RuntimeException exception) {
                sink.accept(ExternalRegistrationDiagnostic.error("tag", definition.id().toString(),
                    ctx.source(definition.source()), "tag registration failed", exception));
                failed++;
            }
        }
        return new RegistrationOutcome(registered, skipped, failed);
    }

    private static void registerOne(PonderTagRegistrationHelper<ResourceLocation> helper,
        TagDefinition definition) {
        TagBuilder builder = helper.registerTag(definition.id())
            .title(definition.title())
            .description(definition.description());
        if (definition.icon() != null) {
            builder.icon(definition.icon());
        }
        if (!definition.itemStack().isEmpty()) {
            builder.item(definition.itemStack(), definition.useItemAsIcon(), definition.useItemAsMainItem());
        }
        if (definition.addToIndex()) {
            builder.addToIndex();
        }
        builder.register();
    }
}
