package net.createmod.ponder.foundation.instruction;

import java.util.function.Consumer;

import net.createmod.ponder.foundation.PonderScene;

public abstract class PonderInstruction {

    public boolean isBlocking() {
        return false;
    }

    public void reset(PonderScene scene) {
    }

    public void onScheduled(PonderScene scene) {
    }

    public abstract boolean isComplete();

    public abstract void tick(PonderScene scene);

    public static PonderInstruction simple(Consumer<PonderScene> callback) {
        return new Simple(callback);
    }

    private static final class Simple extends PonderInstruction {
        private final Consumer<PonderScene> callback;
        private boolean executed;

        private Simple(Consumer<PonderScene> callback) {
            this.callback = callback;
        }

        @Override
        public boolean isComplete() {
            return executed;
        }

        @Override
        public void tick(PonderScene scene) {
            if (executed) {
                return;
            }
            executed = true;
            if (callback != null) {
                callback.accept(scene);
            }
        }
    }
}
