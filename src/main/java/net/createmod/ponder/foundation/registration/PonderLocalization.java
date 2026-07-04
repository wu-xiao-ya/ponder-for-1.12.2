package net.createmod.ponder.foundation.registration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.minecraft.util.ResourceLocation;

public class PonderLocalization implements LangRegistryAccess {

    private final LocalizationCatalog catalog;

    public PonderLocalization() {
        this.catalog = new LocalizationCatalog();
    }

    PonderLocalization(LocalizationCatalog catalog) {
        this.catalog = catalog;
    }

    public void clearAll() {
        RegistrationCommandService.execute(RegistrationCommands.clearLocalization(this));
    }

    public void clearSpecific() {
        RegistrationCommandService.execute(RegistrationCommands.clearSpecificLocalization(this));
    }

    public void registerShared(ResourceLocation key, String enUs) {
        RegistrationCommandService.execute(RegistrationCommands.registerSharedText(this, key, enUs));
    }

    public void registerTag(ResourceLocation key, String title, String description) {
        RegistrationCommandService.execute(RegistrationCommands.registerTagText(this, key, title, description));
    }

    public void registerSpecific(ResourceLocation sceneId, String key, String enUs) {
        RegistrationCommandService.execute(RegistrationCommands.registerSpecificText(this, sceneId, key, enUs));
    }

    public int getSharedTextCount() {
        return catalog.getSharedEntries().size();
    }

    @Override
    public void provideLang(String modId, BiConsumer<String, String> consumer) {
        for (Entry<ResourceLocation, String> entry : catalog.getSharedEntries().entrySet()) {
            if (modId.equals(entry.getKey().getNamespace())) {
                consumer.accept(langKeyForShared(entry.getKey()), entry.getValue());
            }
        }

        for (Entry<ResourceLocation, TagLangEntry> entry : catalog.getTagEntries().entrySet()) {
            if (modId.equals(entry.getKey().getNamespace())) {
                consumer.accept(langKeyForTag(entry.getKey()), entry.getValue().getTitle());
                consumer.accept(langKeyForTagDescription(entry.getKey()), entry.getValue().getDescription());
            }
        }

        for (Entry<ResourceLocation, Map<String, String>> entry : catalog.getSpecificEntries().entrySet()) {
            if (!modId.equals(entry.getKey().getNamespace())) {
                continue;
            }
            for (Entry<String, String> valueEntry : entry.getValue().entrySet()) {
                consumer.accept(langKeyForSpecific(entry.getKey(), valueEntry.getKey()), valueEntry.getValue());
            }
        }
    }

    public void generateSceneLang(SceneRegistryAccess scenes) {
        catalog.clearSpecific();
        for (Entry<ResourceLocation, StoryBoardEntry> entry : scenes.getRegisteredEntries()) {
            PonderSceneRegistry.compileScene(this, entry.getValue());
        }
    }

    @Override
    public String getShared(ResourceLocation key) {
        String value = catalog.getShared(key);
        return value != null ? value : "unregistered shared entry: " + key;
    }

    @Override
    public String getShared(ResourceLocation key, Object... params) {
        return String.format(getShared(key), params);
    }

    @Override
    public String getTagName(ResourceLocation key) {
        TagLangEntry value = catalog.getTag(key);
        return value != null ? value.getTitle() : "unregistered tag entry: " + key;
    }

    @Override
    public String getTagDescription(ResourceLocation key) {
        TagLangEntry value = catalog.getTag(key);
        return value != null ? value.getDescription() : "unregistered tag entry: " + key;
    }

    @Override
    public String getSpecific(ResourceLocation sceneId, String key) {
        String value = catalog.getSpecific(sceneId, key);
        return value != null ? value : "missing specific entry: " + sceneId + "/" + key;
    }

    @Override
    public String getSpecific(ResourceLocation sceneId, String key, Object... params) {
        return String.format(getSpecific(sceneId, key), params);
    }

    private static String langKeyForShared(ResourceLocation key) {
        return key.getNamespace() + ".ponder.shared." + key.getPath();
    }

    private static String langKeyForTag(ResourceLocation key) {
        return key.getNamespace() + ".ponder.tag." + key.getPath();
    }

    private static String langKeyForTagDescription(ResourceLocation key) {
        return key.getNamespace() + ".ponder.tag." + key.getPath() + ".description";
    }

    private static String langKeyForSpecific(ResourceLocation sceneId, String key) {
        return sceneId.getNamespace() + ".ponder." + sceneId.getPath() + "." + key;
    }

    void clearAllState() {
        catalog.clearAll();
    }

    void clearSpecificState() {
        catalog.clearSpecific();
    }

    void registerSharedState(ResourceLocation key, String enUs) {
        catalog.registerShared(key, enUs);
    }

    void registerTagState(ResourceLocation key, String title, String description) {
        catalog.registerTag(key, title, description);
    }

    void registerSpecificState(ResourceLocation sceneId, String key, String enUs) {
        catalog.registerSpecific(sceneId, key, enUs);
    }
}
