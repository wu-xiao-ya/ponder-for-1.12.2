package net.createmod.ponder.foundation.ui;


final class PonderPlaybackState {

    private int playbackTick;
    private boolean playing;

    int getPlaybackTick() {
        return playbackTick;
    }

    void setPlaybackTick(int playbackTick) {
        this.playbackTick = playbackTick;
    }

    boolean isPlaying() {
        return playing;
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
    }

    void togglePlaying() {
        playing = !playing;
    }

    void resetToStart(boolean autoPlay) {
        this.playbackTick = 0;
        this.playing = autoPlay;
    }

    void stop() {
        this.playing = false;
    }

    float getRenderTick(int maxTick, float partialTicks) {
        if (!playing) {
            return playbackTick;
        }
        return Math.min(maxTick, playbackTick + Math.max(0.0F, partialTicks));
    }

}
