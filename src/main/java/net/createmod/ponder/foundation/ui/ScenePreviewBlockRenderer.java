package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

final class ScenePreviewBlockRenderer {

    private ScenePreviewBlockRenderer() {
    }

    static void render(BlockRendererDispatcher dispatcher, PreviewBlockAccess previewWorld,
        @Nullable Method blockStateActualState, boolean showcaseMode, int previewFullBright, RuntimeBlockState block,
        IBlockState state) {
        if (state == null) {
            return;
        }

        IBlockState renderState = state;
        try {
            renderState = PonderPreviewRenderHelper.getActualStateCompat(blockStateActualState, state, previewWorld,
                block.pos);
        } catch (RuntimeException ignored) {
            renderState = state;
        }

        try (BlockPreviewScope previewScope = BlockPreviewScope.open(showcaseMode, previewFullBright, block)) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            try {
                dispatcher.renderBlock(renderState, block.pos, previewWorld, buffer);
            } catch (RuntimeException ignored) {
            }
            Tessellator.getInstance().draw();
        }
    }

    private static final class BlockPreviewScope implements AutoCloseable {

        private final GLStateGuard matrixGuard;
        private boolean closed;

        private BlockPreviewScope(GLStateGuard matrixGuard) {
            this.matrixGuard = matrixGuard;
        }

        static BlockPreviewScope open(boolean showcaseMode, int previewFullBright, RuntimeBlockState block) {
            GLStateGuard matrixGuard = null;
            try {
                matrixGuard = GLStateGuard.matrix();
                PonderSceneRuntime.applyRenderTransforms(block);

                float brightness = Math.min(1.0F, PonderPreviewRenderHelper.computeBlockBrightness(showcaseMode,
                    block));
                float alpha = MathHelper.clamp(0.28F + block.fade * 0.72F, 0.0F, 1.0F);
                GlStateManager.color(brightness, brightness, brightness, alpha);
                PonderPreviewRenderHelper.setPreviewLightmap(previewFullBright);
                return new BlockPreviewScope(matrixGuard);
            } catch (RuntimeException | Error exception) {
                closeScope(exception, matrixGuard);
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
