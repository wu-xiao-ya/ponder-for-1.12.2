package net.createmod.ponder.foundation.ui;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.projection.SceneBounds;
import net.createmod.ponder.foundation.ui.projection.CaptionPlacement;
import net.createmod.ponder.foundation.ui.projection.GuiHighlightPlacement;
import net.createmod.ponder.foundation.ui.projection.GuiOverlayPlacement;
import net.createmod.ponder.foundation.ui.projection.ScenePointProjector;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;

final class ShowcaseCaptionRenderer {

    interface Host {
        @Nullable
        PonderScene.OverlayEvent getShowcaseCaption(PonderScene scene, float currentTick);

        float computeCaptionFade(PonderScene.OverlayEvent overlayEvent, float currentTick);

        PreviewLayout getLastPreviewLayout();

        void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight,
            SpeechRenderer.SpeechPointing pointing, int accentColor, float fade);

        @Nullable
        SpeechRenderer.Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight,
            SpeechRenderer.SpeechPointing pointing);

        void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade);

        void drawCenteredStringNoShadow(String text, int centerX, int y, int color);
    }

    private final FontRenderer fontRenderer;
    private final Host host;
    private final ScenePointProjector projector;

    ShowcaseCaptionRenderer(FontRenderer fontRenderer, Host host, ScenePointProjector projector) {
        this.fontRenderer = fontRenderer;
        this.host = host;
        this.projector = projector;
    }

    void drawCaption(@Nullable PonderScene scene, int previewX, int previewY, int previewWidth, int previewHeight,
        float currentTick, float fade) {
        if (scene == null || projector == null) {
            return;
        }

        PonderScene.OverlayEvent overlayEvent = host.getShowcaseCaption(scene, currentTick);
        if (overlayEvent == null || overlayEvent.getText() == null || overlayEvent.getText().isEmpty()) {
            return;
        }

        float captionFade = host.computeCaptionFade(overlayEvent, currentTick) * fade;
        if (captionFade <= 0.0F) {
            return;
        }

        PreviewLayout layout = host.getLastPreviewLayout();
        if (layout == null) {
            return;
        }

        int maxTextWidth = Math.min(250, previewWidth - 92);
        List<String> lines = fontRenderer.listFormattedStringToWidth(overlayEvent.getText(), maxTextWidth);
        int longestLine = 0;
        for (String line : lines) {
            longestLine = Math.max(longestLine, fontRenderer.getStringWidth(line));
        }

        int boxWidth = Math.max(106, longestLine + 22);
        int boxHeight = 12 + lines.size() * 10;
        PonderScenePreview.PreviewBounds previewBounds = PonderScenePreview.computeBounds(scene);
        SceneBounds sceneBounds = new SceneBounds(previewBounds.minX, previewBounds.minY, previewBounds.minZ,
            previewBounds.maxX, previewBounds.maxY, previewBounds.maxZ);
        SceneProjectionContext context =
            SceneProjectionContext.of(scene, sceneBounds, layout, currentTick, projector);
        List<GuiOverlayPlacement> guiPlacements = PonderOverlayHelper.computeActiveGuiOverlayPlacements(context);
        List<GuiHighlightPlacement> guiHighlightPlacements =
            PonderOverlayHelper.computeActiveGuiHighlightPlacements(context, guiPlacements);
        SpeechRenderer.Point targetPoint =
            PonderOverlayHelper.resolveCaptionTargetPoint(context, overlayEvent, guiHighlightPlacements);

        int boxX;
        int boxY;
        SpeechRenderer.SpeechPointing pointing = SpeechRenderer.SpeechPointing.NONE;
        boolean manualX = overlayEvent.getCaptionX() != Integer.MIN_VALUE;
        boolean manualY = overlayEvent.getCaptionY() != Integer.MIN_VALUE;
        boolean manualPlacement = manualX || manualY;
        int maxBoxX = Math.max(previewX + 16, previewX + previewWidth - boxWidth - 16);
        int bottomClearance = targetPoint == null ? 132 : 116;
        int minBoxY = previewY + 74;
        int autoMaxBoxY = Math.max(minBoxY, previewY + previewHeight - boxHeight - bottomClearance);
        int manualMaxBoxY = Math.max(minBoxY, previewY + previewHeight - boxHeight - 12);

        if (targetPoint != null) {
            int preferredX = targetPoint.x - boxWidth / 2;
            boolean preferAbove = targetPoint.y >= previewY + Math.round(previewHeight * 0.58F);
            int preferredAboveY = targetPoint.y - boxHeight - (preferAbove ? 20 : 14);
            int preferredBelowY = targetPoint.y + (preferAbove ? 18 : 14);
            boolean canPlaceAbove = preferredAboveY >= minBoxY;
            boolean canPlaceBelow = !preferAbove && preferredBelowY <= autoMaxBoxY;

            boxX = MathHelper.clamp(preferredX, previewX + 16, maxBoxX);
            if ((preferAbove && canPlaceAbove) || !canPlaceBelow) {
                boxY = MathHelper.clamp(preferredAboveY, minBoxY, autoMaxBoxY);
                pointing = SpeechRenderer.SpeechPointing.DOWN;
            } else {
                boxY = MathHelper.clamp(preferredBelowY, minBoxY, autoMaxBoxY);
                pointing = SpeechRenderer.SpeechPointing.UP;
            }
        } else {
            boxX = previewX + (previewWidth - boxWidth) / 2;
            int preferredY = overlayEvent.getIndependentY() >= 0 ? previewY + 16 + overlayEvent.getIndependentY() * 2
                : autoMaxBoxY;
            boxY = MathHelper.clamp(preferredY, minBoxY, autoMaxBoxY);
        }

        if (manualX) {
            boxX = PonderOverlayHelper.resolveManualCaptionCoordinate(overlayEvent.getCaptionX(), previewX, previewWidth, boxWidth);
        }
        if (manualY) {
            boxY = PonderOverlayHelper.resolveManualCaptionCoordinate(overlayEvent.getCaptionY(), previewY, previewHeight, boxHeight);
        }
        boxX += overlayEvent.getCaptionOffsetX();
        boxY += overlayEvent.getCaptionOffsetY();

        int maxBoxY = manualPlacement ? manualMaxBoxY : autoMaxBoxY;
        boxX = MathHelper.clamp(boxX, previewX + 16, maxBoxX);
        boxY = MathHelper.clamp(boxY, minBoxY, maxBoxY);

        if (!manualPlacement) {
            int slide = (int) ((1.0F - captionFade) * 8.0F);
            boxY = MathHelper.clamp(boxY + slide, minBoxY, maxBoxY);
            CaptionPlacement adjustedPlacement =
                PonderOverlayHelper.avoidOverlayOverlap(boxX, boxY, boxWidth, boxHeight, pointing, previewY, maxBoxY, guiPlacements);
            boxX = adjustedPlacement.x();
            boxY = adjustedPlacement.y();
            pointing = adjustedPlacement.pointing();
        }

        if (!overlayEvent.isConnectorVisible() || targetPoint == null) {
            pointing = SpeechRenderer.SpeechPointing.NONE;
        } else if (manualPlacement) {
            pointing = PonderOverlayHelper.choosePointing(boxX, boxY, boxWidth, boxHeight, targetPoint);
        }

        host.drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, overlayEvent.getColor(), captionFade);
        SpeechRenderer.Point pointerTip = host.getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
        if (targetPoint != null && pointerTip != null) {
            host.drawCaptionConnector(pointerTip.x, pointerTip.y, targetPoint.x, targetPoint.y, overlayEvent.getColor(), captionFade);
        }

        for (int i = 0; i < lines.size(); i++) {
            host.drawCenteredStringNoShadow(lines.get(i), boxX + boxWidth / 2, boxY + 6 + i * 10, 0xF2F5F8);
        }
    }
}
