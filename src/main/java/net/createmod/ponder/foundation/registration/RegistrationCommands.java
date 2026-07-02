package net.createmod.ponder.foundation.registration;

import java.util.List;
import java.util.function.Predicate;

import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.minecraft.util.ResourceLocation;

final class RegistrationCommands {

    private RegistrationCommands() {
    }

    static RegistrationCommand clearSceneRegistry(PonderSceneRegistry registry) {
        return new ClearSceneRegistryCommand(registry);
    }

    static RegistrationCommand clearTagRegistry(PonderTagRegistry registry) {
        return new ClearTagRegistryCommand(registry);
    }

    static RegistrationCommand clearLocalization(PonderLocalization localization) {
        return new ClearLocalizationCommand(localization);
    }

    static RegistrationCommand clearSpecificLocalization(PonderLocalization localization) {
        return new ClearSpecificLocalizationCommand(localization);
    }

    static RegistrationCommand registerScene(PonderSceneRegistry registry, StoryBoardEntry entry) {
        return new RegisterSceneCommand(registry, entry);
    }

    static RegistrationCommand registerMatcher(PonderSceneRegistry registry, ResourceLocation componentId,
        PonderComponentMatcher matcher) {
        return new RegisterMatcherCommand(registry, componentId, matcher);
    }

    static RegistrationCommand registerTag(PonderTagRegistry registry, PonderTag tag) {
        return new RegisterTagCommand(registry, tag);
    }

    static RegistrationCommand listTag(PonderTagRegistry registry, PonderTag tag) {
        return new ListTagCommand(registry, tag);
    }

    static RegistrationCommand linkComponentTag(PonderTagRegistry registry, ResourceLocation tag,
        ResourceLocation component) {
        return new LinkComponentTagCommand(registry, tag, component);
    }

    static RegistrationCommand registerSharedText(PonderLocalization localization, ResourceLocation key, String enUs) {
        return new RegisterSharedTextCommand(localization, key, enUs);
    }

    static RegistrationCommand registerTagText(PonderLocalization localization, ResourceLocation key, String title,
        String description) {
        return new RegisterTagTextCommand(localization, key, title, description);
    }

    static RegistrationCommand registerSpecificText(PonderLocalization localization, ResourceLocation sceneId,
        String key, String enUs) {
        return new RegisterSpecificTextCommand(localization, sceneId, key, enUs);
    }

    static RegistrationCommand setSceneIndexExclusions(PonderSceneRegistry registry,
        List<Predicate<ResourceLocation>> exclusions) {
        return new SetSceneIndexExclusionsCommand(registry, exclusions);
    }

    static RegistrationCommand setTagIndexExclusions(PonderTagRegistry registry,
        List<Predicate<ResourceLocation>> exclusions) {
        return new SetTagIndexExclusionsCommand(registry, exclusions);
    }

    static RegistrationCommand finishSceneRegistration(PonderSceneRegistry registry) {
        return new FinishSceneRegistrationCommand(registry);
    }

    static RegistrationCommand finishTagRegistration(PonderTagRegistry registry) {
        return new FinishTagRegistrationCommand(registry);
    }

    sealed interface RegistrationCommand permits ClearSceneRegistryCommand, ClearTagRegistryCommand,
        ClearLocalizationCommand, ClearSpecificLocalizationCommand, RegisterSceneCommand, RegisterMatcherCommand,
        RegisterTagCommand, ListTagCommand, LinkComponentTagCommand, RegisterSharedTextCommand, RegisterTagTextCommand,
        RegisterSpecificTextCommand, SetSceneIndexExclusionsCommand, SetTagIndexExclusionsCommand,
        FinishSceneRegistrationCommand, FinishTagRegistrationCommand {

        void execute();
    }

    record ClearSceneRegistryCommand(PonderSceneRegistry registry) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.clearRegistryState();
        }
    }

    record ClearTagRegistryCommand(PonderTagRegistry registry) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.clearRegistryState();
        }
    }

    record ClearLocalizationCommand(PonderLocalization localization) implements RegistrationCommand {

        @Override
        public void execute() {
            localization.clearAllState();
        }
    }

    record ClearSpecificLocalizationCommand(PonderLocalization localization) implements RegistrationCommand {

        @Override
        public void execute() {
            localization.clearSpecificState();
        }
    }

    record RegisterSceneCommand(PonderSceneRegistry registry, StoryBoardEntry entry) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.addStoryBoardState(entry);
        }
    }

    record RegisterMatcherCommand(PonderSceneRegistry registry, ResourceLocation componentId,
        PonderComponentMatcher matcher) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.registerComponentMatcherState(componentId, matcher);
        }
    }

    record RegisterTagCommand(PonderTagRegistry registry, PonderTag tag) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.registerTagState(tag);
        }
    }

    record ListTagCommand(PonderTagRegistry registry, PonderTag tag) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.listTagState(tag);
        }
    }

    record LinkComponentTagCommand(PonderTagRegistry registry, ResourceLocation tag, ResourceLocation component)
        implements RegistrationCommand {

        @Override
        public void execute() {
            registry.addTagToComponentState(tag, component);
        }
    }

    record RegisterSharedTextCommand(PonderLocalization localization, ResourceLocation key, String enUs)
        implements RegistrationCommand {

        @Override
        public void execute() {
            localization.registerSharedState(key, enUs);
        }
    }

    record RegisterTagTextCommand(PonderLocalization localization, ResourceLocation key, String title,
        String description) implements RegistrationCommand {

        @Override
        public void execute() {
            localization.registerTagState(key, title, description);
        }
    }

    record RegisterSpecificTextCommand(PonderLocalization localization, ResourceLocation sceneId, String key,
        String enUs) implements RegistrationCommand {

        @Override
        public void execute() {
            localization.registerSpecificState(sceneId, key, enUs);
        }
    }

    record SetSceneIndexExclusionsCommand(PonderSceneRegistry registry,
        List<Predicate<ResourceLocation>> exclusions) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.setIndexExclusionsState(exclusions);
        }
    }

    record SetTagIndexExclusionsCommand(PonderTagRegistry registry,
        List<Predicate<ResourceLocation>> exclusions) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.setIndexExclusionsState(exclusions);
        }
    }

    record FinishSceneRegistrationCommand(PonderSceneRegistry registry) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.finishRegistration();
        }
    }

    record FinishTagRegistrationCommand(PonderTagRegistry registry) implements RegistrationCommand {

        @Override
        public void execute() {
            registry.finishRegistration();
        }
    }
}
