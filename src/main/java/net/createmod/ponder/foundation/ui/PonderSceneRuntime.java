package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderSchematic;
import net.createmod.ponder.foundation.Vec3iAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

final class PonderSceneRuntime {

    static final int SECTION_FADE_TICKS = 15;
    static final int CAMERA_ROTATE_TICKS = 18;
    private static final float SECTION_FADE_DISTANCE = 0.5F;

    private PonderSceneRuntime() {
    }

    static PonderScenePreview.PreviewBounds computeBounds(PonderScene scene) {
        int minX = Math.min(0, scene.getBasePlateOffsetX());
        int minY = 0;
        int minZ = Math.min(0, scene.getBasePlateOffsetZ());
        int maxX = Math.max(4, scene.getBasePlateOffsetX() + scene.getBasePlateSize() - 1);
        int maxY = 4;
        int maxZ = Math.max(4, scene.getBasePlateOffsetZ() + scene.getBasePlateSize() - 1);

        PonderSchematic schematic = scene.getSchematic();
        for (BlockPos pos : schematic.getBlocks().keySet()) {
            minX = Math.min(minX, Vec3iAccessor.x(pos));
            minY = Math.min(minY, Vec3iAccessor.y(pos));
            minZ = Math.min(minZ, Vec3iAccessor.z(pos));
            maxX = Math.max(maxX, Vec3iAccessor.x(pos));
            maxY = Math.max(maxY, Vec3iAccessor.y(pos));
            maxZ = Math.max(maxZ, Vec3iAccessor.z(pos));
        }

        for (PonderScene.WorldEvent event : scene.getWorldEvents()) {
            for (BlockPos pos : event.getPositions()) {
                minX = Math.min(minX, Vec3iAccessor.x(pos));
                minY = Math.min(minY, Vec3iAccessor.y(pos));
                minZ = Math.min(minZ, Vec3iAccessor.z(pos));
                maxX = Math.max(maxX, Vec3iAccessor.x(pos));
                maxY = Math.max(maxY, Vec3iAccessor.y(pos));
                maxZ = Math.max(maxZ, Vec3iAccessor.z(pos));

                Vec3d transformedCenter = transformEventCenter(event, pos);
                if (transformedCenter != null) {
                    minX = Math.min(minX, MathHelper.floor(transformedCenter.x - 0.5D));
                    minY = Math.min(minY, MathHelper.floor(transformedCenter.y - 0.5D));
                    minZ = Math.min(minZ, MathHelper.floor(transformedCenter.z - 0.5D));
                    maxX = Math.max(maxX, MathHelper.floor(transformedCenter.x + 0.5D));
                    maxY = Math.max(maxY, MathHelper.floor(transformedCenter.y + 0.5D));
                    maxZ = Math.max(maxZ, MathHelper.floor(transformedCenter.z + 0.5D));
                }
            }
        }

        return new PonderScenePreview.PreviewBounds(minX, maxX, minZ, maxZ, maxY, minY);
    }

    static RuntimeState buildState(PonderScene scene, int tick) {
        return buildState(scene, (float) tick);
    }

