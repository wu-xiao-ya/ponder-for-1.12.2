package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.api.registration.TagRegistryAccess;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class PonderIndex {

    private static final List<PonderPlugin> PLUGINS = new ArrayList<PonderPlugin>();
    private static final SceneRegistryAccess SCENES = new NoOpSceneRegistryAccess();
    private static final TagRegistryAccess TAGS = new NoOpTagRegistryAccess();
    private static final LangRegistryAccess LANG = new NoOpLangRegistryAccess();
    private static final Comparator<PonderPlugin> PLUGIN_COMPARATOR = new Comparator<PonderPlugin>() {
        @Override
        public int compare(PonderPlugin left, PonderPlugin right) {
            return left.getModId().compareTo(right.getModId());
        }
    };

    private PonderIndex() {
    }

    public static void addPlugin(PonderPlugin plugin) {
        if (plugin == null) {
            return;
        }
        synchronized (PLUGINS) {
            int index = Collections.binarySearch(PLUGINS, plugin, PLUGIN_COMPARATOR);
            int insertionPoint = index >= 0 ? index : -index - 1;
            PLUGINS.add(insertionPoint, plugin);
        }
    }

    public static void forEachPlugin(Consumer<PonderPlugin> action) {
        if (action == null) {
            return;
        }
        synchronized (PLUGINS) {
            for (PonderPlugin plugin : PLUGINS) {
                action.accept(plugin);
            }
        }
    }

    public static void reload() {
        forEachPlugin(new Consumer<PonderPlugin>() {
            @Override
            public void accept(PonderPlugin plugin) {
                try {
                    plugin.registerScenes(new NoOpSceneRegistrationHelper<ResourceLocation>(plugin.getModId()));
                    plugin.registerTags(new NoOpTagRegistrationHelper<ResourceLocation>(plugin.getModId()));
                    plugin.registerSharedText(new NoOpSharedTextRegistrationHelper());
                } catch (Throwable throwable) {
                    Ponder.LOGGER.warn("Skipped Ponder plugin {} on Forge server shim", plugin.getModId(), throwable);
                }
            }
        });
        Ponder.LOGGER.info("Ponder Forge server shim accepted {} plugin(s)", Integer.valueOf(getPluginCount()));
    }

    public static void registerAll() {
        reload();
    }

    public static void gatherSharedText() {
    }

    public static SceneRegistryAccess getSceneAccess() {
        return SCENES;
    }

    public static void registerComponentMatcher(ResourceLocation componentId, PonderComponentMatcher matcher) {
    }

    public static TagRegistryAccess getTagAccess() {
        return TAGS;
    }

    public static LangRegistryAccess getLangAccess() {
        return LANG;
    }

    public static int getPluginCount() {
        synchronized (PLUGINS) {
            return PLUGINS.size();
        }
    }

    private static ItemStack emptyStack() {
        return new ItemStack((Item) null);
    }

    private static final class NoOpSceneRegistrationHelper<T> implements PonderSceneRegistrationHelper<T> {
        private final String modId;

        private NoOpSceneRegistrationHelper(String modId) {
            this.modId = modId == null ? Ponder.MOD_ID : modId;
        }

        @Override
        public <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
            return new NoOpSceneRegistrationHelper<S>(modId);
        }

        @Override
        public StoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
            ResourceLocation... tags) {
            return NoOpStoryBoardEntry.INSTANCE;
        }

        @Override
        public StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard,
            ResourceLocation... tags) {
            return NoOpStoryBoardEntry.INSTANCE;
        }

        @Override
        public MultiSceneBuilder forComponents(T... components) {
            return NoOpMultiSceneBuilder.INSTANCE;
        }

        @Override
        public MultiSceneBuilder forComponents(Iterable<? extends T> components) {
            return NoOpMultiSceneBuilder.INSTANCE;
        }

        @Override
        public ResourceLocation asLocation(String path) {
            return new ResourceLocation(modId, path);
        }
    }

    private static final class NoOpTagRegistrationHelper<T> implements PonderTagRegistrationHelper<T> {
        private final String modId;

        private NoOpTagRegistrationHelper(String modId) {
            this.modId = modId == null ? Ponder.MOD_ID : modId;
        }

        @Override
        public <S> PonderTagRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
            return new NoOpTagRegistrationHelper<S>(modId);
        }

        @Override
        public TagBuilder registerTag(ResourceLocation location) {
            return NoOpTagBuilder.INSTANCE;
        }

        @Override
        public TagBuilder registerTag(String id) {
            return NoOpTagBuilder.INSTANCE;
        }

        @Override
        public void addTagToComponent(T component, ResourceLocation tag) {
        }

        @Override
        public MultiTagBuilder.Tag<T> addToTag(ResourceLocation tag) {
            return new NoOpMultiTagBuilder<T>();
        }

        @Override
        public MultiTagBuilder.Tag<T> addToTag(ResourceLocation... tags) {
            return new NoOpMultiTagBuilder<T>();
        }

        @Override
        public MultiTagBuilder.Component addToComponent(T component) {
            return new NoOpMultiTagBuilder<Object>();
        }

        @Override
        public MultiTagBuilder.Component addToComponent(T... components) {
            return new NoOpMultiTagBuilder<Object>();
        }
    }

    private static final class NoOpMultiSceneBuilder implements MultiSceneBuilder {
        private static final NoOpMultiSceneBuilder INSTANCE = new NoOpMultiSceneBuilder();

        @Override
        public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard) {
            return this;
        }

        @Override
        public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
            ResourceLocation... tags) {
            return this;
        }

        @Override
        public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard,
            Consumer<StoryBoardEntry> extras) {
            if (extras != null) {
                extras.accept(NoOpStoryBoardEntry.INSTANCE);
            }
            return this;
        }

        @Override
        public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard) {
            return this;
        }

        @Override
        public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, ResourceLocation... tags) {
            return this;
        }

        @Override
        public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard,
            Consumer<StoryBoardEntry> extras) {
            if (extras != null) {
                extras.accept(NoOpStoryBoardEntry.INSTANCE);
            }
            return this;
        }
    }

    private static final class NoOpMultiTagBuilder<T> implements MultiTagBuilder.Tag<T>, MultiTagBuilder.Component {
        @Override
        public MultiTagBuilder.Tag<T> add(T component) {
            return this;
        }

        @Override
        public MultiTagBuilder.Component add(ResourceLocation tag) {
            return this;
        }
    }

    private static final class NoOpTagBuilder implements TagBuilder {
        private static final NoOpTagBuilder INSTANCE = new NoOpTagBuilder();

        @Override
        public TagBuilder title(String title) {
            return this;
        }

        @Override
        public TagBuilder description(String description) {
            return this;
        }

        @Override
        public TagBuilder addToIndex() {
            return this;
        }

        @Override
        public TagBuilder icon(ResourceLocation location) {
            return this;
        }

        @Override
        public TagBuilder icon(String path) {
            return this;
        }

        @Override
        public TagBuilder idAsIcon() {
            return this;
        }

        @Override
        public TagBuilder item(ItemStack stack, boolean useAsIcon, boolean useAsMainItem) {
            return this;
        }

        @Override
        public void register() {
        }
    }

    private static final class NoOpSharedTextRegistrationHelper implements SharedTextRegistrationHelper {
        @Override
        public void registerSharedText(String key, String enUs) {
        }
    }

    private static final class NoOpSceneRegistryAccess implements SceneRegistryAccess {
        @Override
        public boolean doScenesExistForId(ResourceLocation id) {
            return false;
        }

        @Override
        public ResourceLocation resolveComponentId(ItemStack stack) {
            return null;
        }

        @Override
        public Collection<Map.Entry<ResourceLocation, StoryBoardEntry>> getRegisteredEntries() {
            return Collections.emptyList();
        }

        @Override
        public List<PonderScene> compile(ResourceLocation id) {
            return Collections.emptyList();
        }

        @Override
        public List<PonderScene> compile(Collection<StoryBoardEntry> entries) {
            return Collections.emptyList();
        }

        @Override
        public ItemStack getDisplayStack(ResourceLocation componentId) {
            return emptyStack();
        }
    }

    private static final class NoOpTagRegistryAccess implements TagRegistryAccess {
        @Override
        public PonderTag getRegisteredTag(ResourceLocation tagLocation) {
            return new PonderTag(tagLocation, null, emptyStack(), emptyStack());
        }

        @Override
        public List<PonderTag> getListedTags() {
            return Collections.emptyList();
        }

        @Override
        public Set<PonderTag> getTags(ResourceLocation componentId) {
            return Collections.emptySet();
        }

        @Override
        public Set<ResourceLocation> getItems(ResourceLocation tag) {
            return Collections.emptySet();
        }

        @Override
        public Set<ResourceLocation> getItems(PonderTag tag) {
            return new LinkedHashSet<ResourceLocation>();
        }
    }

    private static final class NoOpLangRegistryAccess implements LangRegistryAccess {
        @Override
        public void provideLang(String modId, BiConsumer<String, String> consumer) {
        }

        @Override
        public String getShared(ResourceLocation key) {
            return key == null ? "" : key.toString();
        }

        @Override
        public String getShared(ResourceLocation key, Object... params) {
            return getShared(key);
        }

        @Override
        public String getTagName(ResourceLocation key) {
            return getShared(key);
        }

        @Override
        public String getTagDescription(ResourceLocation key) {
            return "";
        }

        @Override
        public String getSpecific(ResourceLocation sceneId, String key) {
            return key == null ? "" : key;
        }

        @Override
        public String getSpecific(ResourceLocation sceneId, String key, Object... params) {
            return getSpecific(sceneId, key);
        }
    }

    private static final class NoOpStoryBoardEntry implements StoryBoardEntry {
        private static final NoOpStoryBoardEntry INSTANCE = new NoOpStoryBoardEntry();

        @Override
        public PonderStoryBoard getBoard() {
            return null;
        }

        @Override
        public String getNamespace() {
            return Ponder.MOD_ID;
        }

        @Override
        public ResourceLocation getSchematicLocation() {
            return Ponder.asResource("server_shim/empty");
        }

        @Override
        public ResourceLocation getComponent() {
            return Ponder.asResource("server_shim/empty");
        }

        @Override
        public List<ResourceLocation> getTags() {
            return Collections.emptyList();
        }

        @Override
        public List<StoryBoardEntry.SceneOrderingEntry> getOrderingEntries() {
            return Collections.emptyList();
        }

        @Override
        public StoryBoardEntry orderBefore(String namespace, String otherSceneId) {
            return this;
        }

        @Override
        public StoryBoardEntry orderAfter(String namespace, String otherSceneId) {
            return this;
        }

        @Override
        public StoryBoardEntry highlightTag(ResourceLocation tag) {
            return this;
        }

        @Override
        public StoryBoardEntry highlightTags(ResourceLocation... tags) {
            return this;
        }

        @Override
        public StoryBoardEntry highlightAllTags() {
            return this;
        }
    }
}
