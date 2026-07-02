package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.Set;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.foundation.external.definition.TagDefinition;
import net.minecraft.util.ResourceLocation;

final class ExternalTagDefinitionRegistrar {

    private ExternalTagDefinitionRegistrar() {
    }

    static Result register(RegistrationContext ctx, PonderTagRegistrationHelper<ResourceLocation> helper) {
        Set<ResourceLocation> registeredTags = new LinkedHashSet<ResourceLocation>();
        int registered = 0;
        int skipped = 0;
        int failed = 0;

        for (TagDefinition definition : ctx.definitions().tags()) {
            if (!registeredTags.add(definition.id())) {
                Ponder.LOGGER.warn("Skipping duplicate external ponder tag definition '{}'", definition.id());
                skipped++;
                continue;
            }

            try {
                registerOne(helper, definition);
                registered++;
            } catch (RuntimeException exception) {
                Ponder.LOGGER.error("Failed to register external ponder tag '{}' from {}", definition.id(),
                    ctx.sourcePath(definition.source()), exception);
                failed++;
            }
        }
        return new Result(registered, skipped, failed);
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

    record Result(int registered, int skipped, int failed) {
    }
}