    static RuntimeState buildState(PonderScene scene, float tick) {
        Map<BlockPos, RuntimeBlockState> blocksByPosition = new LinkedHashMap<BlockPos, RuntimeBlockState>();
        PonderSchematic schematic = scene.getSchematic();

        for (Map.Entry<BlockPos, IBlockState> entry : schematic.getBlocks().entrySet()) {
            RuntimeBlockState state = new RuntimeBlockState(entry.getKey());
            state.originalState = entry.getValue();
            state.currentState = entry.getValue();
            state.stateDescription = describeState(entry.getValue());
            blocksByPosition.put(state.pos, state);
        }

        for (PonderScene.WorldEvent event : scene.getWorldEvents()) {
            if (event.getTick() > tick) {
                break;
            }

            float elapsed = tick - event.getTick();

            for (BlockPos pos : event.getPositions()) {
                BlockPos key = new BlockPos(pos);
                RuntimeBlockState state = blocksByPosition.get(key);
                if (state == null) {
                    state = new RuntimeBlockState(key);
                    blocksByPosition.put(key, state);
                }

                switch (event.getType()) {
                    case SHOW_SECTION:
                        animateShowSection(state, event.getDirection(), elapsed);
                        state.breakingProgress = 0;
                        break;
                    case RESTORE_BLOCKS:
                        restoreOriginal(state);
                        state.breakingProgress = 0;
                        resetFadeAnimation(state);
                        break;
                    case HIDE_SECTION:
                        animateHideSection(state, event.getDirection(), elapsed);
                        state.breakingProgress = 0;
                        break;
                    case DESTROY_BLOCK:
                        state.visible = false;
                        state.fade = 0.0F;
                        resetFadeOffset(state);
                        state.breakingProgress = 0;
                        state.stateDescription = "destroyed";
                        break;
                    case SET_BLOCKS:
                    case SET_BLOCK:
                    case REPLACE_BLOCKS:
                        IBlockState parsedState = parseState(event.getStateDescription());
                        state.currentState = parsedState;
                        if (state.originalState == null) {
                            state.originalState = parsedState;
                        }
                        state.visible = isRenderable(parsedState);
                        state.fade = state.visible ? 1.0F : 0.0F;
                        resetFadeOffset(state);
                        state.breakingProgress = 0;
                        state.stateDescription = describeState(parsedState);
                        break;
                    case APPLY_NBT:
                        state.tileNbt = event.getStateDescription();
                        break;
                    case BREAK_PROGRESS:
                        if (state.currentState == null) {
                            state.currentState = state.originalState;
                        }
                        state.visible = state.visible || isRenderable(state.currentState);
                        state.breakingProgress = Math.min(10, state.breakingProgress + 1);
                        if (state.stateDescription == null) {
                            state.stateDescription = describeState(state.currentState);
                        }
                        if (state.visible && state.fade == 0.0F) {
                            state.fade = 1.0F;
                        }
                        break;
                    case MOVE_SECTION:
                        applySectionMove(state, event, elapsed);
                        break;
                    case ROTATE_SECTION:
                        applySectionRotation(state, event, elapsed);
                        break;
                    default:
                        break;
                }
            }
        }

        Map<Long, PonderScenePreview.PreviewCellState> cellsByColumn =
            new LinkedHashMap<Long, PonderScenePreview.PreviewCellState>();
        int visibleBlocks = 0;
        for (RuntimeBlockState state : blocksByPosition.values()) {
            updateDisplayMetrics(state);
            if (!state.visible || !isRenderable(state.currentState)) {
                continue;
            }

            visibleBlocks++;
            int renderX = MathHelper.floor(state.renderCenterX);
            int renderY = MathHelper.floor(state.renderCenterY);
            int renderZ = MathHelper.floor(state.renderCenterZ);
            long key = PonderScenePreview.columnKey(renderX, renderZ);
            PonderScenePreview.PreviewCellState cell = cellsByColumn.get(key);
            if (cell == null) {
                cell = new PonderScenePreview.PreviewCellState();
                cell.x = renderX;
                cell.z = renderZ;
                cellsByColumn.put(key, cell);
            }

            cell.visibleCount++;
            cell.breakingProgress = Math.max(cell.breakingProgress, state.breakingProgress);
            if (renderY >= cell.topY) {
                cell.topY = renderY;
                cell.stateDescription = state.stateDescription;
            }
        }

        return new RuntimeState(blocksByPosition, cellsByColumn, visibleBlocks, cellsByColumn.size());
    }

    static float getCameraYaw(PonderScene scene, float tick) {
        float yaw = 0.0F;
        for (PonderScene.CameraEvent event : scene.getCameraEvents()) {
            if (event.getTick() > tick) {
                break;
            }

            float progress = animationProgress(tick - event.getTick(), CAMERA_ROTATE_TICKS);
            yaw += event.getYawDegrees() * easeOutQuad(progress);
        }
        return yaw;
    }

