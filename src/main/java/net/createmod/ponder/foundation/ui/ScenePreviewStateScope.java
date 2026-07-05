package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

final class ScenePreviewStateScope implements AutoCloseable {

    private final Minecraft minecraft;
    private final GLStateGuard itemLightingGuard;
    private final int previousAmbientOcclusion;
    private final boolean previousDepthEnabled;
    private final boolean previousDepthMask;
    private final boolean previousAlphaEnabled;
    private final int previousAlphaFunc;
    private final float previousAlphaRef;
    private final boolean previousBlendEnabled;
    private final int previousBlendSrc;
    private final int previousBlendDst;
    private final boolean previousLightingEnabled;
    private final boolean previousRescaleNormalEnabled;
    private final int previousShadeModel;
    private final boolean restoreAmbientOcclusion;
    private final boolean hasMinecraft;
    private boolean closed;

    private ScenePreviewStateScope(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.hasMinecraft = minecraft != null;
        this.restoreAmbientOcclusion = hasMinecraft && minecraft.gameSettings != null;
        this.previousAmbientOcclusion = restoreAmbientOcclusion ? minecraft.gameSettings.ambientOcclusion : 0;
        this.previousDepthEnabled = hasMinecraft && GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        this.previousDepthMask = hasMinecraft ? GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK) : true;
        this.previousAlphaEnabled = hasMinecraft && GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        this.previousAlphaFunc = hasMinecraft ? GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC) : GL11.GL_ALWAYS;
        this.previousAlphaRef = hasMinecraft ? GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF) : 0.0F;
        this.previousBlendEnabled = hasMinecraft && GL11.glIsEnabled(GL11.GL_BLEND);
        this.previousBlendSrc = hasMinecraft ? GL11.glGetInteger(GL11.GL_BLEND_SRC) : GL11.GL_ONE;
        this.previousBlendDst = hasMinecraft ? GL11.glGetInteger(GL11.GL_BLEND_DST) : GL11.GL_ZERO;
        this.previousLightingEnabled = hasMinecraft && GL11.glIsEnabled(GL11.GL_LIGHTING);
        this.previousRescaleNormalEnabled = hasMinecraft && GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        this.previousShadeModel = hasMinecraft ? GL11.glGetInteger(GL11.GL_SHADE_MODEL) : GL11.GL_FLAT;

        if (hasMinecraft) {
            if (restoreAmbientOcclusion) {
                minecraft.gameSettings.ambientOcclusion = 0;
            }

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            this.itemLightingGuard = GLStateGuard.guiItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            if (minecraft.entityRenderer != null) {
                minecraft.entityRenderer.enableLightmap();
            }
        } else {
            this.itemLightingGuard = GLStateGuard.noop();
        }
    }

    static ScenePreviewStateScope open(Minecraft minecraft) {
        return new ScenePreviewStateScope(minecraft);
    }

    GLStateGuard matrix() {
        return GLStateGuard.matrix();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (hasMinecraft) {
            if (minecraft.entityRenderer != null) {
                minecraft.entityRenderer.disableLightmap();
            }
            if (restoreAmbientOcclusion) {
                minecraft.gameSettings.ambientOcclusion = previousAmbientOcclusion;
            }
            itemLightingGuard.close();
            restorePreviewGlState();
        }
    }

    private void restorePreviewGlState() {
        GlStateManager.shadeModel(previousShadeModel);
        GlStateManager.blendFunc(previousBlendSrc, previousBlendDst);
        GlStateManager.alphaFunc(previousAlphaFunc, previousAlphaRef);
        GlStateManager.depthMask(previousDepthMask);
        restoreLighting();
        restoreRescaleNormal();
        restoreBlend();
        restoreAlpha();
        restoreDepth();
    }

    private void restoreDepth() {
        if (previousDepthEnabled) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
    }

    private void restoreAlpha() {
        if (previousAlphaEnabled) {
            GlStateManager.enableAlpha();
        } else {
            GlStateManager.disableAlpha();
        }
    }

    private void restoreBlend() {
        if (previousBlendEnabled) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
    }

    private void restoreLighting() {
        if (previousLightingEnabled) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }
    }

    private void restoreRescaleNormal() {
        if (previousRescaleNormalEnabled) {
            GlStateManager.enableRescaleNormal();
        } else {
            GlStateManager.disableRescaleNormal();
        }
    }
}
