package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;

final class ScenePreviewStateScope implements AutoCloseable {

    private final Minecraft minecraft;
    private final GLStateGuard itemLightingGuard;
    private final int previousAmbientOcclusion;
    private final boolean restoreAmbientOcclusion;
    private final boolean hasMinecraft;
    private boolean closed;

    private ScenePreviewStateScope(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.hasMinecraft = minecraft != null;
        this.restoreAmbientOcclusion = hasMinecraft && minecraft.gameSettings != null;
        this.previousAmbientOcclusion = restoreAmbientOcclusion ? minecraft.gameSettings.ambientOcclusion : 0;

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
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
        }
    }
}