    private static void revealOriginal(RuntimeBlockState state) {
        if (!isRenderable(state.originalState)) {
            return;
        }
        state.currentState = state.originalState;
        state.visible = true;
        state.stateDescription = describeState(state.currentState);
    }

    private static void restoreOriginal(RuntimeBlockState state) {
        if (!isRenderable(state.originalState)) {
            state.currentState = null;
            state.visible = false;
            state.fade = 0.0F;
            resetFadeOffset(state);
            state.stateDescription = "restored";
            return;
        }
        state.currentState = state.originalState;
        state.visible = true;
        state.stateDescription = describeState(state.currentState);
    }

    private static void animateShowSection(RuntimeBlockState state, EnumFacing direction, float elapsed) {
        if (!isRenderable(state.originalState)) {
            state.visible = false;
            state.fade = 0.0F;
            resetFadeOffset(state);
            return;
        }

        state.currentState = state.originalState;
        state.stateDescription = describeState(state.currentState);

        float progress = animationProgress(elapsed, SECTION_FADE_TICKS);
        float fade = easeOutQuad(progress);
        state.visible = fade > 0.0F;
        state.fade = fade;
        applyDirectionalFadeOffset(state, direction, -1.0F, 1.0F - fade);
    }

    private static void animateHideSection(RuntimeBlockState state, EnumFacing direction, float elapsed) {
        if (!isRenderable(state.currentState)) {
            state.visible = false;
            state.fade = 0.0F;
            resetFadeOffset(state);
            return;
        }

        float progress = animationProgress(elapsed, SECTION_FADE_TICKS);
        float fade = 1.0F - progress * progress;
        state.visible = fade > 0.0F;
        state.fade = fade;
        applyDirectionalFadeOffset(state, direction, 1.0F, 1.0F - fade);

        if (!state.visible) {
            state.breakingProgress = 0;
        }
    }

    private static void applySectionMove(RuntimeBlockState state, PonderScene.WorldEvent event, float elapsed) {
        Vec3d offset = event.getOffset();
        if (offset == null) {
            return;
        }

        float progress = transformProgress(elapsed, event.getDuration());
        if (progress <= 0.0F) {
            return;
        }

        Vec3d applied = offset.scale(progress);
        if (isNearZero(applied)) {
            return;
        }
        state.transforms.add(TransformStep.move(applied));
    }

    private static void applySectionRotation(RuntimeBlockState state, PonderScene.WorldEvent event, float elapsed) {
        Vec3d rotation = event.getRotation();
        Vec3d pivot = event.getPivot();
        if (rotation == null || pivot == null) {
            return;
        }

        float progress = transformProgress(elapsed, event.getDuration());
        if (progress <= 0.0F) {
            return;
        }

        Vec3d applied = rotation.scale(progress);
        if (isNearZero(applied)) {
            return;
        }
        state.transforms.add(TransformStep.rotate(pivot, applied));
    }

    private static float animationProgress(float elapsed, int totalTicks) {
        if (totalTicks <= 0) {
            return 1.0F;
        }
        return MathHelper.clamp((elapsed + 1.0F) / totalTicks, 0.0F, 1.0F);
    }

    private static float easeOutQuad(float progress) {
        float inverse = 1.0F - progress;
        return 1.0F - inverse * inverse;
    }

    private static float transformProgress(float elapsed, int duration) {
        if (duration <= 0) {
            return 1.0F;
        }
        return easeOutQuad(animationProgress(elapsed, duration));
    }

    private static void applyDirectionalFadeOffset(RuntimeBlockState state, EnumFacing direction, float directionScale,
        float amount) {
        if (direction == null || amount <= 0.0F) {
            resetFadeOffset(state);
            return;
        }

        float offset = SECTION_FADE_DISTANCE * amount;
        state.fadeOffsetX = direction.getXOffset() * offset * directionScale;
        state.fadeOffsetY = direction.getYOffset() * offset * directionScale;
        state.fadeOffsetZ = direction.getZOffset() * offset * directionScale;
    }

    private static void resetFadeAnimation(RuntimeBlockState state) {
        state.fade = state.visible ? 1.0F : 0.0F;
        resetFadeOffset(state);
    }

