package net.createmod.ponder.api.registration;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.PonderReloadDetails;
import net.minecraft.util.ResourceLocation;

public interface PonderPlugin {

    String getModId();

    default void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    }

    default void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    default void registerSharedText(SharedTextRegistrationHelper helper) {
    }

    default PonderReloadDetails collectReloadDetails() {
        return PonderReloadDetails.EMPTY;
    }

    default void onPonderLevelRestore(PonderLevel ponderLevel) {
    }

    default void indexExclusions(IndexExclusionHelper helper) {
    }
}
