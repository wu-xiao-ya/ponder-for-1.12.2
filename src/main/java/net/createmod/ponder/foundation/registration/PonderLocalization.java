package net.createmod.ponder.foundation.registration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.minecraft.util.ResourceLocation;

public class PonderLocalization implements LangRegistryAccess {

    private final Map<ResourceLocation, String> shared = new LinkedHashMap<ResourceLocation, String>();
    private final Map<ResourceLocation, TagLangEntry> tag = new LinkedHashMap<ResourceLocation, TagLangEntry>();
    private final Map<ResourceLocation, Map<String, String>> specific =
        new LinkedHashMap<ResourceLocation, Map<String, String>>();

    public void clearAll() {
        shared.clear();
        tag.clear();
        specific.clear();
    }

    public void clearSpecific() {
        specific.clear();
    }

    public void registerShared(ResourceLocation key, String enUs) {
        shared.put(key, enUs);
    }

    public void registerTag(ResourceLocation key, String title, String description) {
        tag.put(key, new TagLangEntry(title, description));
    }

    public void registerSpecific(ResourceLocation sceneId, String key, String enUs) {
        Map<String, String> values = specific.get(sceneId);
        if (values == null) {
            values = new LinkedHashMap<String, String>();
            specific.put(sceneId, values);
        }
        values.put(key, enUs);
    }

    @Override
    public void provideLang(String modId, BiConsumer<String, String> consumer) {
        generateSceneLang();

        for (Entry<ResourceLocation, String> entry : shared.entrySet()) {
            if (modId.equals(entry.getKey().getNamespace())) {
                consumer.accept(langKeyForShared(entry.getKey()), entry.getValue());
            }
        }

        for (Entry<ResourceLocation, TagLangEntry> entry : tag.entrySet()) {
            if (modId.equals(entry.getKey().getNamespace())) {
                consumer.accept(langKeyForTag(entry.getKey()), entry.getValue().title);
                consumer.accept(langKeyForTagDescription(entry.getKey()), entry.getValue().description);
            }
        }

        for (Entry<ResourceLocation, Map<String, String>> entry : specific.entrySet()) {
            if (!modId.equals(entry.getKey().getNamespace())) {
                continue;
            }
            for (Entry<String, String> valueEntry : entry.getValue().entrySet()) {
                consumer.accept(langKeyForSpecific(entry.getKey(), valueEntry.getKey()), valueEntry.getValue());
            }
        }
    }

    private void generateSceneLang() {
        clearSpecific();
        for (Entry<ResourceLocation, StoryBoardEntry> entry : PonderIndex.getSceneAccess().getRegisteredEntries()) {
            PonderSceneRegistry.compileScene(this, entry.getValue());
        }
    }

    @Override
    public String getShared(ResourceLocation key) {
        String value = shared.get(key);
        return value != null ? value : "unregistered shared entry: " + key;
    }

    @Override
    public String getShared(ResourceLocation key, Object... params) {
        return String.format(getShared(key), params);
    }

    @Override
    public String getTagName(ResourceLocation key) {
        TagLangEntry value = tag.get(key);
        return value != null ? value.title : "unregistered tag entry: " + key;
    }

    @Override
    public String getTagDescription(ResourceLocation key) {
        TagLangEntry value = tag.get(key);
        return value != null ? value.description : "unregistered tag entry: " + key;
    }

    @Override
    public String getSpecific(ResourceLocation sceneId, String key) {
        Map<String, String> values = specific.get(sceneId);
        if (values == null || !values.containsKey(key)) {
            return "missing specific entry: " + sceneId + "/" + key;
        }
        return values.get(key);
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

    private static final class TagLangEntry {
        private final String title;
        private final String description;

        private TagLangEntry(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
