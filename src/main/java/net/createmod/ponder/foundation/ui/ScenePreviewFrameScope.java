package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;

final class ScenePreviewFrameScope implements AutoCloseable {

    private final GLStateGuard scissorGuard;
    private final ScenePreviewStateScope previewState;
    private final GLStateGuard matrixGuard;
    private boolean closed;

    private ScenePreviewFrameScope(GLStateGuard scissorGuard, ScenePreviewStateScope previewState,
        GLStateGuard matrixGuard) {
        this.scissorGuard = scissorGuard;
        this.previewState = previewState;
        this.matrixGuard = matrixGuard;
    }

    static ScenePreviewFrameScope open(Minecraft minecraft, int originX, int originY, int width, int height) {
        GLStateGuard scissorGuard = null;
        ScenePreviewStateScope previewState = null;
        GLStateGuard matrixGuard = null;
        try {
            scissorGuard = GLStateGuard.scissor(minecraft, originX, originY, width, height);
            previewState = ScenePreviewStateScope.open(minecraft);
            matrixGuard = previewState.matrix();
            return new ScenePreviewFrameScope(scissorGuard, previewState, matrixGuard);
        } catch (RuntimeException | Error exception) {
            closeScope(exception, matrixGuard);
            closeScope(exception, previewState);
            closeScope(exception, scissorGuard);
            throw exception;
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        Throwable failure = null;
        failure = closeScope(failure, matrixGuard);
        failure = closeScope(failure, previewState);
        failure = closeScope(failure, scissorGuard);
        rethrowScopeFailure(failure);
    }

    private static Throwable closeScope(Throwable failure, GLStateGuard guard) {
        if (guard == null) {
            return failure;
        }

        try {
            guard.close();
        } catch (RuntimeException | Error closeFailure) {
            return addScopeFailure(failure, closeFailure);
        }
        return failure;
    }

    private static Throwable closeScope(Throwable failure, ScenePreviewStateScope scope) {
        if (scope == null) {
            return failure;
        }

        try {
            scope.close();
        } catch (RuntimeException | Error closeFailure) {
            return addScopeFailure(failure, closeFailure);
        }
        return failure;
    }

    private static Throwable addScopeFailure(Throwable failure, Throwable closeFailure) {
        if (failure == null) {
            return closeFailure;
        }
        failure.addSuppressed(closeFailure);
        return failure;
    }

    private static void rethrowScopeFailure(Throwable failure) {
        if (failure == null) {
            return;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        throw (Error) failure;
    }
}
