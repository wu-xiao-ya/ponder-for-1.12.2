package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;

final class ScenePreviewRenderer {

    private final Minecraft minecraft;
    private final boolean showcaseMode;
    private final int previewFullBright;
    @Nullable
    private final Method blockStateActualState;
    private final Map<String, TileEntity> tileEntityPreviewCache = new LinkedHashMap<String, TileEntity>();

    ScenePreviewRenderer(Minecraft minecraft, boolean showcaseMode, int previewFullBright,
        @Nullable Method blockStateActualState) {
        this.minecraft = minecraft;
        this.showcaseMode = showcaseMode;
        this.previewFullBright = previewFullBright;
        this.blockStateActualState = blockStateActualState;
    }

    void clearTileEntityPreviewCache() {
        tileEntityPreviewCache.clear();
    }

    PreviewBlockAccess createPreviewWorld(PonderSceneRuntimeTypes.RuntimeState runtimeState) {
        return new PreviewBlockAccess(runtimeState, block -> getOrCreatePreviewTileEntity(block, block.currentState));
    }

    void setPreviewLightmap() {
        PonderPreviewRenderHelper.setPreviewLightmap(previewFullBright);
    }

    void renderPreview(PonderScene scene, PreviewBounds bounds, PonderSceneRuntimeTypes.RuntimeState runtimeState,
        PreviewLayout layout, float renderTick, PonderPreviewCameraState cameraState,
        PonderOverlayLayoutHelper overlayLayoutHelper) {
        try (ScenePreviewFrameScope previewFrame = ScenePreviewFrameScope.open(minecraft, layout.originX, layout.originY,
            layout.width, layout.height)) {
            minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            float viewportCenterX = layout.originX + layout.width / 2.0F;
            float viewportAnchorY = overlayLayoutHelper.getPreviewAnchorY(layout);
            float scale = overlayLayoutHelper.computePreviewScale(scene, bounds, layout);
            float centerX = (bounds.minX + bounds.maxX + 1) / 2.0F;
            float centerY = (bounds.minY + bounds.maxY + 1) / 2.0F;
            float centerZ = (bounds.minZ + bounds.maxZ + 1) / 2.0F;
            float animatedYaw = cameraState.getPreviewYaw() + PonderSceneRuntime.getCameraYaw(scene, renderTick);

            GlStateManager.translate(viewportCenterX, viewportAnchorY, 190.0F);
            GlStateManager.scale(scale, -scale, scale);
            GlStateManager.rotate(cameraState.getPreviewPitch(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(animatedYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-centerX, -centerY - scene.getSceneOffsetY(), -centerZ);
            renderPreviewScenePass(scene, bounds, runtimeState, renderTick);
        }
    }

    private static final class ScenePreviewFrameScope implements AutoCloseable {

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
    }

    private void renderPreviewScenePass(PonderScene scene, PreviewBounds bounds,
        PonderSceneRuntimeTypes.RuntimeState runtimeState, float renderTick) {
        ScenePreviewShadowRenderer.draw(bounds, showcaseMode);

        BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
        PreviewBlockAccess previewWorld = createPreviewWorld(runtimeState);
        List<PonderSceneRuntimeTypes.RuntimeBlockState> visibleBlocks =
            new ArrayList<PonderSceneRuntimeTypes.RuntimeBlockState>(runtimeState.blocksByPosition().values());
        Collections.sort(visibleBlocks, new Comparator<PonderSceneRuntimeTypes.RuntimeBlockState>() {
            @Override
            public int compare(PonderSceneRuntimeTypes.RuntimeBlockState left,
                PonderSceneRuntimeTypes.RuntimeBlockState right) {
                int yCompare = Double.compare(left.renderCenterY, right.renderCenterY);
                if (yCompare != 0) {
                    return yCompare;
                }
                int zCompare = Double.compare(left.renderCenterZ, right.renderCenterZ);
                if (zCompare != 0) {
                    return zCompare;
                }
                return Double.compare(left.renderCenterX, right.renderCenterX);
            }
        });

        setPreviewLightmap();
        for (PonderSceneRuntimeTypes.RuntimeBlockState block : visibleBlocks) {
            if (!block.visible || block.fade <= 0.0F) {
                continue;
            }

            IBlockState state = block.currentState;
            if (state == null) {
                continue;
            }

            ScenePreviewBlockRenderer.render(dispatcher, previewWorld, blockStateActualState, showcaseMode,
                previewFullBright, block, state);
            renderTileEntityPreview(block, state);
        }

        renderActorPreviews(scene, renderTick);
    }

    void renderTileEntityPreview(RuntimeBlockState block, IBlockState state) {
        if (state == null || minecraft == null || minecraft.world == null || !state.getBlock().hasTileEntity(state)) {
            return;
        }

        TileEntity tileEntity = getOrCreatePreviewTileEntity(block, state);
        if (tileEntity == null) {
            return;
        }

        ScenePreviewTileEntityRenderer.render(minecraft, previewFullBright, block, tileEntity);
    }

    @Nullable
    private TileEntity getOrCreatePreviewTileEntity(RuntimeBlockState block, IBlockState state) {
        if (state == null || minecraft == null || minecraft.world == null) {
            return null;
        }

        String cacheKey = block.pos.toLong() + "|" + state.toString() + "|"
            + (block.tileNbt == null ? "" : block.tileNbt);
        TileEntity cached = tileEntityPreviewCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            TileEntity created = state.getBlock().createTileEntity(minecraft.world, state);
            if (created == null) {
                return null;
            }
            created.setWorld(minecraft.world);
            created.setPos(block.pos);
            if (block.tileNbt != null && !block.tileNbt.trim().isEmpty()) {
                PonderPreviewRenderHelper.applyTileNbt(created, block.tileNbt);
            }
            tileEntityPreviewCache.put(cacheKey, created);
            return created;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void renderActorPreviews(PonderScene scene, float currentTick) {
        ActorPreviewRenderPass.render(this, scene, currentTick);
    }

    private static final class ActorPreviewStateScope implements AutoCloseable {

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
    }

    private static final class ActorPreviewRenderPass implements AutoCloseable {

        private final ScenePreviewRenderer renderer;
        private final float currentTick;
        private final List<PonderSceneRuntime.ActorRuntimeState> actors;
        private final ActorPreviewStateScope actorStateScope;
        private boolean closed;

        private ActorPreviewRenderPass(ScenePreviewRenderer renderer, float currentTick,
            List<PonderSceneRuntime.ActorRuntimeState> actors, ActorPreviewStateScope actorStateScope) {
            this.renderer = renderer;
            this.currentTick = currentTick;
            this.actors = actors;
            this.actorStateScope = actorStateScope;
        }

        static void render(ScenePreviewRenderer renderer, PonderScene scene, float currentTick) {
            ActorPreviewRenderPass renderPass = open(renderer, scene, currentTick);
            if (renderPass == null) {
                return;
            }

            try (ActorPreviewRenderPass pass = renderPass) {
                pass.render();
            }
        }

        @Nullable
        private static ActorPreviewRenderPass open(ScenePreviewRenderer renderer, PonderScene scene, float currentTick) {
            List<PonderSceneRuntime.ActorRuntimeState> actors = PonderSceneRuntime.buildActorStates(scene, currentTick);
            if (actors.isEmpty()) {
                return null;
            }

            return new ActorPreviewRenderPass(renderer, currentTick, actors, ActorPreviewStateScope.open());
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
            ActorPreviewAppearance.ActorPreviewRenderData renderData = ActorPreviewAppearance.resolve(actor,
                currentTick);

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
