package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.minecraft.util.ResourceLocation;

public final class ExternalSceneRegistrationService {

    private ExternalSceneRegistrationService() {
    }

    public static RegistrationOutcome registerLoadedScenes(ExternalDefinitionSet definitions,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        return registerScenes(RegistrationContext.of(definitions), helper);
    }

    private static RegistrationOutcome registerScenes(RegistrationContext ctx,
        PonderSceneRegistrationHelper<ResourceLocation> helper) {
        int registered = 0;
        int failed = 0;
        for (SceneDefinition definition : ctx.definitions().scenes()) {
            try {
                CompiledSceneBundle bundle = CompiledSceneBundle.compile(ctx, definition);
                for (ComponentDefinition component : definition.components()) {
                    StoryBoardEntry entry = helper.addStoryBoard(component.componentId(),
                        bundle.schematicLocation(),
                        bundle.storyBoard(),
                        bundle.tags());
                    PonderIndex.registerComponentMatcher(component.componentId(), component.componentMatcher());

                    for (ResourceLocation before : bundle.orderBefore()) {
                        orderScene(entry, before, true);
                    }
                    for (ResourceLocation after : bundle.orderAfter()) {
                        orderScene(entry, after, false);
                    }
                    registered++;
                }
            } catch (RuntimeException exception) {
                Ponder.LOGGER.error("Failed to register external ponder scene '{}:{}' from {}",
                    ctx.namespace(definition.source()), definition.sceneId(), ctx.sourcePath(definition.source()),
                    exception);
                failed++;
            }
        }
        return new RegistrationOutcome(registered, 0, failed);
    }

    private static void orderScene(StoryBoardEntry entry, ResourceLocation location, boolean before) {
        if (before) {
            entry.orderBefore(location.getNamespace(), location.getPath());
            return;
        }
        entry.orderAfter(location.getNamespace(), location.getPath());
    }
}
