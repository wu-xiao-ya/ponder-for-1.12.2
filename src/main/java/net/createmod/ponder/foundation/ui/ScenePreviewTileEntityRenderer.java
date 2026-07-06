package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.Vec3iAccessor;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

final class ScenePreviewTileEntityRenderer {

    private ScenePreviewTileEntityRenderer() {
    }

    @SuppressWarnings("unchecked")
    static void render(Minecraft minecraft, int previewFullBright, RuntimeBlockState block, TileEntity tileEntity) {
        if (minecraft == null || minecraft.world == null || tileEntity == null) {
            return;
        }

        try {
            TileEntityPreviewScope previewScope = TileEntityPreviewScope.open(minecraft, previewFullBright, block,
                tileEntity);
            if (previewScope == null) {
                return;
            }
            try (TileEntityPreviewScope activeScope = previewScope) {
                activeScope.render(tileEntity);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static final class TileEntityPreviewScope implements AutoCloseable {

        private final GLStateGuard matrixGuard;
        private final TileEntitySpecialRenderer<TileEntity> renderer;
        private boolean closed;

        private TileEntityPreviewScope(GLStateGuard matrixGuard, TileEntitySpecialRenderer<TileEntity> renderer) {
            this.matrixGuard = matrixGuard;
            this.renderer = renderer;
        }

        @Nullable
        static TileEntityPreviewScope open(Minecraft minecraft, int previewFullBright, RuntimeBlockState block,
            TileEntity tileEntity) {
            tileEntity.setWorld(minecraft.world);
            tileEntity.setPos(block.pos);

            TileEntitySpecialRenderer<TileEntity> tileEntityRenderer =
                TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
            if (tileEntityRenderer == null) {
                return null;
            }

            GLStateGuard matrixGuard = GLStateGuard.matrix();
            boolean colorApplied = false;
            try {
                PonderSceneRuntime.applyRenderTransforms(block);
                GlStateManager.translate(Vec3iAccessor.x(block.pos), Vec3iAccessor.y(block.pos),
                    Vec3iAccessor.z(block.pos));
                PonderPreviewRenderHelper.setPreviewLightmap(previewFullBright);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                colorApplied = true;
                return new TileEntityPreviewScope(matrixGuard, tileEntityRenderer);
            } catch (RuntimeException | Error exception) {
                if (colorApplied) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
                closeScope(exception, matrixGuard);
                throw exception;
            }
        }

        void render(TileEntity tileEntity) {
            renderer.render(tileEntity, 0.0D, 0.0D, 0.0D, 0.0F, -1, 1.0F);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;

            Throwable failure = null;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            failure = closeScope(failure, matrixGuard);
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
