package net.createmod.ponder.foundation.ui;

import java.nio.FloatBuffer;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

final class SnapshotGuiLayoutHelper {

    private SnapshotGuiLayoutHelper() {
    }

    static void refreshGuiReferences(GuiScreen gui, Minecraft mc, int width, int height) {
        SnapshotGuiReflectionHelper.setPossibleField(gui, "mc", mc);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146297_k", mc);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "fontRenderer", mc.fontRenderer);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146289_q", mc.fontRenderer);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "itemRender", mc.getRenderItem());
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146296_j", mc.getRenderItem());
        SnapshotGuiReflectionHelper.setPossibleField(gui, "width", Integer.valueOf(width));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146294_l", Integer.valueOf(width));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "height", Integer.valueOf(height));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146295_m", Integer.valueOf(height));
    }

    static void setGuiContainerPosition(GuiScreen gui, int x, int y) {
        SnapshotGuiReflectionHelper.setPossibleField(gui, "guiLeft", Integer.valueOf(x));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_147003_i", Integer.valueOf(x));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "guiTop", Integer.valueOf(y));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_147009_r", Integer.valueOf(y));
    }

    static void renderSnapshotFrame(Minecraft mc, int x, int y, int width, int height, Runnable drawAction) {
        try (SnapshotGuiFrameScope frameScope = SnapshotGuiFrameScope.open(mc, x, y, width, height)) {
            drawAction.run();
        }
    }

    private static final class SnapshotGuiFrameScope implements AutoCloseable {

        private final GLStateGuard matrixGuard;
        private final boolean previousBlendEnabled;
        private final int previousBlendSrc;
        private final int previousBlendDst;
        private final boolean previousAlphaEnabled;
        private final int previousAlphaFunc;
        private final float previousAlphaRef;
        private final float previousRed;
        private final float previousGreen;
        private final float previousBlue;
        private final float previousColorAlpha;
        private final GLStateGuard scissorGuard;
        private boolean closed;

        private SnapshotGuiFrameScope(Minecraft mc, int x, int y, int width, int height) {
            this.matrixGuard = GLStateGuard.matrix();
            this.previousBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            this.previousBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
            this.previousBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
            this.previousAlphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            this.previousAlphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
            this.previousAlphaRef = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
            FloatBuffer previousColor = captureCurrentColor();
            this.previousRed = previousColor.get(0);
            this.previousGreen = previousColor.get(1);
            this.previousBlue = previousColor.get(2);
            this.previousColorAlpha = previousColor.get(3);
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.scissorGuard = GLStateGuard.scissor(mc, x, y, width, height);
        }

        static SnapshotGuiFrameScope open(Minecraft mc, int x, int y, int width, int height) {
            return new SnapshotGuiFrameScope(mc, x, y, width, height);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            RuntimeException failure = null;

            failure = closeGuard(scissorGuard, failure);
            failure = restoreColor(failure);
            failure = restoreAlphaFunction(failure);
            failure = restoreBlendFunction(failure);
            failure = restoreAlpha(failure);
            failure = restoreBlend(failure);
            failure = closeGuard(matrixGuard, failure);

            if (failure != null) {
                throw failure;
            }
        }

        private static FloatBuffer captureCurrentColor() {
            FloatBuffer color = BufferUtils.createFloatBuffer(4);
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
            return color;
        }

        private RuntimeException restoreColor(RuntimeException failure) {
            try {
                GlStateManager.color(previousRed, previousGreen, previousBlue, previousColorAlpha);
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException restoreAlphaFunction(RuntimeException failure) {
            try {
                GlStateManager.alphaFunc(previousAlphaFunc, previousAlphaRef);
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException restoreBlendFunction(RuntimeException failure) {
            try {
                GlStateManager.blendFunc(previousBlendSrc, previousBlendDst);
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException restoreAlpha(RuntimeException failure) {
            try {
                if (previousAlphaEnabled) {
                    GlStateManager.enableAlpha();
                } else {
                    GlStateManager.disableAlpha();
                }
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException restoreBlend(RuntimeException failure) {
            try {
                if (previousBlendEnabled) {
                    GlStateManager.enableBlend();
                } else {
                    GlStateManager.disableBlend();
                }
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException closeGuard(GLStateGuard guard, RuntimeException failure) {
            try {
                guard.close();
            } catch (RuntimeException exception) {
                failure = addSuppressed(failure, exception);
            }
            return failure;
        }

        private RuntimeException addSuppressed(RuntimeException failure, RuntimeException exception) {
            if (failure == null) {
                return exception;
            }
            failure.addSuppressed(exception);
            return failure;
        }
    }
}
