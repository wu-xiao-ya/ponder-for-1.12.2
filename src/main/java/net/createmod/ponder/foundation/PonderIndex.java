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
import net.createmod.ponder.foundation.registration.DefaultPonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.registration.DefaultPonderTagRegistrationHelper;
import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.createmod.ponder.foundation.registration.DefaultSharedTextRegistrationHelper;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.createmod.ponder.foundation.registration.PonderTagRegistry;
import net.minecraft.util.ResourceLocation;

public final class PonderIndex {

    private static final PonderLocalization LOCALIZATION = new PonderLocalization();
    private static final PonderSceneRegistry SCENES = new PonderSceneRegistry(LOCALIZATION);
    private static final PonderTagRegistry TAGS = new PonderTagRegistry(LOCALIZATION);
    private static final List<PonderPlugin> PLUGINS = new ArrayList<PonderPlugin>();
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
        Ponder.LOGGER.info("Reloading Ponder plugin registry");
        LOCALIZATION.clearAll();
        SCENES.clearRegistry();
        TAGS.clearRegistry();
        registerAll();
        gatherSharedText();
        Ponder.LOGGER.info("Ponder registry now contains {} scene entries and {} listed tags",
            Integer.valueOf(SCENES.getRegisteredEntryCount()), Integer.valueOf(TAGS.getListedTagCount()));
    }

    public static void registerAll() {
        forEachPlugin(new Consumer<PonderPlugin>() {
            @Override
            public void accept(PonderPlugin plugin) {
                plugin.registerScenes(new DefaultPonderSceneRegistrationHelper(plugin.getModId(), SCENES));
                plugin.registerTags(new DefaultPonderTagRegistrationHelper(plugin.getModId(), TAGS, LOCALIZATION));
            }
        });
    }

    public static void gatherSharedText() {
        forEachPlugin(new Consumer<PonderPlugin>() {
            @Override
            public void accept(PonderPlugin plugin) {
                plugin.registerSharedText(new DefaultSharedTextRegistrationHelper(plugin.getModId(), LOCALIZATION));
            }
        });
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
