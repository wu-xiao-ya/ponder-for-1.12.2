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
import java.util.function.Predicate;

import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.createmod.ponder.foundation.PonderSchematic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderSceneRegistry extends AbstractPonderRegistry implements SceneRegistryAccess {

    private final Map<ResourceLocation, List<StoryBoardEntry>> scenes =
        new LinkedHashMap<ResourceLocation, List<StoryBoardEntry>>();
    private final Map<ResourceLocation, PonderComponentMatcher> componentMatchers =
        new LinkedHashMap<ResourceLocation, PonderComponentMatcher>();
    private final PonderLocalization localization;
    private final List<Predicate<ResourceLocation>> indexExclusions = new ArrayList<Predicate<ResourceLocation>>();

    public PonderSceneRegistry(PonderLocalization localization) {
        this.localization = localization;
    }

    public void clearRegistry() {
        RegistrationCommandService.execute(RegistrationCommands.clearSceneRegistry(this));
    }

    public void setIndexExclusions(List<Predicate<ResourceLocation>> exclusions) {
        RegistrationCommandService.execute(RegistrationCommands.setSceneIndexExclusions(this, exclusions));
    }

    public void addStoryBoard(StoryBoardEntry entry) {
        RegistrationCommandService.execute(RegistrationCommands.registerScene(this, entry));
    }

    public void registerComponentMatcher(ResourceLocation componentId, PonderComponentMatcher matcher) {
        RegistrationCommandService.execute(RegistrationCommands.registerMatcher(this, componentId, matcher));
    }

    public int getRegisteredEntryCount() {
        int count = 0;
        for (List<StoryBoardEntry> value : scenes.values()) {
            count += value.size();
        }
        return count;
    }

    public int getRegisteredComponentCount() {
        int count = 0;
        for (ResourceLocation componentId : scenes.keySet()) {
            if (!isExcluded(componentId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean doScenesExistForId(ResourceLocation id) {
        if (id == null || isExcluded(id)) {
            return false;
        }

        List<StoryBoardEntry> directEntries = scenes.get(id);
        if (directEntries != null && !directEntries.isEmpty()) {
            return true;
        }

        for (PonderComponentMatcher matcher : componentMatchers.values()) {
            if (isExcluded(matcher.getItemId())) {
                continue;
            }
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
            if (isExcluded(entry.getKey())) {
                continue;
            }
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
            if (isExcluded(mapEntry.getKey())) {
                continue;
            }
            for (StoryBoardEntry storyBoardEntry : mapEntry.getValue()) {
                entries.add(new SimpleImmutableEntry<ResourceLocation, StoryBoardEntry>(mapEntry.getKey(),
                    storyBoardEntry));
            }
        }
        return Collections.unmodifiableList(entries);
    }

    @Override
    public List<PonderScene> compile(ResourceLocation id) {
        if (id == null || isExcluded(id)) {
            return Collections.emptyList();
        }

        List<StoryBoardEntry> matchedEntries = new ArrayList<StoryBoardEntry>();
        Set<StoryBoardEntry> visited = Collections.newSetFromMap(new IdentityHashMap<StoryBoardEntry, Boolean>());
        addEntries(scenes.get(id), matchedEntries, visited);

        for (Map.Entry<ResourceLocation, PonderComponentMatcher> entry : componentMatchers.entrySet()) {
            if (isExcluded(entry.getKey())) {
                continue;
            }
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
            if (entry == null || isExcluded(entry.getComponent())) {
                continue;
            }
            compiled.add(compileScene(localization, entry));
        }
        return Collections.unmodifiableList(compiled);
    }

    @Override
    public ItemStack getDisplayStack(ResourceLocation componentId) {
        if (componentId == null || isExcluded(componentId)) {
            return ItemStack.EMPTY;
        }

        PonderComponentMatcher matcher = componentMatchers.get(componentId);
        if (matcher != null) {
            return matcher.createDisplayStack();
        }

        for (PonderComponentMatcher candidate : componentMatchers.values()) {
            if (isExcluded(candidate.getItemId())) {
                continue;
            }
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

    private boolean isExcluded(ResourceLocation componentId) {
        if (componentId == null) {
            return false;
        }
        for (Predicate<ResourceLocation> predicate : indexExclusions) {
            if (predicate.test(componentId)) {
                return true;
            }
        }
        return false;
    }

    public static PonderScene compileScene(PonderLocalization localization, StoryBoardEntry entry) {
        PonderScene scene = new PonderScene(entry, localization);
        scene.setSchematic(PonderSchematic.load(entry.getSchematicLocation()));
        scene.program(new PonderSceneBuilder(scene), scene.getSceneBuildingUtil());
        return scene;
    }

    void clearRegistryState() {
        scenes.clear();
        componentMatchers.clear();
        indexExclusions.clear();
        resetRegistrationPhase();
    }

    void setIndexExclusionsState(List<Predicate<ResourceLocation>> exclusions) {
        indexExclusions.clear();
        if (exclusions != null) {
            indexExclusions.addAll(exclusions);
        }
    }

    void addStoryBoardState(StoryBoardEntry entry) {
        ensureRegistrationOpen();
        if (entry == null || isExcluded(entry.getComponent())) {
            return;
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

    void registerComponentMatcherState(ResourceLocation componentId, PonderComponentMatcher matcher) {
        ensureRegistrationOpen();
        if (componentId == null || matcher == null) {
            return;
        }
        if (isExcluded(componentId)) {
            return;
        }
        componentMatchers.put(componentId, matcher);
    }
}
