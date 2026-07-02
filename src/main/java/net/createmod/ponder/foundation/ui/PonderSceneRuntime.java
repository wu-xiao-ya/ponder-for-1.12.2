package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderSchematic;
import net.createmod.ponder.foundation.Vec3iAccessor;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.MoveStep;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RotateStep;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeBlockState;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.RuntimeState;
import net.createmod.ponder.foundation.ui.PonderSceneRuntimeTypes.TransformStep;
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
    private static final AnimationSpec SECTION_SHOW_FADE = AnimationSpec.easeOutQuad(SECTION_FADE_TICKS);
    private static final AnimationSpec SECTION_HIDE_FADE = AnimationSpec.linear(SECTION_FADE_TICKS);
    private static final AnimationSpec CAMERA_ROTATE = AnimationSpec.easeOutQuad(CAMERA_ROTATE_TICKS);
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
            RuntimeBlockState state = new RuntimeBlockState(entry.getKey(), new ArrayList<TransformStep>());
            state.originalState = entry.getValue();
            state.currentState = entry.getValue();
            state.stateDescription = describeState(entry.getValue());
            state.renderCenterX = Vec3iAccessor.x(entry.getKey()) + 0.5D;
            state.renderCenterY = Vec3iAccessor.y(entry.getKey()) + 0.5D;
            state.renderCenterZ = Vec3iAccessor.z(entry.getKey()) + 0.5D;
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
                    state = new RuntimeBlockState(key, new ArrayList<TransformStep>());
                    state.renderCenterX = Vec3iAccessor.x(key) + 0.5D;
                    state.renderCenterY = Vec3iAccessor.y(key) + 0.5D;
                    state.renderCenterZ = Vec3iAccessor.z(key) + 0.5D;
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

            yaw += event.getYawDegrees() * CAMERA_ROTATE.progress(tick - event.getTick());
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

        float fade = SECTION_SHOW_FADE.progress(elapsed);
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

        float progress = SECTION_HIDE_FADE.progress(elapsed);
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

        float progress = AnimationSpec.easeOutQuad(event.getDuration()).progress(elapsed);
        if (progress <= 0.0F) {
            return;
        }

        Vec3d applied = offset.scale(progress);
        if (isNearZero(applied)) {
            return;
        }
        state.transforms.add(new MoveStep(applied));
    }

    private static void applySectionRotation(RuntimeBlockState state, PonderScene.WorldEvent event, float elapsed) {
        Vec3d rotation = event.getRotation();
        Vec3d pivot = event.getPivot();
        if (rotation == null || pivot == null) {
            return;
        }

        float progress = AnimationSpec.easeOutQuad(event.getDuration()).progress(elapsed);
        if (progress <= 0.0F) {
            return;
        }

        Vec3d applied = rotation.scale(progress);
        if (isNearZero(applied)) {
            return;
        }
        state.transforms.add(new RotateStep(pivot, applied));
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
        if (transform instanceof MoveStep moveStep) {
            transformed = transformed.add(moveStep.offset());
        }
        if (transform instanceof RotateStep rotateStep) {
            transformed = rotateAround(transformed, rotateStep.pivot(), rotateStep.rotation());
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
            if (transform instanceof MoveStep moveStep && !isNearZero(moveStep.offset())) {
                net.minecraft.client.renderer.GlStateManager.translate(moveStep.offset().x, moveStep.offset().y,
                    moveStep.offset().z);
            }
            if (transform instanceof RotateStep rotateStep && !isNearZero(rotateStep.rotation())) {
                net.minecraft.client.renderer.GlStateManager.translate(rotateStep.pivot().x, rotateStep.pivot().y,
                    rotateStep.pivot().z);
                if (Math.abs(rotateStep.rotation().x) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) rotateStep.rotation().x, 1.0F, 0.0F, 0.0F);
                }
                if (Math.abs(rotateStep.rotation().y) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) rotateStep.rotation().y, 0.0F, 1.0F, 0.0F);
                }
                if (Math.abs(rotateStep.rotation().z) >= 1.0E-4D) {
                    net.minecraft.client.renderer.GlStateManager.rotate((float) rotateStep.rotation().z, 0.0F, 0.0F, 1.0F);
                }
                net.minecraft.client.renderer.GlStateManager.translate(-rotateStep.pivot().x, -rotateStep.pivot().y,
                    -rotateStep.pivot().z);
            }
        }
    }

    static final class ActorRuntimeState {
        public final int actorId;
        public final PonderScene.ActorKind kind;
        public Vec3d position;
        public Vec3d rotation = Vec3d.ZERO;
        public float cartYaw;
        public float fade = 1.0F;
        public boolean visible = true;
        public String poseName;
        public String displayName;
        public ItemStack itemStack = ItemStack.EMPTY;

        ActorRuntimeState(int actorId, PonderScene.ActorKind kind, Vec3d position) {
            this.actorId = actorId;
            this.kind = kind;
            this.position = position;
        }
    }

    public static List<ActorRuntimeState> buildActorStates(PonderScene scene, float currentTick) {
        if (scene == null) {
            return Collections.emptyList();
        }

        Map<Integer, ActorRuntimeState> actors = new LinkedHashMap<Integer, ActorRuntimeState>();
        for (PonderScene.ActorEvent event : scene.getActorEvents()) {
            if (event.getTick() > currentTick) {
                break;
            }

            ActorRuntimeState actor = actors.get(Integer.valueOf(event.getActorId()));
            if (event.getType() == PonderScene.ActorEventType.SPAWN) {
                actor = new ActorRuntimeState(event.getActorId(), event.getActorKind(),
                    event.getLocation() == null ? Vec3d.ZERO : event.getLocation());
                actor.poseName = event.getPoseName();
                actor.cartYaw = event.getAngle();
                actor.displayName = event.getDisplayName();
                actor.itemStack = event.getItemStack().copy();
                float progress = actorProgress(event, currentTick);
                actor.fade = progress;
                actor.visible = progress > 0.0F;
                actors.put(Integer.valueOf(event.getActorId()), actor);
                continue;
            }

            if (actor == null) {
                continue;
            }

            if (event.getType() == PonderScene.ActorEventType.MOVE && event.getOffset() != null) {
                actor.position = actor.position.add(event.getOffset().scale(actorProgress(event, currentTick)));
            } else if (event.getType() == PonderScene.ActorEventType.ROTATE) {
                if (event.getRotation() != null) {
                    actor.rotation = actor.rotation.add(event.getRotation().scale(actorProgress(event, currentTick)));
                } else {
                    actor.cartYaw += event.getAngle() * actorProgress(event, currentTick);
                }
            } else if (event.getType() == PonderScene.ActorEventType.POSE) {
                actor.poseName = event.getPoseName();
            } else if (event.getType() == PonderScene.ActorEventType.HIDE) {
                float progress = actorProgress(event, currentTick);
                actor.fade = 1.0F - progress;
                actor.visible = actor.fade > 0.0F;
            }
        }

        return new ArrayList<ActorRuntimeState>(actors.values());
    }

    private static float actorProgress(PonderScene.ActorEvent event, float currentTick) {
        return AnimationSpec.linear(Math.max(1, event.getDuration())).progress(currentTick - event.getTick());
    }

}
