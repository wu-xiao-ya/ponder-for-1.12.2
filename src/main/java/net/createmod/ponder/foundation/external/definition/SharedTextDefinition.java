package net.createmod.ponder.foundation.external.definition;

/**
 * A named shared-text entry that can be referenced by scene operations.
 * Extracted as a forward-looking data model for the external JSON format's
 * {@code sharedText} section, keyed by identifier.
 */
public record SharedTextDefinition(
    SourceInfo source,
    String key,
    String text,
    String color,
    int independentY,
    int captionOffsetX,
    int captionOffsetY,
    boolean connectorVisible,
    boolean placeNearTarget
) {
    public SharedTextDefinition {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        if (text == null) text = "";
        if (color == null) color = "WHITE";
    }
}
