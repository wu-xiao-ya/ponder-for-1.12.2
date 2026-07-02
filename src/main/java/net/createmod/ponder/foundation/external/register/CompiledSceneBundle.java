package net.createmod.ponder.foundation.external.register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.createmod.ponder.foundation.external.definition.SourceInfo;
import net.createmod.ponder.foundation.external.validate.ExternalJsonValidation;
import net.minecraft.util.ResourceLocation;

public record CompiledSceneBundle(
    SceneDefinition definition,
    SourceInfo source,
    PonderStoryBoard storyBoard,
    ResourceLocation schematicLocation,
    ResourceLocation[] tags,
    ResourceLocation[] orderBefore,
    ResourceLocation[] orderAfter
) {
    public CompiledSceneBundle {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        if (source == null) {
            source = SourceInfo.EMPTY;
        }
        if (storyBoard == null) {
            throw new IllegalArgumentException("storyBoard must not be null");
        }
        if (schematicLocation == null) {
            throw new IllegalArgumentException("schematicLocation must not be null");
        }
        if (tags == null) {
            tags = new ResourceLocation[0];
        }
        if (orderBefore == null) {
            orderBefore = new ResourceLocation[0];
        }
        if (orderAfter == null) {
            orderAfter = new ResourceLocation[0];
        }
        tags = Arrays.copyOf(tags, tags.length);
        orderBefore = Arrays.copyOf(orderBefore, orderBefore.length);
        orderAfter = Arrays.copyOf(orderAfter, orderAfter.length);
    }

    @Override
    public ResourceLocation[] tags() {
        return Arrays.copyOf(tags, tags.length);
    }

    @Override
    public ResourceLocation[] orderBefore() {
        return Arrays.copyOf(orderBefore, orderBefore.length);
    }

    @Override
    public ResourceLocation[] orderAfter() {
        return Arrays.copyOf(orderAfter, orderAfter.length);
    }

    public static CompiledSceneBundle compile(RegistrationContext ctx, SceneDefinition definition) {
        if (ctx == null) {
            throw new IllegalArgumentException("ctx must not be null");
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        SourceInfo source = ctx.source(definition.source());
        String namespace = source.namespace();
        return new CompiledSceneBundle(
            definition,
            source,
            ExternalStoryBoardBuilder.buildStoryBoard(definition),
            ExternalJsonValidation.parseLocation(definition.schematic(), namespace),
            copyLocations(definition.tags()),
            compileLocations(definition.orderBefore(), namespace),
            compileLocations(definition.orderAfter(), namespace)
        );
    }

    private static ResourceLocation[] copyLocations(List<ResourceLocation> locations) {
        if (locations.isEmpty()) {
            return new ResourceLocation[0];
        }
        return locations.toArray(new ResourceLocation[locations.size()]);
    }

    private static ResourceLocation[] compileLocations(List<String> values, String namespace) {
        if (values.isEmpty()) {
            return new ResourceLocation[0];
        }
        List<ResourceLocation> locations = new ArrayList<ResourceLocation>(values.size());
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            locations.add(ExternalJsonValidation.parseLocation(value, namespace));
        }
        return locations.toArray(new ResourceLocation[locations.size()]);
    }
}
