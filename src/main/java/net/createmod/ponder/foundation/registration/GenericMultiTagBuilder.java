package net.createmod.ponder.foundation.registration;

import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.util.ResourceLocation;

public class GenericMultiTagBuilder<T> implements MultiTagBuilder {

    public class Tag implements MultiTagBuilder.Tag<T> {
        private final PonderTagRegistrationHelper<T> helper;
        private final Iterable<ResourceLocation> tags;

        public Tag(PonderTagRegistrationHelper<T> helper, Iterable<ResourceLocation> tags) {
            this.helper = helper;
            this.tags = tags;
        }

        @Override
        public MultiTagBuilder.Tag<T> add(T component) {
            for (ResourceLocation tag : tags) {
                helper.addTagToComponent(component, tag);
            }
            return this;
        }
    }

    public class Component implements MultiTagBuilder.Component {
        private final PonderTagRegistrationHelper<T> helper;
        private final Iterable<T> components;

        public Component(PonderTagRegistrationHelper<T> helper, Iterable<T> components) {
            this.helper = helper;
            this.components = components;
        }

        @Override
        public MultiTagBuilder.Component add(ResourceLocation tag) {
            for (T component : components) {
                helper.addTagToComponent(component, tag);
            }
            return this;
        }
    }
}
