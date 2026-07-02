package net.createmod.ponder.foundation.ui.projection;

import java.util.Objects;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.SpeechRenderer;

public record GuiHighlightPlacement(
    PonderScene.OverlayEvent overlayEvent,
    int rectX,
    int rectY,
    int rectWidth,
    int rectHeight,
    @Nullable SpeechRenderer.Point targetPoint) {

    public GuiHighlightPlacement {
        Objects.requireNonNull(overlayEvent);
    }
}
