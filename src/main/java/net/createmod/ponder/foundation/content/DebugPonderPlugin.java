package net.createmod.ponder.foundation.content;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

public class DebugPonderPlugin implements PonderPlugin {

    private static final ResourceLocation DEBUG_COMPONENT = new ResourceLocation("minecraft", "compass");
    private static final ResourceLocation DEBUG_TAG = Ponder.asResource("debug");

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        TestScenes.registerAll(helper, DEBUG_COMPONENT, DEBUG_TAG);
        DebugScenes.registerAll(helper, DEBUG_COMPONENT, DEBUG_TAG);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(DEBUG_TAG)
            .title("Debug")
            .description("Upstream-style debug storyboards for validating the 1.12.2 backport runtime.")
            .item(Items.COMPASS)
            .addToIndex()
            .register();

        helper.addTagToComponent(DEBUG_COMPONENT, DEBUG_TAG);
    }
}
