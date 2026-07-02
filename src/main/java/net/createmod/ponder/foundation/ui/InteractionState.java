package net.createmod.ponder.foundation.ui;

record InteractionState(
    boolean showcaseGroupSelectorOpen,
    boolean playbackBarDragging
) {
    static final InteractionState DEFAULT = new InteractionState(false, false);

    InteractionState withGroupSelectorOpen(boolean open) {
        return new InteractionState(open, playbackBarDragging);
    }

    InteractionState withPlaybackBarDragging(boolean dragging) {
        return new InteractionState(showcaseGroupSelectorOpen, dragging);
    }
}
