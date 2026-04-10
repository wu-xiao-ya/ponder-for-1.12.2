package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.util.ResourceLocation;

public class PonderStoryBoardEntry implements StoryBoardEntry {

    private final PonderStoryBoard board;
    private final String namespace;
    private final ResourceLocation schematicLocation;
    private final ResourceLocation component;
    private final List<ResourceLocation> tags = new ArrayList<ResourceLocation>();
    private final List<SceneOrderingEntry> orderingEntries = new ArrayList<SceneOrderingEntry>();

    public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, ResourceLocation schematicLocation,
        ResourceLocation component) {
        this.board = board;
        this.namespace = namespace;
        this.schematicLocation = schematicLocation;
        this.component = component;
    }

    @Override
    public PonderStoryBoard getBoard() {
        return board;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public ResourceLocation getSchematicLocation() {
        return schematicLocation;
    }

    @Override
    public ResourceLocation getComponent() {
        return component;
    }

    @Override
    public List<ResourceLocation> getTags() {
        return Collections.unmodifiableList(tags);
    }

    @Override
    public List<SceneOrderingEntry> getOrderingEntries() {
        return Collections.unmodifiableList(orderingEntries);
    }

    @Override
    public StoryBoardEntry orderBefore(String namespace, String otherSceneId) {
        orderingEntries.add(SceneOrderingEntry.before(namespace, otherSceneId));
        return this;
    }

    @Override
    public StoryBoardEntry orderAfter(String namespace, String otherSceneId) {
        orderingEntries.add(SceneOrderingEntry.after(namespace, otherSceneId));
        return this;
    }

    @Override
    public StoryBoardEntry highlightTag(ResourceLocation tag) {
        tags.add(tag);
        return this;
    }

    @Override
    public StoryBoardEntry highlightTags(ResourceLocation... tags) {
        Collections.addAll(this.tags, tags);
        return this;
    }

    @Override
    public StoryBoardEntry highlightAllTags() {
        tags.add(PonderTag.Highlight.ALL);
        return this;
    }
}
