package net.createmod.ponder.foundation;

import java.util.List;
import java.util.function.Predicate;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.registration.DefaultPonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.registration.DefaultPonderTagRegistrationHelper;
import net.createmod.ponder.foundation.registration.PonderIndexExclusionHelper;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.createmod.ponder.foundation.registration.PonderTagRegistry;
import net.minecraft.util.ResourceLocation;

final class PonderReloadOrchestrator {

    private final PonderLocalization localization;
    private final PonderSceneRegistry scenes;
    private final PonderTagRegistry tags;
    private final List<PonderPlugin> plugins;

    PonderReloadOrchestrator(PonderLocalization localization, PonderSceneRegistry scenes, PonderTagRegistry tags,
        List<PonderPlugin> plugins) {
        this.localization = localization;
        this.scenes = scenes;
        this.tags = tags;
        this.plugins = plugins;
    }

    public PonderReloadReport reload() {
        Ponder.LOGGER.info("Reloading Ponder plugin registry");

        clearRegistries();
        applyIndexExclusions();
        registerAll();
        scenes.finishRegistration();
        tags.finishRegistration();
        int sharedTextCount = gatherSharedText();
        PonderReloadDetails details = collectReloadDetails();
        localization.generateSceneLang(scenes);
        int componentCount = scenes.getRegisteredComponentCount();

        PonderReloadReport report = new PonderReloadReport(scenes.getRegisteredEntryCount(), componentCount,
            tags.getListedTagCount(), plugins.size(), sharedTextCount, details);
        Ponder.LOGGER.info("Ponder registry now contains {}", report.formatCounts());
        return report;
    }

    public void registerAll() {
        for (PonderPlugin plugin : plugins) {
            plugin.registerScenes(new DefaultPonderSceneRegistrationHelper(plugin.getModId(), scenes));
            plugin.registerTags(new DefaultPonderTagRegistrationHelper(plugin.getModId(), tags, localization));
        }
    }

    public int gatherSharedText() {
        final int[] sharedTextCount = new int[1];
        for (PonderPlugin plugin : plugins) {
            plugin.registerSharedText((key, enUs) -> {
                localization.registerShared(new ResourceLocation(plugin.getModId(), key), enUs);
                sharedTextCount[0]++;
            });
        }
        return sharedTextCount[0];
    }

    private PonderReloadDetails collectReloadDetails() {
        PonderReloadDetails details = PonderReloadDetails.EMPTY;
        for (PonderPlugin plugin : plugins) {
            if (plugin instanceof PonderReloadDetailsProvider provider) {
                details = details.merge(provider.collectReloadDetails());
            }
        }
        return details;
    }

    private void clearRegistries() {
        localization.clearAll();
        scenes.clearRegistry();
        tags.clearRegistry();
    }

    private void applyIndexExclusions() {
        PonderIndexExclusionHelper helper = new PonderIndexExclusionHelper();
        for (PonderPlugin plugin : plugins) {
            plugin.indexExclusions(helper);
        }

        List<Predicate<ResourceLocation>> exclusions = helper.getExclusions();
        scenes.setIndexExclusions(exclusions);
        tags.setIndexExclusions(exclusions);
    }
}
