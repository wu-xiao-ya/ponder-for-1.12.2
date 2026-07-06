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
import net.createmod.ponder.foundation.Vec3iAccessor;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

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

        private ScenePreviewFrameScope(Minecraft minecraft, int originX, int originY, int width, int height) {
            this.scissorGuard = GLStateGuard.scissor(minecraft, originX, originY, width, height);
            this.previewState = ScenePreviewStateScope.open(minecraft);
            this.matrixGuard = previewState.matrix();
        }

        static ScenePreviewFrameScope open(Minecraft minecraft, int originX, int originY, int width, int height) {
            return new ScenePreviewFrameScope(minecraft, originX, originY, width, height);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;

            matrixGuard.close();
            previewState.close();
            scissorGuard.close();
        }
    }

    private void renderPreviewScenePass(PonderScene scene, PreviewBounds bounds,
        PonderSceneRuntimeTypes.RuntimeState runtimeState, float renderTick) {
        drawSceneShadow(bounds);

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

            renderBlockModelPreview(dispatcher, previewWorld, block, state);
            renderTileEntityPreview(block, state);
        }

        renderActorPreviews(scene, renderTick);
    }

    void renderBlockModelPreview(BlockRendererDispatcher dispatcher, PreviewBlockAccess previewWorld,
        RuntimeBlockState block, IBlockState state) {
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

        try (GLStateGuard matrixGuard = GLStateGuard.matrix()) {
            PonderSceneRuntime.applyRenderTransforms(block);
            float brightness = Math.min(1.0F, PonderPreviewRenderHelper.computeBlockBrightness(showcaseMode, block));
            float alpha = MathHelper.clamp(0.28F + block.fade * 0.72F, 0.0F, 1.0F);
            GlStateManager.color(brightness, brightness, brightness, alpha);
            setPreviewLightmap();

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            try {
                dispatcher.renderBlock(renderState, block.pos, previewWorld, buffer);
            } catch (RuntimeException ignored) {
            }
            Tessellator.getInstance().draw();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SuppressWarnings("unchecked")
    void renderTileEntityPreview(RuntimeBlockState block, IBlockState state) {
        if (state == null || minecraft == null || minecraft.world == null || !state.getBlock().hasTileEntity(state)) {
            return;
        }

        TileEntity tileEntity = getOrCreatePreviewTileEntity(block, state);
        if (tileEntity == null) {
            return;
        }

        try {
            tileEntity.setWorld(minecraft.world);
            tileEntity.setPos(block.pos);
            TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
            if (renderer == null) {
                return;
            }

            try (GLStateGuard matrixGuard = GLStateGuard.matrix()) {
                PonderSceneRuntime.applyRenderTransforms(block);
                GlStateManager.translate(Vec3iAccessor.x(block.pos), Vec3iAccessor.y(block.pos),
                    Vec3iAccessor.z(block.pos));
                setPreviewLightmap();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                renderer.render(tileEntity, 0.0D, 0.0D, 0.0D, 0.0F, -1, 1.0F);
            }
        } catch (RuntimeException ignored) {
        }
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

    private void drawSceneShadow(PreviewBounds bounds) {
        float minX = bounds.minX - 0.35F;
        float maxX = bounds.maxX + 1.35F;
        float minZ = bounds.minZ - 0.35F;
        float maxZ = bounds.maxZ + 1.35F;
        float y = bounds.minY + 0.01F;

        try (GLStateGuard textureGuard = GLStateGuard.textureDisabled();
            GLStateGuard colorGuard = GLStateGuard.color(0.0F, 0.0F, 0.0F, showcaseMode ? 0.12F : 0.06F)) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(minX, y, minZ);
            GL11.glVertex3f(maxX, y, minZ);
            GL11.glVertex3f(maxX, y, maxZ);
            GL11.glVertex3f(minX, y, maxZ);
            GL11.glEnd();
        }
    }

    private void renderActorPreviews(PonderScene scene, float currentTick) {
        List<PonderSceneRuntime.ActorRuntimeState> actors = PonderSceneRuntime.buildActorStates(scene, currentTick);
        if (actors.isEmpty()) {
            return;
        }

        try (GLStateGuard textureGuard = GLStateGuard.textureDisabled();
            GLStateGuard cullGuard = GLStateGuard.cullDisabled();
            GLStateGuard colorGuard = GLStateGuard.color(1.0F, 1.0F, 1.0F, 1.0F);
            GLStateGuard blendGuard = GLStateGuard.blendEnabled()) {
            for (PonderSceneRuntime.ActorRuntimeState actor : actors) {
                if (!actor.visible || actor.fade <= 0.0F) {
                    continue;
                }
                renderActorPreview(actor, currentTick);
            }
        }
    }

    private void renderActorPreview(PonderSceneRuntime.ActorRuntimeState actor, float currentTick) {
        float alpha = MathHelper.clamp(actor.fade, 0.0F, 1.0F);
        int color = getActorBaseColor(actor);
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float bobOffset = getActorBobOffset(actor, currentTick);
        float yaw = actor.kind == PonderScene.ActorKind.CART ? actor.cartYaw : (float) actor.rotation.y;
        yaw += getActorYawOffset(actor, currentTick);

        try (GLStateGuard matrixGuard = GLStateGuard.matrix()) {
            GlStateManager.translate(actor.position.x, actor.position.y + bobOffset, actor.position.z);
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) actor.rotation.x, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) actor.rotation.z, 0.0F, 0.0F, 1.0F);

            if (actor.kind == PonderScene.ActorKind.BIRB) {
                drawBirbBody(actor, red, green, blue, alpha, currentTick);
                drawActorHeading(alpha);
            } else if (actor.kind == PonderScene.ActorKind.ITEM) {
                drawItemBody(red, green, blue, alpha, currentTick);
                drawActorHeading(alpha * 0.7F);
            } else {
                drawActorPrism(red, green, blue, alpha, 0.30F, 0.16F, 0.18F);
                drawCartWheels(alpha);
                drawActorHeading(alpha);
            }
        }
    }

    private void drawActorHeading(float alpha) {
        int lineColor = withAlpha(0xF2F5F8, alpha * 235.0F);
        float red = ((lineColor >> 16) & 0xFF) / 255.0F;
        float green = ((lineColor >> 8) & 0xFF) / 255.0F;
        float blue = (lineColor & 0xFF) / 255.0F;
        float lineAlpha = ((lineColor >>> 24) & 0xFF) / 255.0F;

        try (GLStateGuard colorGuard = GLStateGuard.color(red, green, blue, lineAlpha);
            GLStateGuard lineWidthGuard = GLStateGuard.lineWidth(2.0F)) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex3f(0.0F, 0.05F, 0.0F);
            GL11.glVertex3f(0.0F, 0.05F, -0.38F);
            GL11.glEnd();
        }
    }

    private void drawBirbBody(PonderSceneRuntime.ActorRuntimeState actor, float red, float green, float blue, float alpha,
        float currentTick) {
        if (isBirbDancePose(actor)) {
            drawActorPrism(red, green, blue, alpha, 0.16F, 0.28F, 0.16F);
            float wing = 0.04F + Math.abs(MathHelper.sin(currentTick * 0.35F)) * 0.05F;
            drawActorPrism(red * 0.92F, green * 0.92F, blue, alpha * 0.92F, wing, 0.12F, 0.04F, -0.18F, 0.08F, 0.0F);
            drawActorPrism(red * 0.92F, green * 0.92F, blue, alpha * 0.92F, wing, 0.12F, 0.04F, 0.18F, 0.08F, 0.0F);
            return;
        }
        if (isBirbCursorPose(actor)) {
            drawActorPrism(red, green, blue, alpha, 0.13F, 0.34F, 0.13F);
            drawActorPrism(red * 0.85F, green * 0.95F, blue, alpha * 0.9F, 0.04F, 0.10F, 0.10F, 0.0F, 0.26F, -0.14F);
            return;
        }
        drawActorPrism(red, green, blue, alpha, 0.14F, 0.34F, 0.14F);
    }

    private void drawItemBody(float red, float green, float blue, float alpha, float currentTick) {
        float sway = Math.abs(MathHelper.sin(currentTick * 0.25F)) * 0.02F;
        drawActorPrism(red, green, blue, alpha, 0.12F + sway, 0.06F, 0.12F + sway);
    }

    private void drawCartWheels(float alpha) {
        int wheelColor = withAlpha(0x2B2B2B, alpha * 255.0F);
        float red = ((wheelColor >> 16) & 0xFF) / 255.0F;
        float green = ((wheelColor >> 8) & 0xFF) / 255.0F;
        float blue = (wheelColor & 0xFF) / 255.0F;
        float wheelAlpha = ((wheelColor >>> 24) & 0xFF) / 255.0F;
        float[][] wheels = new float[][] {
            {-0.22F, -0.12F},
            {0.22F, -0.12F},
            {-0.22F, 0.12F},
            {0.22F, 0.12F}
        };
        for (float[] wheel : wheels) {
            drawActorPrism(red, green, blue, wheelAlpha, 0.05F, 0.05F, 0.05F, wheel[0], -0.12F, wheel[1]);
        }
    }

    private void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
        float halfZ) {
        drawActorPrism(red, green, blue, alpha, halfX, height, halfZ, 0.0F, 0.0F, 0.0F);
    }

    private void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
        float halfZ, float offsetX, float offsetY, float offsetZ) {
        float x1 = offsetX - halfX;
        float x2 = offsetX + halfX;
        float y1 = offsetY;
        float y2 = offsetY + height;
        float z1 = offsetZ - halfZ;
        float z2 = offsetZ + halfZ;

        try (GLStateGuard colorGuard = GLStateGuard.color(red, green, blue, alpha)) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y1, z1);
            GL11.glVertex3f(x2, y2, z1);
            GL11.glVertex3f(x1, y2, z1);

            GL11.glVertex3f(x1, y1, z2);
            GL11.glVertex3f(x2, y1, z2);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x1, y2, z2);

            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x1, y1, z2);
            GL11.glVertex3f(x1, y2, z2);
            GL11.glVertex3f(x1, y2, z1);

            GL11.glVertex3f(x2, y1, z1);
            GL11.glVertex3f(x2, y1, z2);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x2, y2, z1);

            GL11.glVertex3f(x1, y2, z1);
            GL11.glVertex3f(x2, y2, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x1, y2, z2);

            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y1, z1);
            GL11.glVertex3f(x2, y1, z2);
            GL11.glVertex3f(x1, y1, z2);
            GL11.glEnd();
        }
    }

    private int getActorBaseColor(PonderSceneRuntime.ActorRuntimeState actor) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return 0xD9C27A;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return 0xE0D6A8;
        }
        if (isBirbDancePose(actor)) {
            return 0xE58ACF;
        }
        if (isBirbCursorPose(actor)) {
            return 0x7FCDE0;
        }
        if (isBirbPoiPose(actor)) {
            return 0x82D173;
        }
        return 0xA8D89A;
    }

    private float getActorBobOffset(PonderSceneRuntime.ActorRuntimeState actor, float currentTick) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return MathHelper.sin(currentTick * 0.15F + actor.actorId) * 0.01F;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return Math.abs(MathHelper.sin(currentTick * 0.22F + actor.actorId * 0.4F)) * 0.05F;
        }
        if (isBirbDancePose(actor)) {
            return Math.abs(MathHelper.sin(currentTick * 0.35F + actor.actorId * 0.5F)) * 0.10F;
        }
        return Math.abs(MathHelper.sin(currentTick * 0.18F + actor.actorId * 0.35F)) * 0.04F;
    }

    private float getActorYawOffset(PonderSceneRuntime.ActorRuntimeState actor, float currentTick) {
        if (actor.kind != PonderScene.ActorKind.BIRB) {
            return 0.0F;
        }
        if (isBirbCursorPose(actor)) {
            return MathHelper.sin(currentTick * 0.30F + actor.actorId) * 18.0F;
        }
        if (isBirbDancePose(actor)) {
            return MathHelper.sin(currentTick * 0.45F + actor.actorId) * 10.0F;
        }
        return 0.0F;
    }

    private boolean isBirbDancePose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("DancePose");
    }

    private boolean isBirbCursorPose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FaceCursorPose");
    }

    private boolean isBirbPoiPose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FacePointOfInterestPose");
    }

    private static int withAlpha(int color, float alpha) {
        int alphaChannel = MathHelper.clamp((int) alpha, 0, 255);
        return alphaChannel << 24 | (color & 0x00FFFFFF);
    }
}
