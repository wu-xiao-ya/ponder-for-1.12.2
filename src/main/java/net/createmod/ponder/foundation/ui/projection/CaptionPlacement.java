package net.createmod.ponder.foundation.ui.projection;

import java.util.Objects;

import net.createmod.ponder.foundation.ui.SpeechRenderer;

public record CaptionPlacement(int x, int y, SpeechRenderer.SpeechPointing pointing) {

    public CaptionPlacement {
        Objects.requireNonNull(pointing);
    }
}
