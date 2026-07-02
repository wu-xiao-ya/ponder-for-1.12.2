package net.createmod.ponder.foundation.registration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public class LocalizationCatalog {

    private final Map<ResourceLocation, String> shared = new LinkedHashMap<ResourceLocation, String>();
    private final Map<ResourceLocation, TagLangEntry> tag = new LinkedHashMap<ResourceLocation, TagLangEntry>();
    private final Map<ResourceLocation, Map<String, String>> specific =
        new LinkedHashMap<ResourceLocation, Map<String, String>>();

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

    public String getShared(ResourceLocation key) {
        return shared.get(key);
    }

    public TagLangEntry getTag(ResourceLocation key) {
        return tag.get(key);
    }

    public String getSpecific(ResourceLocation sceneId, String key) {
        Map<String, String> values = specific.get(sceneId);
        if (values == null) {
            return null;
        }
        return values.get(key);
    }

    public Map<ResourceLocation, String> getSharedEntries() {
        return Collections.unmodifiableMap(shared);
    }

    public Map<ResourceLocation, TagLangEntry> getTagEntries() {
        return Collections.unmodifiableMap(tag);
    }

    public Map<ResourceLocation, Map<String, String>> getSpecificEntries() {
        return Collections.unmodifiableMap(specific);
    }

    public void clearAll() {
        shared.clear();
        tag.clear();
        specific.clear();
    }

    public void clearSpecific() {
        specific.clear();
    }
}