package net.createmod.ponder.foundation.registration;

import java.util.Arrays;
import java.util.function.Function;

import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.util.ResourceLocation;

public class DefaultPonderTagRegistrationHelper implements PonderTagRegistrationHelper<ResourceLocation> {

    private final String namespace;
    private final PonderTagRegistry tagRegistry;
    private final PonderLocalization localization;

    public DefaultPonderTagRegistrationHelper(String namespace, PonderTagRegistry tagRegistry,
        PonderLocalization localization) {
        this.namespace = namespace;
        this.tagRegistry = tagRegistry;
        this.localization = localization;
    }

    @Override
    public <T> PonderTagRegistrationHelper<T> withKeyFunction(Function<T, ResourceLocation> keyGen) {
        return new GenericPonderTagRegistrationHelper<T>(this, keyGen);
    }

    @Override
    public TagBuilder registerTag(ResourceLocation location) {
        return new PonderTagBuilder(location, this);
    }

    @Override
    public TagBuilder registerTag(String id) {
        return new PonderTagBuilder(new ResourceLocation(namespace, id), this);
    }

    void finishTagRegister(PonderTagBuilder builder) {
        localization.registerTag(builder.getId(), builder.getTitle(), builder.getDescription());

        PonderTag tag = new PonderTag(builder.getId(), builder.getTextureIconLocation(), builder.getItemIcon(),
            builder.getMainItem());
        tagRegistry.registerTag(tag);
        if (builder.shouldAddToIndex()) {
            tagRegistry.listTag(tag);
        }
    }

    @Override
    public void addTagToComponent(ResourceLocation component, ResourceLocation tag) {
        tagRegistry.addTagToComponent(tag, component);
    }

    @Override
    public MultiTagBuilder.Tag<ResourceLocation> addToTag(ResourceLocation tag) {
        return new GenericMultiTagBuilder<ResourceLocation>().new Tag(this, Arrays.asList(tag));
    }

    @Override
    public MultiTagBuilder.Tag<ResourceLocation> addToTag(ResourceLocation... tags) {
        return new GenericMultiTagBuilder<ResourceLocation>().new Tag(this, Arrays.asList(tags));
    }

    @Override
    public MultiTagBuilder.Component addToComponent(ResourceLocation component) {
        return new GenericMultiTagBuilder<ResourceLocation>().new Component(this, Arrays.asList(component));
    }

    @Override
    public MultiTagBuilder.Component addToComponent(ResourceLocation... components) {
        return new GenericMultiTagBuilder<ResourceLocation>().new Component(this, Arrays.asList(components));
    }
}