    private static void resetFadeOffset(RuntimeBlockState state) {
        state.fadeOffsetX = 0.0F;
        state.fadeOffsetY = 0.0F;
        state.fadeOffsetZ = 0.0F;
    }

    private static boolean isRenderable(IBlockState state) {
        return state != null && state.getBlock() != Blocks.AIR;
    }

    private static IBlockState parseState(String stateDescription) {
        if (stateDescription == null || stateDescription.isEmpty() || "null".equals(stateDescription)) {
            return null;
        }

        try {
            String raw = stateDescription.trim();
            int meta = -1;
            int metaSeparator = raw.indexOf('#');
            if (metaSeparator >= 0) {
                String metaRaw = raw.substring(metaSeparator + 1);
                raw = raw.substring(0, metaSeparator);
                try {
                    meta = Integer.parseInt(metaRaw);
                } catch (NumberFormatException ignored) {
                    meta = -1;
                }
            }

            Block block = Block.REGISTRY.getObject(new ResourceLocation(raw));
            if (block == null) {
                return null;
            }

            if (meta < 0) {
                return block.getDefaultState();
            }

            try {
                return block.getStateFromMeta(meta);
            } catch (RuntimeException ignored) {
                return block.getDefaultState();
            }
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static String describeState(IBlockState state) {
        if (state == null) {
            return "visible";
        }
        ResourceLocation blockId = Block.REGISTRY.getNameForObject(state.getBlock());
        if (blockId == null) {
            return state.toString();
        }

        int meta;
        try {
            meta = state.getBlock().getMetaFromState(state);
        } catch (RuntimeException ignored) {
            meta = -1;
        }
        return meta >= 0 ? blockId + "#" + meta : blockId.toString();
    }

    private static Vec3d transformEventCenter(PonderScene.WorldEvent event, BlockPos pos) {
        Vec3d center = new Vec3d(pos).add(0.5D, 0.5D, 0.5D);
        if (event.getOffset() != null) {
            center = center.add(event.getOffset());
        }
        if (event.getRotation() != null && event.getPivot() != null) {
            center = rotateAround(center, event.getPivot(), event.getRotation());
        }
        return center;
    }

    private static void updateDisplayMetrics(RuntimeBlockState state) {
        Vec3d transformed = applyTransformsToPoint(state, new Vec3d(state.pos).add(0.5D, 0.5D, 0.5D));
        state.renderCenterX = transformed.x;
        state.renderCenterY = transformed.y;
        state.renderCenterZ = transformed.z;
    }

    private static Vec3d applyTransformsToPoint(RuntimeBlockState state, Vec3d point) {
        Vec3d transformed = point;
        for (TransformStep transform : state.transforms) {
            transformed = applyTransform(transformed, transform);
        }
        return transformed.add(state.fadeOffsetX, state.fadeOffsetY, state.fadeOffsetZ);
    }

    private static Vec3d applyTransform(Vec3d point, TransformStep transform) {
        Vec3d transformed = point;
        if (transform.offset != null) {
            transformed = transformed.add(transform.offset);
        }
        if (transform.rotation != null && transform.pivot != null) {
            transformed = rotateAround(transformed, transform.pivot, transform.rotation);
        }
        return transformed;
    }

    private static Vec3d rotateAround(Vec3d point, Vec3d pivot, Vec3d rotation) {
        Vec3d translated = point.subtract(pivot);
        translated = rotateX(translated, rotation.x);
        translated = rotateY(translated, rotation.y);
        translated = rotateZ(translated, rotation.z);
        return translated.add(pivot);
    }

    private static Vec3d rotateX(Vec3d vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double y = vec.y * cosine - vec.z * sine;
        double z = vec.y * sine + vec.z * cosine;
        return new Vec3d(vec.x, y, z);
    }

    private static Vec3d rotateY(Vec3d vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double x = vec.x * cosine + vec.z * sine;
        double z = vec.z * cosine - vec.x * sine;
        return new Vec3d(x, vec.y, z);
    }

    private static Vec3d rotateZ(Vec3d vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double x = vec.x * cosine - vec.y * sine;
        double y = vec.x * sine + vec.y * cosine;
        return new Vec3d(x, y, vec.z);
    }

    private static boolean isNearZero(Vec3d vec) {
        return vec == null
            || (Math.abs(vec.x) < 1.0E-4D && Math.abs(vec.y) < 1.0E-4D && Math.abs(vec.z) < 1.0E-4D);
    }

    static void applyRenderTransforms(RuntimeBlockState state) {
        if (state == null) {
            return;
        }

        if (state.fadeOffsetX != 0.0F || state.fadeOffsetY != 0.0F || state.fadeOffsetZ != 0.0F) {
            net.minecraft.client.renderer.GlStateManager.translate(state.fadeOffsetX, state.fadeOffsetY,
                state.fadeOffsetZ);
        }

        for (TransformStep transform : state.transforms) {
            if (transform.offset != null && !isNearZero(transform.offset)) {
                net.minecraft.client.renderer.GlStateManager.translate(transform.offset.x, transform.offset.y,
                    transform.offset.z);
            }
            if (transform.rotation != null && transform.pivot != null && !isNearZero(transform.rotation)) {
                net.minecraft.client.renderer.GlStateManager.translate(transform.pivot.x, transform.pivot.y,
                    transform.pivot.z);
                if (Math.abs(transform.rotation.x) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) transform.rotation.x, 1.0F, 0.0F, 0.0F);
                }
                if (Math.abs(transform.rotation.y) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) transform.rotation.y, 0.0F, 1.0F, 0.0F);
                }
                if (Math.abs(transform.rotation.z) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) transform.rotation.z, 0.0F, 0.0F, 1.0F);
                }
                net.minecraft.client.renderer.GlStateManager.translate(-transform.pivot.x, -transform.pivot.y,
                    -transform.pivot.z);
            }
        }
    }

    static final class TransformStep {
        final Vec3d offset;
        final Vec3d pivot;
        final Vec3d rotation;

        private TransformStep(Vec3d offset, Vec3d pivot, Vec3d rotation) {
            this.offset = offset;
            this.pivot = pivot;
            this.rotation = rotation;
        }

        static TransformStep move(Vec3d offset) {
            return new TransformStep(offset, null, null);
        }

        static TransformStep rotate(Vec3d pivot, Vec3d rotation) {
            return new TransformStep(null, pivot, rotation);
        }
    }

    static final class RuntimeBlockState {
        final BlockPos pos;
        IBlockState originalState;
        IBlockState currentState;
        boolean visible;
        float fade;
        float fadeOffsetX;
        float fadeOffsetY;
        float fadeOffsetZ;
        int breakingProgress;
        String stateDescription = "visible";
        String tileNbt;
        final List<TransformStep> transforms = new ArrayList<TransformStep>();
        double renderCenterX;
        double renderCenterY;
        double renderCenterZ;

        RuntimeBlockState(BlockPos pos) {
            this.pos = pos;
            this.renderCenterX = Vec3iAccessor.x(pos) + 0.5D;
            this.renderCenterY = Vec3iAccessor.y(pos) + 0.5D;
            this.renderCenterZ = Vec3iAccessor.z(pos) + 0.5D;
        }
    }

    static final class RuntimeState {
        final Map<BlockPos, RuntimeBlockState> blocksByPosition;
        final Map<Long, PonderScenePreview.PreviewCellState> cellsByColumn;
        final int visibleBlocks;
        final int columnsWithBlocks;

        RuntimeState(Map<BlockPos, RuntimeBlockState> blocksByPosition,
            Map<Long, PonderScenePreview.PreviewCellState> cellsByColumn, int visibleBlocks, int columnsWithBlocks) {
            this.blocksByPosition = Collections.unmodifiableMap(blocksByPosition);
            this.cellsByColumn = Collections.unmodifiableMap(cellsByColumn);
            this.visibleBlocks = visibleBlocks;
            this.columnsWithBlocks = columnsWithBlocks;
        }
    }
}
