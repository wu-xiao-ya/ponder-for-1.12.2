package net.createmod.ponder.foundation;

import java.util.Collections;
import java.util.List;

import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.util.ResourceLocation;

public class PonderScene {

    public static final String TITLE_KEY = "header";

    private ResourceLocation sceneId;


    public static final class RecordedOperation {
        private final int tick;
        private final String description;

        public RecordedOperation(int tick, String description) {
            this.tick = tick;
            this.description = description;
        }

        public int getTick() {
            return tick;
        }

        public String getDescription() {
            return description;
        }
    }

    public PonderScene() {
    }

    public ResourceLocation getSceneId() {
        return sceneId;
    }

    public void setSceneId(ResourceLocation sceneId) {
        this.sceneId = sceneId;
    }

    public String getTitle() {
        return "";
    }

    public List<RecordedOperation> getRecordedOperations() {
        return Collections.emptyList();
    }

    public List<String> getOperationLog() {
        return Collections.emptyList();
    }

    public void program(SceneBuilder scene, SceneBuildingUtil util) {
    }

    public StoryBoardEntry getEntry() {
        return null;
    }
}
