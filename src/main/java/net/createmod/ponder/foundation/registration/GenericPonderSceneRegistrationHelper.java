package net.createmod.ponder.foundation.registration;

import java.util.Arrays;
import java.util.function.Function;

import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.util.ResourceLocation;

public class GenericPonderSceneRegistrationHelper<T> implements PonderSceneRegistrationHelper<T> {

    private final PonderSceneRegistrationHelper<ResourceLocation> helperDelegate;
    private final Function<T, ResourceLocation> keyGen;

    public GenericPonderSceneRegistrationHelper(PonderSceneRegistrationHelper<ResourceLocation> helperDelegate,
        Function<T, ResourceLocation> keyGen) {
        this.helperDelegate = helperDelegate;
        this.keyGen = keyGen;
    }

    @Override
    public <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> nextKeyGen) {
        return new GenericPonderSceneRegistrationHelper<S>(helperDelegate, nextKeyGen.andThen(this.keyGen));
    }

    @Override
    public StoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
        ResourceLocation... tags) {
        return helperDelegate.addStoryBoard(keyGen.apply(component), schematicLocation, storyBoard, tags);
    }

    @Override
    public StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard,
        ResourceLocation... tags) {
        return helperDelegate.addStoryBoard(keyGen.apply(component), schematicPath, storyBoard, tags);
    }

    @Override
    public MultiSceneBuilder forComponents(T... components) {
        return new GenericMultiSceneBuilder<T>(this, Arrays.asList(components));
    }

    @Override
    public MultiSceneBuilder forComponents(Iterable<? extends T> components) {
        return new GenericMultiSceneBuilder<T>(this, components);
    }

    @Override
    public ResourceLocation asLocation(String path) {
        return helperDelegate.asLocation(path);
    }
}
