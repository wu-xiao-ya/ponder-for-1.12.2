package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

final class InteractionHitCache {

    // --- Bounds records ---

    record PlaybackBarBounds(int x, int y, int width, int height) {}
    record ShowcaseHeaderIconBounds(int x, int y, int size) {}
    record ShowcaseGroupPopupBounds(int x, int y, int width, int height) {}
    record NextUpCardBounds(int x, int y, int width, int height) {}

    private LayoutCache layout = LayoutCache.EMPTY;
    private InteractionState interaction = InteractionState.DEFAULT;

    // --- Compatible getters ---

    PreviewLayout getPreviewLayout() {
        return layout.previewLayout();
    }

    int getShowcaseHeaderIconX() {
        return layout.showcaseHeaderIcon().x();
    }

    int getShowcaseHeaderIconY() {
        return layout.showcaseHeaderIcon().y();
    }

    int getShowcaseHeaderIconSize() {
        return layout.showcaseHeaderIcon().size();
    }

    boolean isShowcaseHeaderIconActive() {
        return layout.hasShowcaseHeaderIcon();
    }

    int getShowcaseGroupPopupX() {
        return layout.showcaseGroupPopup().x();
    }

    int getShowcaseGroupPopupY() {
        return layout.showcaseGroupPopup().y();
    }

    int getShowcaseGroupPopupWidth() {
        return layout.showcaseGroupPopup().width();
    }

    int getShowcaseGroupPopupHeight() {
        return layout.showcaseGroupPopup().height();
    }

   boolean isShowcaseGroupPopupActive() {
        return layout.hasShowcaseGroupPopup();
    }

    boolean isPreviewActive() {
        return layout.hasPreview();
    }

    boolean isPlaybackBarActive() {
        return layout.hasPlaybackBar();
    }

    boolean isNextUpCardActive() {
        return layout.hasNextUpCard();
    }

    List<ShowcaseGroupIconHitBox> getShowcaseGroupIcons() {
        return layout.showcaseGroupIcons();
    }

    int getNextUpCardX() {
        return layout.nextUpCard().x();
    }

    int getNextUpCardY() {
        return layout.nextUpCard().y();
    }

    int getNextUpCardWidth() {
        return layout.nextUpCard().width();
    }

    int getNextUpCardHeight() {
        return layout.nextUpCard().height();
    }

    int getPlaybackBarX() {
        return layout.playbackBar().x();
    }

    int getPlaybackBarY() {
        return layout.playbackBar().y();
    }

    int getPlaybackBarWidth() {
        return layout.playbackBar().width();
    }

    int getPlaybackBarHeight() {
        return layout.playbackBar().height();
    }

    boolean isPlaybackBarDragging() {
        return interaction.playbackBarDragging();
    }

    boolean isShowcaseGroupSelectorOpen() {
        return interaction.showcaseGroupSelectorOpen();
    }

    // --- Grouped bounds getters ---

    PlaybackBarBounds getPlaybackBarBounds() {
        return layout.playbackBar();
    }

    ShowcaseHeaderIconBounds getShowcaseHeaderIconBounds() {
        return layout.showcaseHeaderIcon();
    }

    ShowcaseGroupPopupBounds getShowcaseGroupPopupBounds() {
        return layout.showcaseGroupPopup();
    }

    NextUpCardBounds getNextUpCardBounds() {
        return layout.nextUpCard();
    }

    // --- Compatible setters ---

    void setPreviewLayout(PreviewLayout previewLayout) {
        this.layout = layout.withPreview(previewLayout);
    }

    void setShowcaseHeaderIconBounds(int x, int y, int size) {
        this.layout = layout.withShowcaseHeaderIcon(new ShowcaseHeaderIconBounds(x, y, size));
    }

    void setShowcaseGroupPopupState(List<ShowcaseGroupIconHitBox> icons, int x, int y, int width, int height) {
        List<ShowcaseGroupIconHitBox> safeIcons = icons == null
            ? Collections.<ShowcaseGroupIconHitBox>emptyList()
            : icons;
        this.layout = layout.withShowcaseGroupPopup(new ShowcaseGroupPopupBounds(x, y, width, height), safeIcons);
    }

    void setPlaybackBarBounds(int x, int y, int width, int height) {
        this.layout = layout.withPlaybackBar(new PlaybackBarBounds(x, y, width, height));
    }

