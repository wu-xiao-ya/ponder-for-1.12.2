package net.createmod.ponder.foundation.ui.projection;

import java.util.Objects;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.SpeechRenderer;

public record GuiOverlayPlacement(
    PonderScene.OverlayEvent overlayEvent,
    @Nullable SpeechRenderer.Point targetPoint,
    boolean aboveTarget,
    int panelX,
    int panelY,
    int panelWidth,
    int panelHeight,
    int drawX,
    int drawY,
    int drawWidth,
    int drawHeight) {

    public GuiOverlayPlacement {
        Objects.requireNonNull(overlayEvent);
    }
}
