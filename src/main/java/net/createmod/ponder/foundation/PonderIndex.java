package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.TagRegistryAccess;
import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.createmod.ponder.foundation.registration.PonderTagRegistry;
import net.minecraft.util.ResourceLocation;

public final class PonderIndex {

    private static final PonderLocalization LOCALIZATION = new PonderLocalization();
    private static final PonderSceneRegistry SCENES = new PonderSceneRegistry(LOCALIZATION);
    private static final PonderTagRegistry TAGS = new PonderTagRegistry(LOCALIZATION);
    private static final List<PonderPlugin> PLUGINS = new ArrayList<PonderPlugin>();
    private static final PonderReloadOrchestrator RELOAD_ORCHESTRATOR =
        new PonderReloadOrchestrator(LOCALIZATION, SCENES, TAGS, PLUGINS);
    private static final Comparator<PonderPlugin> PLUGIN_COMPARATOR = new Comparator<PonderPlugin>() {
        @Override
        public int compare(PonderPlugin left, PonderPlugin right) {
            boolean leftIsPonder = Ponder.MOD_ID.equals(left.getModId());
            boolean rightIsPonder = Ponder.MOD_ID.equals(right.getModId());
            if (leftIsPonder != rightIsPonder) {
                return leftIsPonder ? -1 : 1;
            }
            return left.getModId().compareTo(right.getModId());
        }
    };

    private PonderIndex() {
    }

    public static void addPlugin(PonderPlugin plugin) {
        synchronized (PLUGINS) {
            int index = Collections.binarySearch(PLUGINS, plugin, PLUGIN_COMPARATOR);
            int insertionPoint = index >= 0 ? index : -index - 1;
            PLUGINS.add(insertionPoint, plugin);
        }
    }

    public static void forEachPlugin(Consumer<PonderPlugin> action) {
        for (PonderPlugin plugin : PLUGINS) {
            action.accept(plugin);
        }
    }

    public static void reload() {
        RELOAD_ORCHESTRATOR.reload();
    }

    public static void registerAll() {
        RELOAD_ORCHESTRATOR.registerAll();
    }

    public static void gatherSharedText() {
        RELOAD_ORCHESTRATOR.gatherSharedText();
    }

    public static SceneRegistryAccess getSceneAccess() {
        return SCENES;
    }

    public static void registerComponentMatcher(ResourceLocation componentId, PonderComponentMatcher matcher) {
        SCENES.registerComponentMatcher(componentId, matcher);
    }

    public static TagRegistryAccess getTagAccess() {
        return TAGS;
    }

    public static LangRegistryAccess getLangAccess() {
        return LOCALIZATION;
    }

    public static int getPluginCount() {
        return PLUGINS.size();
    }
}