    void setNextUpCardBounds(int x, int y, int width, int height) {
        this.layout = layout.withNextUpCard(new NextUpCardBounds(x, y, width, height));
    }

    void setPlaybackBarDragging(boolean dragging) {
        this.interaction = interaction.withPlaybackBarDragging(dragging);
    }

    void setShowcaseGroupSelectorOpen(boolean open) {
        this.interaction = interaction.withGroupSelectorOpen(open);
    }

    // --- Reset methods ---

    void resetShowcase() {
        this.layout = layout.clearPreviewAndShowcaseGroupIcons();
    }

    void resetPreview() {
        this.layout = layout.clearedPreview();
    }

    void resetPlaybackBar() {
        this.layout = layout.clearedPlaybackBar();
    }

    void resetShowcaseHeader() {
        this.layout = layout.clearedShowcaseHeader();
    }

    void resetShowcaseGroupPopup() {
        this.layout = layout.clearedShowcaseGroupPopup();
    }

    void resetNextUpCard() {
        this.layout = layout.clearedNextUpCard();
    }

    void resetDebug() {
        this.layout = layout.clearPreviewAndNextUp();
    }

    void resetAll() {
        this.layout = LayoutCache.EMPTY;
        this.interaction = InteractionState.DEFAULT;
    }

    // --- Hit-test methods ---

    ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
        for (ShowcaseGroupIconHitBox icon : layout.showcaseGroupIcons()) {
            if (isWithin(mouseX, mouseY, icon.x, icon.y, icon.x + icon.width, icon.y + icon.height)) {
                return icon;
            }
        }
        return null;
    }

    boolean isMouseInsideShowcaseGroupPopup(int mouseX, int mouseY) {
        return isWithin(mouseX, mouseY,
            layout.showcaseGroupPopup().x(), layout.showcaseGroupPopup().y(),
            layout.showcaseGroupPopup().x() + layout.showcaseGroupPopup().width(),
            layout.showcaseGroupPopup().y() + layout.showcaseGroupPopup().height());
    }

    boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        return layout.hasShowcaseHeaderIcon()
            && isWithin(mouseX, mouseY,
                layout.showcaseHeaderIcon().x(), layout.showcaseHeaderIcon().y(),
                layout.showcaseHeaderIcon().x() + layout.showcaseHeaderIcon().size(),
                layout.showcaseHeaderIcon().y() + layout.showcaseHeaderIcon().size());
    }

    boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return layout.hasPlaybackBar()
            && isWithin(mouseX, mouseY,
                layout.playbackBar().x(), layout.playbackBar().y() - 3,
                layout.playbackBar().x() + layout.playbackBar().width(),
                layout.playbackBar().y() + layout.playbackBar().height() + 4);
    }

    int estimatePlaybackTickForMouse(int mouseX, int maxTick) {
        if (!layout.hasPlaybackBar()) {
            return 0;
        }
        float progress = (mouseX - layout.playbackBar().x())
            / (float) Math.max(1, layout.playbackBar().width());
        progress = net.minecraft.util.math.MathHelper.clamp(progress, 0.0F, 1.0F);
        return Math.round(progress * maxTick);
    }

    int estimatePlaybackTickForMouse(int mouseX, PlaybackBarBounds bounds, int maxTick) {
        if (bounds == null || bounds.width() <= 0) {
            return 0;
        }
        float progress = (mouseX - bounds.x()) / (float) Math.max(1, bounds.width());
        progress = net.minecraft.util.math.MathHelper.clamp(progress, 0.0F, 1.0F);
        return Math.round(progress * maxTick);
    }

    boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        return layout.hasNextUpCard()
            && isWithin(mouseX, mouseY,
                layout.nextUpCard().x(), layout.nextUpCard().y(),
                layout.nextUpCard().x() + layout.nextUpCard().width(),
                layout.nextUpCard().y() + layout.nextUpCard().height() + 8);
    }

    boolean isMouseOverPreview(int mouseX, int mouseY) {
        return layout.hasPreview()
            && isWithin(mouseX, mouseY,
                layout.previewLayout().originX, layout.previewLayout().originY,
                layout.previewLayout().originX + layout.previewLayout().width,
                layout.previewLayout().originY + layout.previewLayout().height);
    }

    // --- Utility ---

    private static boolean isWithin(int mouseX, int mouseY, int minX, int minY, int maxX, int maxY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }
}
