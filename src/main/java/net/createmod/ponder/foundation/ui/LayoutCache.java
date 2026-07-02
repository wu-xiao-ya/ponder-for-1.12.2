package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

record LayoutCache(
    PreviewLayout previewLayout,
    InteractionHitCache.PlaybackBarBounds playbackBar,
    InteractionHitCache.ShowcaseHeaderIconBounds showcaseHeaderIcon,
    InteractionHitCache.ShowcaseGroupPopupBounds showcaseGroupPopup,
    InteractionHitCache.NextUpCardBounds nextUpCard,
    List<ShowcaseGroupIconHitBox> showcaseGroupIcons
) {
    static final LayoutCache EMPTY = new LayoutCache(
        null,
        new InteractionHitCache.PlaybackBarBounds(0, 0, 0, 0),
        new InteractionHitCache.ShowcaseHeaderIconBounds(0, 0, 0),
        new InteractionHitCache.ShowcaseGroupPopupBounds(0, 0, 0, 0),
        new InteractionHitCache.NextUpCardBounds(0, 0, 0, 0),
        Collections.emptyList()
    );

    boolean hasPreview() {
        return previewLayout != null;
    }

    boolean hasPlaybackBar() {
        return playbackBar.width() > 0;
    }

    boolean hasShowcaseHeaderIcon() {
        return showcaseHeaderIcon.size() > 0;
    }

    boolean hasShowcaseGroupPopup() {
        return showcaseGroupPopup.width() > 0 && showcaseGroupPopup.height() > 0;
    }

    boolean hasNextUpCard() {
        return nextUpCard.width() > 0;
    }

    LayoutCache withPreview(PreviewLayout pl) {
        return new LayoutCache(pl, playbackBar, showcaseHeaderIcon, showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache withPlaybackBar(InteractionHitCache.PlaybackBarBounds bb) {
        return new LayoutCache(previewLayout, bb, showcaseHeaderIcon, showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache withShowcaseHeaderIcon(InteractionHitCache.ShowcaseHeaderIconBounds sh) {
        return new LayoutCache(previewLayout, playbackBar, sh, showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache withShowcaseGroupPopup(InteractionHitCache.ShowcaseGroupPopupBounds gp, List<ShowcaseGroupIconHitBox> icons) {
        return new LayoutCache(previewLayout, playbackBar, showcaseHeaderIcon, gp, nextUpCard, icons);
    }

    LayoutCache withNextUpCard(InteractionHitCache.NextUpCardBounds nc) {
        return new LayoutCache(previewLayout, playbackBar, showcaseHeaderIcon, showcaseGroupPopup, nc, showcaseGroupIcons);
    }

    LayoutCache clearPreviewAndShowcaseGroupIcons() {
        return new LayoutCache(
            null,
            playbackBar,
            new InteractionHitCache.ShowcaseHeaderIconBounds(showcaseHeaderIcon.x(), showcaseHeaderIcon.y(), 0),
            new InteractionHitCache.ShowcaseGroupPopupBounds(showcaseGroupPopup.x(), showcaseGroupPopup.y(), 0, 0),
            new InteractionHitCache.NextUpCardBounds(nextUpCard.x(), nextUpCard.y(), 0, 0),
            Collections.emptyList()
        );
    }

    LayoutCache clearPreviewAndNextUp() {
        return new LayoutCache(
            null,
            playbackBar,
            showcaseHeaderIcon,
            showcaseGroupPopup,
            new InteractionHitCache.NextUpCardBounds(nextUpCard.x(), nextUpCard.y(), 0, 0),
            showcaseGroupIcons
        );
    }

    LayoutCache clearedPreview() {
        return new LayoutCache(null, playbackBar, showcaseHeaderIcon, showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache clearedPlaybackBar() {
        return new LayoutCache(previewLayout, new InteractionHitCache.PlaybackBarBounds(0, 0, 0, 0), showcaseHeaderIcon,
            showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache clearedShowcaseHeader() {
        return new LayoutCache(previewLayout, playbackBar,
            new InteractionHitCache.ShowcaseHeaderIconBounds(showcaseHeaderIcon.x(), showcaseHeaderIcon.y(), 0),
            showcaseGroupPopup, nextUpCard, showcaseGroupIcons);
    }

    LayoutCache clearedShowcaseGroupPopup() {
        return new LayoutCache(previewLayout, playbackBar, showcaseHeaderIcon,
            new InteractionHitCache.ShowcaseGroupPopupBounds(showcaseGroupPopup.x(), showcaseGroupPopup.y(), 0, 0),
            nextUpCard, Collections.emptyList());
    }

    LayoutCache clearedNextUpCard() {
        return new LayoutCache(previewLayout, playbackBar, showcaseHeaderIcon, showcaseGroupPopup,
            new InteractionHitCache.NextUpCardBounds(nextUpCard.x(), nextUpCard.y(), 0, 0), showcaseGroupIcons);
    }
}
