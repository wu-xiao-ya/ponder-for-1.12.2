package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;

final class ActorPreviewStateScope implements AutoCloseable {

    private final GLStateGuard textureGuard;
    private final GLStateGuard cullGuard;
    private final GLStateGuard colorGuard;
    private final GLStateGuard blendGuard;
    private boolean closed;

    private ActorPreviewStateScope(GLStateGuard textureGuard, GLStateGuard cullGuard, GLStateGuard colorGuard,
        GLStateGuard blendGuard) {
        this.textureGuard = textureGuard;
        this.cullGuard = cullGuard;
        this.colorGuard = colorGuard;
        this.blendGuard = blendGuard;
    }

    static ActorPreviewStateScope open() {
        GLStateGuard textureGuard = null;
        GLStateGuard cullGuard = null;
        GLStateGuard colorGuard = null;
        GLStateGuard blendGuard = null;
        try {
            textureGuard = GLStateGuard.textureDisabled();
            cullGuard = GLStateGuard.cullDisabled();
            colorGuard = GLStateGuard.color(1.0F, 1.0F, 1.0F, 1.0F);
            blendGuard = GLStateGuard.blendEnabled();
            return new ActorPreviewStateScope(textureGuard, cullGuard, colorGuard, blendGuard);
        } catch (RuntimeException | Error exception) {
            closeScope(exception, blendGuard);
            closeScope(exception, colorGuard);
            closeScope(exception, cullGuard);
            closeScope(exception, textureGuard);
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
        failure = closeScope(failure, blendGuard);
        failure = closeScope(failure, colorGuard);
        failure = closeScope(failure, cullGuard);
        failure = closeScope(failure, textureGuard);
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
