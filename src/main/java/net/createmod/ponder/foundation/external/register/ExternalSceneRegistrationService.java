package net.createmod.ponder.foundation.external.register;

import java.util.function.Consumer;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SourceInfo;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.minecraft.util.ResourceLocation;

public final class ExternalSceneRegistrationService {

    private ExternalSceneRegistrationService() {
    }

    public static RegistrationOutcome registerLoadedScenes(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return registerLoadedScenesResult(definitions, helper).registrationOutcome();
    }

    public static ExternalSceneRegistrationResult registerLoadedScenesResult(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return registerScenes(RegistrationContext.of(definitions), helper, ExternalRegistrationDiagnostics.logger());
    }

    private static ExternalSceneRegistrationResult registerScenes(RegistrationContext ctx,
        PonderSceneRegistrationHelper<ResourceLocation> helper,
        Consumer<ExternalRegistrationDiagnostic> diagnosticSink) {
        Consumer<ExternalRegistrationDiagnostic> sink = diagnosticSink != null ? diagnosticSink
            : ExternalRegistrationDiagnostics.logger();
        int sceneDefinitions = 0;
        int compiledBundles = 0;
        int componentBindings = 0;
        int compileFailures = 0;
        int registered = 0;
        int skipped = 0;
        int failed = 0;
        for (SceneDefinition definition : ctx.definitions().scenes()) {
            sceneDefinitions++;
            SourceInfo source = ctx.source(definition.source());
            CompiledSceneBundle bundle;
            try {
                bundle = CompiledSceneBundle.compile(ctx, definition);
                compiledBundles++;
                componentBindings += definition.components().size();
            } catch (RuntimeException exception) {
                sink.accept(ExternalRegistrationDiagnostic.error("scene", definition.sceneId().toString(), source,
                    "scene compilation failed", exception));
                compileFailures++;
                continue;
            }

            for (ComponentDefinition component : definition.components()) {
                ResourceLocation currentComponentId = component.componentId();
                try {
                    int entryCountBefore = PonderIndex.getSceneAccess().getRegisteredEntries().size();
                    StoryBoardEntry entry = helper.addStoryBoard(component.componentId(),
                        bundle.schematicLocation(),
                        bundle.storyBoard(),
                        bundle.tags());
                    int entryCountAfter = PonderIndex.getSceneAccess().getRegisteredEntries().size();
                    if (entryCountAfter == entryCountBefore) {
                        skipped++;
                        continue;
                    }
                    PonderIndex.registerComponentMatcher(component.componentId(), component.componentMatcher());

                    for (ResourceLocation before : bundle.orderBefore()) {
                        orderScene(entry, before, true);
                    }
                    for (ResourceLocation after : bundle.orderAfter()) {
                        orderScene(entry, after, false);
                    }
                    registered++;
                } catch (RuntimeException exception) {
                    sink.accept(ExternalRegistrationDiagnostic.error("scene", definition.sceneId().toString(), source,
                        "component " + currentComponentId + " registration failed", exception));
                    failed++;
                }
            }
        }
        return new ExternalSceneRegistrationResult(
            new ExternalSceneCompileSummary(sceneDefinitions, compiledBundles, componentBindings, compileFailures),
            new RegistrationOutcome(registered, skipped, failed)
        );
    }

    private static void orderScene(StoryBoardEntry entry, ResourceLocation location, boolean before) {
        if (before) {
            entry.orderBefore(location.getNamespace(), location.getPath());
            return;
        }
        entry.orderAfter(location.getNamespace(), location.getPath());
    }
}
