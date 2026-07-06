package net.createmod.ponder.foundation.ui;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.renderer.GlStateManager;

final class ActorPreviewRenderPass implements AutoCloseable {

    private final float currentTick;
    private final List<PonderSceneRuntime.ActorRuntimeState> actors;
    private final ActorPreviewStateScope actorStateScope;
    private boolean closed;

    private ActorPreviewRenderPass(float currentTick, List<PonderSceneRuntime.ActorRuntimeState> actors,
        ActorPreviewStateScope actorStateScope) {
        this.currentTick = currentTick;
        this.actors = actors;
        this.actorStateScope = actorStateScope;
    }

    static void render(PonderScene scene, float currentTick) {
        ActorPreviewRenderPass renderPass = open(scene, currentTick);
        if (renderPass == null) {
            return;
        }

        try (ActorPreviewRenderPass pass = renderPass) {
            pass.render();
        }
    }

    @Nullable
    private static ActorPreviewRenderPass open(PonderScene scene, float currentTick) {
        List<PonderSceneRuntime.ActorRuntimeState> actors = PonderSceneRuntime.buildActorStates(scene, currentTick);
        if (actors.isEmpty()) {
            return null;
        }

        return new ActorPreviewRenderPass(currentTick, actors, ActorPreviewStateScope.open());
    }

    private void render() {
        for (PonderSceneRuntime.ActorRuntimeState actor : actors) {
            if (!actor.visible || actor.fade <= 0.0F) {
                continue;
            }
            renderActor(actor);
        }
    }

    private void renderActor(PonderSceneRuntime.ActorRuntimeState actor) {
        ActorPreviewAppearance.ActorPreviewRenderData renderData = ActorPreviewAppearance.resolve(actor, currentTick);

        try (GLStateGuard matrixGuard = GLStateGuard.matrix()) {
            GlStateManager.translate(actor.position.x, actor.position.y + renderData.bobOffset, actor.position.z);
            GlStateManager.rotate(renderData.yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) actor.rotation.x, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) actor.rotation.z, 0.0F, 0.0F, 1.0F);
            ActorPreviewBodyRenderer.render(actor.kind, renderData);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        Throwable failure = null;
        failure = closeScope(failure, actorStateScope);
        rethrowScopeFailure(failure);
    }

    private static Throwable closeScope(Throwable failure, ActorPreviewStateScope scope) {
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
