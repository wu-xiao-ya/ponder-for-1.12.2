package net.createmod.ponder.foundation.registration;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.createmod.ponder.foundation.PonderSchematic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderSceneRegistry implements SceneRegistryAccess {

    private final Map<ResourceLocation, List<StoryBoardEntry>> scenes =
        new LinkedHashMap<ResourceLocation, List<StoryBoardEntry>>();
    private final Map<ResourceLocation, PonderComponentMatcher> componentMatchers =
        new LinkedHashMap<ResourceLocation, PonderComponentMatcher>();
    private final PonderLocalization localization;
    private boolean allowRegistration = true;

    public PonderSceneRegistry(PonderLocalization localization) {
        this.localization = localization;
    }

    public void clearRegistry() {
        scenes.clear();
        componentMatchers.clear();
        allowRegistration = true;
    }

    public void addStoryBoard(StoryBoardEntry entry) {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }

        List<StoryBoardEntry> entries = scenes.get(entry.getComponent());
        if (entries == null) {
            entries = new ArrayList<StoryBoardEntry>();
            scenes.put(entry.getComponent(), entries);
        }
        entries.add(entry);

        if (!componentMatchers.containsKey(entry.getComponent())) {
            componentMatchers.put(entry.getComponent(), PonderComponentMatcher.simple(entry.getComponent()));
        }
    }

    public void registerComponentMatcher(ResourceLocation componentId, PonderComponentMatcher matcher) {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }
        if (componentId == null || matcher == null) {
            return;
        }
        componentMatchers.put(componentId, matcher);
    }

    public int getRegisteredEntryCount() {
        int count = 0;
        for (List<StoryBoardEntry> value : scenes.values()) {
            count += value.size();
        }
        return count;
    }

    @Override
    public boolean doScenesExistForId(ResourceLocation id) {
        if (id == null) {
            return false;
        }

        List<StoryBoardEntry> directEntries = scenes.get(id);
        if (directEntries != null && !directEntries.isEmpty()) {
            return true;
        }

        for (PonderComponentMatcher matcher : componentMatchers.values()) {
            if (id.equals(matcher.getItemId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceLocation resolveComponentId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        ResourceLocation bestComponent = null;
        int bestSpecificity = Integer.MIN_VALUE;
        for (Map.Entry<ResourceLocation, PonderComponentMatcher> entry : componentMatchers.entrySet()) {
            PonderComponentMatcher matcher = entry.getValue();
            if (!matcher.matches(stack)) {
                continue;
            }

            int specificity = matcher.getSpecificity();
            if (bestComponent == null || specificity > bestSpecificity) {
                bestComponent = entry.getKey();
                bestSpecificity = specificity;
            }
        }
        return bestComponent;
    }

    @Override
    public Collection<Map.Entry<ResourceLocation, StoryBoardEntry>> getRegisteredEntries() {
        List<Map.Entry<ResourceLocation, StoryBoardEntry>> entries =
            new ArrayList<Map.Entry<ResourceLocation, StoryBoardEntry>>();
        for (Map.Entry<ResourceLocation, List<StoryBoardEntry>> mapEntry : scenes.entrySet()) {
            for (StoryBoardEntry storyBoardEntry : mapEntry.getValue()) {
                entries.add(new SimpleImmutableEntry<ResourceLocation, StoryBoardEntry>(mapEntry.getKey(),
                    storyBoardEntry));
            }
        }
        return Collections.unmodifiableList(entries);
    }

    @Override
    public List<PonderScene> compile(ResourceLocation id) {
        if (id == null) {
            return Collections.emptyList();
        }

        List<StoryBoardEntry> matchedEntries = new ArrayList<StoryBoardEntry>();
        Set<StoryBoardEntry> visited = Collections.newSetFromMap(new IdentityHashMap<StoryBoardEntry, Boolean>());
        addEntries(scenes.get(id), matchedEntries, visited);

        for (Map.Entry<ResourceLocation, PonderComponentMatcher> entry : componentMatchers.entrySet()) {
            if (!id.equals(entry.getValue().getItemId())) {
                continue;
            }
            addEntries(scenes.get(entry.getKey()), matchedEntries, visited);
        }

        if (matchedEntries.isEmpty()) {
            return Collections.emptyList();
        }
        return compile(matchedEntries);
    }

    @Override
    public List<PonderScene> compile(Collection<StoryBoardEntry> entries) {
        List<PonderScene> compiled = new ArrayList<PonderScene>();
        for (StoryBoardEntry entry : entries) {
            compiled.add(compileScene(localization, entry));
        }
        return Collections.unmodifiableList(compiled);
    }

    @Override
    public ItemStack getDisplayStack(ResourceLocation componentId) {
        if (componentId == null) {
            return ItemStack.EMPTY;
        }

        PonderComponentMatcher matcher = componentMatchers.get(componentId);
        if (matcher != null) {
            return matcher.createDisplayStack();
        }

        for (PonderComponentMatcher candidate : componentMatchers.values()) {
            if (componentId.equals(candidate.getItemId())) {
                return candidate.createDisplayStack();
            }
        }

        return ItemStack.EMPTY;
    }

    private static void addEntries(List<StoryBoardEntry> source, List<StoryBoardEntry> target, Set<StoryBoardEntry> visited) {
        if (source == null || source.isEmpty()) {
            return;
        }

        for (StoryBoardEntry entry : source) {
            if (visited.add(entry)) {
                target.add(entry);
            }
        }
    }

    public static PonderScene compileScene(PonderLocalization localization, StoryBoardEntry entry) {
        PonderScene scene = new PonderScene(entry, localization);
        scene.setSchematic(PonderSchematic.load(entry.getSchematicLocation()));
        scene.program(new PonderSceneBuilder(scene), scene.getSceneBuildingUtil());
        return scene;
    }
}
