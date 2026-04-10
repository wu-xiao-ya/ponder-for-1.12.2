package net.createmod.ponder.foundation.registration;

import java.util.function.Consumer;

import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.util.ResourceLocation;

public class GenericMultiSceneBuilder<T> implements MultiSceneBuilder {

    private final Iterable<? extends T> components;
    private final PonderSceneRegistrationHelper<T> helper;

    public GenericMultiSceneBuilder(PonderSceneRegistrationHelper<T> helper, Iterable<? extends T> components) {
        this.helper = helper;
        this.components = components;
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard) {
        return addStoryBoard(schematicLocation, storyBoard, new Consumer<StoryBoardEntry>() {
            @Override
            public void accept(StoryBoardEntry entry) {
            }
        });
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
        final ResourceLocation... tags) {
        return addStoryBoard(schematicLocation, storyBoard, new Consumer<StoryBoardEntry>() {
            @Override
            public void accept(StoryBoardEntry entry) {
                entry.highlightTags(tags);
            }
        });
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
        Consumer<StoryBoardEntry> extras) {
        for (T component : components) {
            extras.accept(helper.addStoryBoard(component, schematicLocation, storyBoard));
        }
        return this;
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard);
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, ResourceLocation... tags) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard, tags);
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard,
        Consumer<StoryBoardEntry> extras) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard, extras);
    }
}
