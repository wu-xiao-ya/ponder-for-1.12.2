package net.createmod.ponder.foundation.external.definition;

import java.util.Collections;
import java.util.List;

/**
 * Immutable model for a single scene-building operation, serialized from
 * external JSON. All fields are present to cover every operation type;
 * per-type validity is enforced by the operation executor, not the data model.
 */
public record SceneOperationDefinition(
    String type,
    int xOffset,
    int zOffset,
    int size,
    int duration,
    int ticks,
    int times,
    int independentY,
    float factor,
    float degrees,
    float offsetY,
    boolean enabled,
    String text,
    String color,
    String overlayId,
    String parentOverlayId,
    String parentGuiId,
    String blockGui,
    String blockState,
    int blockMeta,
    String blockNbt,
    String direction,
    int captionX,
    int captionY,
    int captionOffsetX,
    int captionOffsetY,
    boolean connectorVisible,
    boolean placeNearTarget,
    String pointMode,
    String snapshot,
    String texture,
    int textureU,
    int textureV,
    int regionWidth,
    int regionHeight,
    int displayWidth,
    int displayHeight,
    int textureWidth,
    int textureHeight,
    int offsetX,
    boolean framed,
    boolean scaleToParent,
    boolean stretchTexture,
    int stretchBorder,
    int guiX,
    int guiY,
    int guiWidth,
    int guiHeight,
    int[] pos,
    int[] from,
    int[] to,
    double[] offset,
    double[] rotation,
    double[] pivot,
    double[] pointAt,
    float rotX,
    float rotY,
    float rotZ,
    List<SceneOperationDefinition> childOperations
) {
    public SceneOperationDefinition {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type must not be blank");
        if (childOperations == null) childOperations = Collections.emptyList();
    }
}
