package net.createmod.ponder.foundation.registration;

public final class TagLangEntry {

    private final String title;
    private final String description;

    public TagLangEntry(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}