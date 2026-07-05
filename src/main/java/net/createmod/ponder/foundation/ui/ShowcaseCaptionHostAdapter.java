package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.gui.FontRenderer;

final class ShowcaseCaptionHostAdapter implements ShowcaseCaptionRenderer.Host {

    private final PonderDebugScreenHostSupport hostSupport;
    private final FontRenderer fontRenderer;

    ShowcaseCaptionHostAdapter(PonderDebugScreenHostSupport hostSupport, FontRenderer fontRenderer) {
        this.hostSupport = hostSupport;
        this.fontRenderer = fontRenderer;
    }

    @Override
    @Nullable
    public PonderScene.OverlayEvent getShowcaseCaption(PonderScene scene, float currentTick) {
        if (scene == null) {
            return null;
        }
        PonderScene.OverlayEvent activeCaption = null;
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if ((overlayEvent.getType() == PonderScene.OverlayEventType.TEXT
                || overlayEvent.getType() == PonderScene.OverlayEventType.GUI_HIGHLIGHT)
                && overlayEvent.getTick() <= currentTick
                && currentTick <= overlayEvent.getTick() + overlayEvent.getDuration()
                && overlayEvent.getText() != null
                && !overlayEvent.getText().isEmpty()) {
                activeCaption = overlayEvent;
            }
        }
        return activeCaption;
    }

    @Override
    public float computeCaptionFade(PonderScene.OverlayEvent overlayEvent, float currentTick) {
        return overlayEvent == null ? 0.0F
            : PonderOverlayHelper.computeOverlayFade(overlayEvent.getTick(), overlayEvent.getDuration(),
                currentTick);
    }

    @Override
    public PreviewLayout getLastPreviewLayout() {
        return hostSupport.getLastPreviewLayout();
    }

    @Override
    public void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing, int accentColor, float fade) {
        SpeechRenderer.drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, accentColor, fade);
    }

    @Override
    @Nullable
    public SpeechRenderer.Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing) {
        return SpeechRenderer.getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
    }

    @Override
    public void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
        SpeechRenderer.drawCaptionConnector(startX, startY, endX, endY, color, fade);
    }

    @Override
    public void drawCenteredStringNoShadow(String text, int centerX, int y, int color) {
        SpeechRenderer.drawCenteredStringNoShadow(fontRenderer, text, centerX, y, color);
    }
}
