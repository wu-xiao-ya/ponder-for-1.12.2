package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.AnimatedSceneElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.InputElementBuilder;
import net.createmod.ponder.api.element.MinecartElement;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.PonderElement;
import net.createmod.ponder.api.element.TextElementBuilder;
import net.createmod.ponder.api.scene.GuiSnapshotBuilder;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.DebugInstructions;
import net.createmod.ponder.api.scene.EffectInstructions;
import net.createmod.ponder.api.scene.OverlayInstructions;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SpecialInstructions;
import net.createmod.ponder.api.scene.WorldInstructions;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.createmod.ponder.foundation.ui.PonderGuiSnapshotRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class PonderSceneBuilder implements SceneBuilder {

    private final OverlayInstructions overlay = new NoOpOverlayInstructions();
    private final WorldInstructions world = new NoOpWorldInstructions();
    private final DebugInstructions debug = new NoOpDebugInstructions();
    private final EffectInstructions effects = new NoOpEffectInstructions();
    private final SpecialInstructions special = new NoOpSpecialInstructions();
    private final PonderScene scene;
    private final Map<ElementLink<WorldSectionElement>, List<BlockPos>> independentSectionPositions =
        new LinkedHashMap<ElementLink<WorldSectionElement>, List<BlockPos>>();
    private final Map<ElementLink<WorldSectionElement>, Vec3d> independentSectionPivots =
        new LinkedHashMap<ElementLink<WorldSectionElement>, Vec3d>();
    private final Map<ElementLink<? extends PonderElement>, Integer> actorIds =
        new LinkedHashMap<ElementLink<? extends PonderElement>, Integer>();
    private final Map<ElementLink<? extends PonderElement>, PonderScene.ActorKind> actorKinds =
        new LinkedHashMap<ElementLink<? extends PonderElement>, PonderScene.ActorKind>();
    private final Map<BlockPos, IBlockState> trackedBlockStates = new LinkedHashMap<BlockPos, IBlockState>();
    private final Map<BlockPos, String> trackedTileNbt = new LinkedHashMap<BlockPos, String>();
    private int nextActorId = 1;
    private int overlayBuilderSequence;

    public PonderSceneBuilder(PonderScene scene) {
        this.scene = scene;
    }

    @Override
    public OverlayInstructions overlay() {
        return overlay;
    }

    @Override
    public WorldInstructions world() {
        return world;
    }

    @Override
    public DebugInstructions debug() {
        return debug;
    }

    @Override
    public EffectInstructions effects() {
        return effects;
    }

    @Override
    public SpecialInstructions special() {
        return special;
    }

    @Override
    public PonderScene getScene() {
        return scene;
    }

    @Override
    public void title(String sceneId, String title) {
        if (sceneId == null || sceneId.isEmpty()) {
            scene.setSceneId(scene.getSchematicLocation());
        } else {
            scene.setSceneId(new ResourceLocation(scene.getNamespace(), sceneId));
        }
        scene.setTitle(title);
        scene.getLocalization().registerSpecific(scene.getSceneId(), PonderScene.TITLE_KEY, title);
        scene.recordOperation("scene.title(" + scene.getSceneId() + ", " + title + ")");
    }

    @Override
    public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
        int safeBasePlateSize = Math.max(1, basePlateSize);
        scene.setBasePlateOffsetX(xOffset);
        scene.setBasePlateOffsetZ(zOffset);
        scene.setBasePlateSize(safeBasePlateSize);
        scene.recordOperation("scene.configureBasePlate(" + xOffset + ", " + zOffset + ", " + safeBasePlateSize
            + ")");
    }

    @Override
    public void scaleSceneView(float factor) {
        scene.setScaleFactor(factor);
        scene.recordOperation("scene.scaleSceneView(" + factor + ")");
    }

    @Override
    public void removeShadow() {
        scene.setHidePlatformShadow(true);
        scene.recordOperation("scene.removeShadow()");
    }

    @Override
    public void setSceneOffsetY(float yOffset) {
        scene.setSceneOffsetY(yOffset);
        scene.recordOperation("scene.setSceneOffsetY(" + yOffset + ")");
    }

    @Override
    public void showBasePlate() {
        int extent = Math.max(0, scene.getBasePlateSize() - 1);
        world.showSection(scene.getSceneBuildingUtil()
            .select()
            .cuboid(new BlockPos(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ()),
                new Vec3i(extent, 0, extent)), EnumFacing.UP);
    }

    public void addInstruction(Consumer<PonderScene> callback) {
        if (callback != null) {
            callback.accept(scene);
        }
    }

    @Override
    public void idle(int ticks) {
        int clampedTicks = Math.max(0, ticks);
        scene.addIdleTicks(clampedTicks);
        scene.recordOperation("scene.idle(" + clampedTicks + ")");
    }

    @Override
    public void idleSeconds(int seconds) {
        idle(seconds * 20);
    }

    @Override
    public void markAsFinished() {
        scene.setFinished(true);
        scene.recordOperation("scene.markAsFinished()");
    }

    @Override
    public void setNextUpEnabled(boolean isEnabled) {
        scene.setNextUpEnabled(isEnabled);
        scene.recordOperation("scene.setNextUpEnabled(" + isEnabled + ")");
    }

    @Override
    public void rotateCameraY(float degrees) {
        scene.addCameraYaw(degrees);
        scene.recordCameraYaw(degrees);
        scene.recordOperation("scene.rotateCameraY(" + degrees + ")");
    }

    @Override
    public void addKeyframe() {
        scene.incrementKeyframeCount();
        scene.recordOperation("scene.addKeyframe()");
    }

    @Override
    public void addLazyKeyframe() {
        scene.incrementLazyKeyframeCount();
        scene.recordOperation("scene.addLazyKeyframe()");
    }

    private String describeSelection(Selection selection) {
        return String.valueOf(selection);
    }

    private String describeSlot(Object slot) {
        if (slot == null) {
            return "null";
        }
        return slot.getClass().getSimpleName() + "@"
            + Integer.toHexString(System.identityHashCode(slot));
    }

    private String describeArgs(Object... args) {
        return Arrays.toString(args);
    }

    private String describeLink(Object link) {
        if (link == null) {
            return "null";
        }
        return link.getClass().getSimpleName() + "@"
            + Integer.toHexString(System.identityHashCode(link));
    }

    private String describeState(IBlockState state) {
        if (state == null) {
            return "null";
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

    private AxisAlignedBB selectionBounds(Selection selection) {
        BlockPos min = null;
        BlockPos max = null;
        for (BlockPos pos : selection) {
            if (min == null) {
                min = new BlockPos(pos);
                max = new BlockPos(pos);
                continue;
            }
            min = new BlockPos(Math.min(Vec3iAccessor.x(min), Vec3iAccessor.x(pos)),
                Math.min(Vec3iAccessor.y(min), Vec3iAccessor.y(pos)),
                Math.min(Vec3iAccessor.z(min), Vec3iAccessor.z(pos)));
            max = new BlockPos(Math.max(Vec3iAccessor.x(max), Vec3iAccessor.x(pos)),
                Math.max(Vec3iAccessor.y(max), Vec3iAccessor.y(pos)),
                Math.max(Vec3iAccessor.z(max), Vec3iAccessor.z(pos)));
        }
        if (min == null || max == null) {
            Vec3d center = selection.getCenter();
            return new AxisAlignedBB(center.x - 0.25D, center.y - 0.25D, center.z - 0.25D, center.x + 0.25D,
                center.y + 0.25D, center.z + 0.25D);
        }
        return new AxisAlignedBB(Vec3iAccessor.x(min), Vec3iAccessor.y(min), Vec3iAccessor.z(min),
            Vec3iAccessor.x(max) + 1.0D, Vec3iAccessor.y(max) + 1.0D, Vec3iAccessor.z(max) + 1.0D);
    }

    private List<BlockPos> copySelectionPositions(Selection selection) {
        List<BlockPos> positions = new ArrayList<BlockPos>();
        for (BlockPos pos : selection) {
            positions.add(new BlockPos(pos));
        }
        return positions;
    }

    private IBlockState getTrackedState(BlockPos pos) {
        BlockPos key = new BlockPos(pos);
        if (trackedBlockStates.containsKey(key)) {
            return trackedBlockStates.get(key);
        }
        IBlockState schematicState = scene.getSchematic().getBlocks().get(key);
        trackedBlockStates.put(key, schematicState);
        return schematicState;
    }

    private void setTrackedState(BlockPos pos, IBlockState state) {
        trackedBlockStates.put(new BlockPos(pos), state);
    }

    private String getTrackedTileNbt(BlockPos pos) {
        return trackedTileNbt.get(new BlockPos(pos));
    }

    private void setTrackedTileNbt(BlockPos pos, String nbt) {
        BlockPos key = new BlockPos(pos);
        if (nbt == null || nbt.trim().isEmpty()) {
            trackedTileNbt.remove(key);
            return;
        }
        trackedTileNbt.put(key, nbt);
    }

    private NBTTagCompound parseTrackedTileNbt(String rawNbt) {
        if (rawNbt == null || rawNbt.trim().isEmpty()) {
            return new NBTTagCompound();
        }
        try {
            return JsonToNBT.getTagFromJson(rawNbt);
        } catch (NBTException ignored) {
            return new NBTTagCompound();
        }
    }

    private String describeNbt(NBTTagCompound tag) {
        return tag == null ? "{}" : tag.toString();
    }

    @Nullable
    private TileEntity createTrackedTileEntity(BlockPos pos, @Nullable Class<? extends TileEntity> teType) {
        TileEntity tileEntity = null;
        IBlockState state = getTrackedState(pos);

        if (state != null && state.getBlock() != null && state.getBlock().hasTileEntity(state)) {
            try {
                tileEntity = state.getBlock().createTileEntity((World) null, state);
            } catch (Throwable ignored) {
                tileEntity = null;
            }
        }

        if (tileEntity == null && teType != null) {
            try {
                tileEntity = teType.newInstance();
            } catch (Throwable ignored) {
                tileEntity = null;
            }
        }

        if (tileEntity == null) {
            return null;
        }

        if (teType != null && !teType.isInstance(tileEntity)) {
            return null;
        }

        tileEntity.setPos(new BlockPos(pos));
        String trackedNbt = getTrackedTileNbt(pos);
        if (trackedNbt != null && !trackedNbt.trim().isEmpty()) {
            try {
                tileEntity.readFromNBT(parseTrackedTileNbt(trackedNbt));
            } catch (RuntimeException ignored) {
            }
        }

        return tileEntity;
    }

    private String writeTrackedTileNbt(BlockPos pos, TileEntity tileEntity) {
        NBTTagCompound tag = new NBTTagCompound();
        try {
            tileEntity.writeToNBT(tag);
        } catch (RuntimeException ignored) {
        }
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        return describeNbt(tag);
    }

    private List<BlockPos> asSinglePosition(BlockPos pos) {
        return Collections.singletonList(new BlockPos(pos));
    }

    private void recordGroupedStateEvent(PonderScene.WorldEventType eventType, Selection selection,
        UnaryOperator<IBlockState> stateFunc) {
        Map<String, List<BlockPos>> positionsByState = new LinkedHashMap<String, List<BlockPos>>();
        for (BlockPos pos : selection) {
            IBlockState previousState = getTrackedState(pos);
            IBlockState nextState = stateFunc.apply(previousState);
            setTrackedState(pos, nextState);
            if (previousState == null || nextState == null || previousState.getBlock() != nextState.getBlock()) {
                setTrackedTileNbt(pos, null);
            }
            String description = describeState(nextState);
            List<BlockPos> positions = positionsByState.get(description);
            if (positions == null) {
                positions = new ArrayList<BlockPos>();
                positionsByState.put(description, positions);
            }
            positions.add(new BlockPos(pos));
        }

        for (Map.Entry<String, List<BlockPos>> entry : positionsByState.entrySet()) {
            scene.recordWorldEvent(eventType, entry.getValue(), entry.getKey());
        }
    }

    private void recordGroupedStateEvent(PonderScene.WorldEventType eventType, BlockPos pos,
        UnaryOperator<IBlockState> stateFunc) {
        IBlockState previousState = getTrackedState(pos);
        IBlockState nextState = stateFunc.apply(previousState);
        setTrackedState(pos, nextState);
        if (previousState == null || nextState == null || previousState.getBlock() != nextState.getBlock()) {
            setTrackedTileNbt(pos, null);
        }
        scene.recordWorldEvent(eventType, asSinglePosition(pos), describeState(nextState));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private IBlockState cyclePropertyValue(IBlockState state, IProperty property) {
        if (state == null || property == null || !state.getPropertyKeys().contains(property)) {
            return state;
        }

        Collection allowedValues = property.getAllowedValues();
        if (allowedValues == null || allowedValues.isEmpty()) {
            return state;
        }

        Object currentValue = state.getValue(property);
        Iterator iterator = allowedValues.iterator();
        Object first = iterator.next();
        Object next = first;
        while (iterator.hasNext()) {
            Object candidate = iterator.next();
            if (currentValue == null ? candidate == null : currentValue.equals(candidate)) {
                next = iterator.hasNext() ? iterator.next() : first;
                return cyclePropertyValueTyped(state, property, next);
            }
        }

        return cyclePropertyValueTyped(state, property, first);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private IBlockState cyclePropertyValueTyped(IBlockState state, IProperty property, Object value) {
        return state.withProperty(property, (Comparable) value);
    }

    private IBlockState toggleRedstoneState(IBlockState state) {
        if (state == null) {
            return Blocks.AIR.getDefaultState();
        }

        for (IProperty<?> property : state.getPropertyKeys()) {
            String name = property.getName();
            if ("powered".equals(name) || "lit".equals(name)) {
                return cyclePropertyValue(state, property);
            }
            if ("power".equals(name) && Integer.class.isAssignableFrom(property.getValueClass())) {
                @SuppressWarnings("unchecked")
                IProperty<Integer> intProperty = (IProperty<Integer>) property;
                Integer current = state.getValue(intProperty);
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (Integer value : intProperty.getAllowedValues()) {
                    if (value.intValue() < min) {
                        min = value.intValue();
                    }
                    if (value.intValue() > max) {
                        max = value.intValue();
                    }
                }
                int target = current != null && current.intValue() > min ? min : max;
                return state.withProperty(intProperty, Integer.valueOf(target));
            }
        }

        return state;
    }

    private void rememberIndependentSection(ElementLink<WorldSectionElement> link, Selection selection) {
        independentSectionPositions.put(link, copySelectionPositions(selection));
        independentSectionPivots.put(link, selection.getCenter());
    }

    private <T extends PonderElement> int rememberActor(ElementLink<T> link, PonderScene.ActorKind kind) {
        int actorId = nextActorId++;
        actorIds.put(link, Integer.valueOf(actorId));
        actorKinds.put(link, kind);
        return actorId;
    }

    private Integer getActorId(ElementLink<? extends PonderElement> link) {
        return actorIds.get(link);
    }

    private PonderScene.ActorKind getActorKind(ElementLink<? extends PonderElement> link) {
        return actorKinds.get(link);
    }

    private LoggedTextElementBuilder createTextBuilder(String call, int duration, Vec3d defaultPointAt) {
        String builderId = "overlay.text#" + (++overlayBuilderSequence);
        scene.recordOperation(call + " -> " + builderId);
        PonderScene.OverlayEvent overlayEvent = scene.createOverlayEvent(duration);
        overlayEvent.pointAt = defaultPointAt;
        return new LoggedTextElementBuilder(builderId, overlayEvent);
    }

    private final class NoOpOverlayInstructions implements OverlayInstructions {

        @Override
        public TextElementBuilder showText(int duration) {
            return createTextBuilder("overlay.showText(" + duration + ")", duration, null);
        }

        @Override
        public TextElementBuilder showOutlineWithText(Selection selection, int duration) {
            return createTextBuilder(
                "overlay.showOutlineWithText(" + describeSelection(selection) + ", " + duration + ")", duration,
                selection.getCenter());
        }

        @Override
        public GuiSnapshotBuilder showGuiSnapshot(String snapshotId, int duration) {
            return showGuiSnapshot(snapshotId == null ? null : new ResourceLocation(snapshotId), duration);
        }

        @Override
        public GuiSnapshotBuilder showGuiSnapshot(ResourceLocation snapshotId, int duration) {
            String builderId = "overlay.guiSnapshot#" + (++overlayBuilderSequence);
            scene.recordOperation("overlay.showGuiSnapshot(" + snapshotId + ", " + duration + ") -> " + builderId);
            PonderScene.OverlayEvent overlayEvent = scene.createOverlayEvent(duration).guiSnapshot(snapshotId);
            return new LoggedGuiSnapshotBuilder(builderId, overlayEvent);
        }

        @Override
        public GuiSnapshotBuilder showGuiSnapshot(ResourceLocation texture, int width, int height, int duration) {
            String builderId = "overlay.guiSnapshotExternal#" + (++overlayBuilderSequence);
            scene.recordOperation("overlay.showGuiSnapshot(" + texture + ", " + width + "x" + height + ", "
                + duration + ") -> " + builderId);
            PonderScene.OverlayEvent overlayEvent = scene.createOverlayEvent(duration)
                .guiTexture(texture, 0, 0, width, height, width, height, width, height, 0, 0, true);
            return new LoggedGuiSnapshotBuilder(builderId, overlayEvent);
        }

        @Override
        public GuiSnapshotBuilder showBlockGui(ResourceLocation blockId, int meta, int width, int height, int duration) {
            String builderId = "overlay.blockGui#" + (++overlayBuilderSequence);
            ResourceLocation snapshotId = PonderGuiSnapshotRegistry.registerBlockGuiSnapshot(blockId, meta, width, height);
            scene.recordOperation("overlay.showBlockGui(" + blockId + ", meta=" + meta + ", " + width + "x"
                + height + ", " + duration + ") -> " + builderId);
            PonderScene.OverlayEvent overlayEvent = scene.createOverlayEvent(duration).guiSnapshot(snapshotId);
            return new LoggedGuiSnapshotBuilder(builderId, overlayEvent);
        }

        @Override
        public InputElementBuilder showControls(Vec3d sceneSpace, Pointing direction, int duration) {
            String builderId = "overlay.controls#" + (++overlayBuilderSequence);
            scene.recordOperation("overlay.showControls(" + sceneSpace + ", " + direction + ", " + duration + ") -> "
                + builderId);
            PonderScene.OverlayEvent overlayEvent = scene.createOverlayEvent(duration)
                .controls(sceneSpace, direction)
                .palette(PonderPalette.INPUT)
                .placeNearTarget(true);
            return new LoggedInputElementBuilder(builderId, overlayEvent);
        }

        @Override
        public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AxisAlignedBB boundingBox, int duration) {
            scene.recordOperation("overlay.chaseBoundingBoxOutline(" + color + ", slot=" + describeSlot(slot) + ", "
                + boundingBox + ", " + duration + ")");
            scene.createOverlayEvent(duration)
                .sceneOutline(boundingBox)
                .palette(color)
                .overlayId(slot == null ? null : describeSlot(slot));
        }

        @Override
        public void showCenteredScrollInput(BlockPos pos, EnumFacing side, int duration) {
            scene.recordOperation("overlay.showCenteredScrollInput(" + pos + ", " + side + ", " + duration + ")");
            showScrollInput(scene.getSceneBuildingUtil().vector().blockSurface(pos, side), side, duration);
        }

        @Override
        public void showScrollInput(Vec3d location, EnumFacing side, int duration) {
            scene.recordOperation("overlay.showScrollInput(" + location + ", " + side + ", " + duration + ")");
            Vec3d expands = axisPlane(side, 0.22D, 0.03D);
            scene.createOverlayEvent(duration)
                .valueBox(location, expands)
                .palette(PonderPalette.INPUT)
                .placeNearTarget(true);
        }

        @Override
        public void showRepeaterScrollInput(BlockPos pos, int duration) {
            scene.recordOperation("overlay.showRepeaterScrollInput(" + pos + ", " + duration + ")");
            Vec3d location = scene.getSceneBuildingUtil().vector().blockSurface(pos, EnumFacing.DOWN)
                .add(0.0D, 3 / 16.0D, 0.0D);
            scene.createOverlayEvent(duration)
                .valueBox(location, new Vec3d(1 / 6.0D, 1 / 16.0D, 1 / 6.0D))
                .palette(PonderPalette.INPUT)
                .placeNearTarget(true);
        }

        @Override
        public void showFilterSlotInput(Vec3d location, int duration) {
            scene.recordOperation("overlay.showFilterSlotInput(" + location + ", " + duration + ")");
            scene.createOverlayEvent(duration)
                .valueBox(location, new Vec3d(0.10D, 0.10D, 0.10D))
                .palette(PonderPalette.INPUT)
                .placeNearTarget(true);
        }

        @Override
        public void showFilterSlotInput(Vec3d location, EnumFacing side, int duration) {
            scene.recordOperation(
                "overlay.showFilterSlotInput(" + location + ", " + side + ", " + duration + ")");
            Vec3d adjusted = location.add(new Vec3d(side.getDirectionVec()).scale(-3 / 128.0D));
            scene.createOverlayEvent(duration)
                .valueBox(adjusted, axisPlane(side, 11 / 128.0D, 0.02D))
                .palette(PonderPalette.INPUT)
                .placeNearTarget(true);
        }

        @Override
        public void showLine(PonderPalette color, Vec3d start, Vec3d end, int duration) {
            scene.recordOperation("overlay.showLine(" + color + ", " + start + ", " + end + ", " + duration + ")");
            scene.createOverlayEvent(duration)
                .sceneLine(start, end, false)
                .palette(color == null ? PonderPalette.WHITE : color);
        }

        @Override
        public void showBigLine(PonderPalette color, Vec3d start, Vec3d end, int duration) {
            scene.recordOperation(
                "overlay.showBigLine(" + color + ", " + start + ", " + end + ", " + duration + ")");
            scene.createOverlayEvent(duration)
                .sceneLine(start, end, true)
                .palette(color == null ? PonderPalette.WHITE : color);
        }

        @Override
        public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
            scene.recordOperation("overlay.showOutline(" + color + ", slot=" + describeSlot(slot) + ", "
                + describeSelection(selection) + ", " + duration + ")");
            scene.createOverlayEvent(duration)
                .sceneOutline(selectionBounds(selection))
                .palette(color)
                .overlayId(slot == null ? null : describeSlot(slot));
        }
    }

    private final class LoggedInputElementBuilder implements InputElementBuilder {

        private final String builderId;
        private final PonderScene.OverlayEvent overlayEvent;

        private LoggedInputElementBuilder(String builderId, PonderScene.OverlayEvent overlayEvent) {
            this.builderId = builderId;
            this.overlayEvent = overlayEvent;
        }

        private InputElementBuilder record(String call) {
            scene.recordOperation(builderId + "." + call);
            return this;
        }

        @Override
        public InputElementBuilder withItem(net.minecraft.item.ItemStack stack) {
            overlayEvent.controlItem(stack);
            return record("withItem(" + stack + ")");
        }

        @Override
        public InputElementBuilder leftClick() {
            overlayEvent.controlLeftClick();
            return record("leftClick()");
        }

        @Override
        public InputElementBuilder rightClick() {
            overlayEvent.controlRightClick();
            return record("rightClick()");
        }

        @Override
        public InputElementBuilder scroll() {
            overlayEvent.controlScroll();
            return record("scroll()");
        }

        @Override
        public InputElementBuilder showing(Object icon) {
            return record("showing(" + icon + ")");
        }

        @Override
        public InputElementBuilder whileSneaking() {
            overlayEvent.controlSneaking();
            return record("whileSneaking()");
        }

        @Override
        public InputElementBuilder whileCTRL() {
            overlayEvent.controlCtrl();
            return record("whileCTRL()");
        }
    }

    private final class LoggedGuiSnapshotBuilder implements GuiSnapshotBuilder {
        private final String builderId;
        private final PonderScene.OverlayEvent overlayEvent;

        private LoggedGuiSnapshotBuilder(String builderId, PonderScene.OverlayEvent overlayEvent) {
            this.builderId = builderId;
            this.overlayEvent = overlayEvent;
        }

        private GuiSnapshotBuilder record(String call) {
            scene.recordOperation(builderId + "." + call);
            return this;
        }

        @Override
        public GuiSnapshotBuilder pointAt(Vec3d vec) {
            overlayEvent.pointAt(vec);
            return record("pointAt(" + vec + ")");
        }

        @Override
        public GuiSnapshotBuilder independent(int y) {
            overlayEvent.independent(y);
            return record("independent(" + y + ")");
        }

        @Override
        public GuiSnapshotBuilder placeNearTarget() {
            overlayEvent.placeNearTarget(true);
            return record("placeNearTarget()");
        }

        @Override
        public GuiSnapshotBuilder offset(int x, int y) {
            overlayEvent.offset(x, y);
            return record("offset(" + x + ", " + y + ")");
        }
    }

    private Vec3d axisPlane(EnumFacing side, double flat, double depth) {
        switch (side.getAxis()) {
            case X:
                return new Vec3d(depth, flat, flat);
            case Y:
                return new Vec3d(flat, depth, flat);
            case Z:
            default:
                return new Vec3d(flat, flat, depth);
        }
    }

    private final class LoggedTextElementBuilder implements TextElementBuilder {

        private final String builderId;
        private final PonderScene.OverlayEvent overlayEvent;

        private LoggedTextElementBuilder(String builderId, PonderScene.OverlayEvent overlayEvent) {
            this.builderId = builderId;
            this.overlayEvent = overlayEvent;
        }

        private TextElementBuilder record(String call) {
            scene.recordOperation(builderId + "." + call);
            return this;
        }

        @Override
        public TextElementBuilder colored(PonderPalette color) {
            overlayEvent.palette = color == null ? PonderPalette.WHITE : color;
            return record("colored(" + color + ")");
        }

        @Override
        public TextElementBuilder pointAt(Vec3d vec) {
            overlayEvent.pointAt = vec;
            return record("pointAt(" + vec + ")");
        }

        @Override
        public TextElementBuilder independent(int y) {
            overlayEvent.independentY = y;
            return record("independent(" + y + ")");
        }

        @Override
        public TextElementBuilder text(String defaultText) {
            overlayEvent.text = defaultText == null ? "" : defaultText;
            return record("text(\"" + defaultText + "\")");
        }

        @Override
        public TextElementBuilder text(String defaultText, Object... params) {
            overlayEvent.text = defaultText == null ? "" : defaultText;
            return record("text(\"" + defaultText + "\", " + describeArgs(params) + ")");
        }

        @Override
        public TextElementBuilder sharedText(ResourceLocation key) {
            overlayEvent.text = key == null ? "" : key.toString();
            return record("sharedText(" + key + ")");
        }

        @Override
        public TextElementBuilder sharedText(ResourceLocation key, Object... params) {
            overlayEvent.text = key == null ? "" : key.toString();
            return record("sharedText(" + key + ", " + describeArgs(params) + ")");
        }

        @Override
        public TextElementBuilder sharedText(String key) {
            overlayEvent.text = key == null ? "" : key;
            return record("sharedText(\"" + key + "\")");
        }

        @Override
        public TextElementBuilder sharedText(String key, Object... params) {
            overlayEvent.text = key == null ? "" : key;
            return record("sharedText(\"" + key + "\", " + describeArgs(params) + ")");
        }

        @Override
        public TextElementBuilder placeNearTarget() {
            overlayEvent.placeNearTarget = true;
            return record("placeNearTarget()");
        }

        @Override
        public TextElementBuilder attachKeyFrame() {
            return record("attachKeyFrame()");
        }
    }

    private final class NoOpWorldInstructions implements WorldInstructions {

        @Override
        public void incrementBlockBreakingProgress(BlockPos pos) {
            scene.recordOperation("world.incrementBlockBreakingProgress(" + pos + ")");
            scene.recordWorldEvent(PonderScene.WorldEventType.BREAK_PROGRESS, pos, null);
        }

        @Override
        public void showSection(Selection selection, EnumFacing fadeInDirection) {
            scene.recordOperation("world.showSection(" + describeSelection(selection) + ", " + fadeInDirection + ")");
            scene.recordWorldEvent(PonderScene.WorldEventType.SHOW_SECTION, selection, null, fadeInDirection);
        }

        @Override
        public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, EnumFacing fadeInDirection) {
            ElementLink<WorldSectionElement> link = new SimpleElementLink<WorldSectionElement>();
            scene.recordOperation("world.showIndependentSection(" + describeSelection(selection) + ", "
                + fadeInDirection + ") -> " + describeLink(link));
            scene.recordWorldEvent(PonderScene.WorldEventType.SHOW_SECTION, selection, null, fadeInDirection);
            rememberIndependentSection(link, selection);
            return link;
        }

        @Override
        public ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection) {
            ElementLink<WorldSectionElement> link = new SimpleElementLink<WorldSectionElement>();
            scene.recordOperation(
                "world.showIndependentSectionImmediately(" + describeSelection(selection) + ") -> " + describeLink(link));
            scene.recordWorldEvent(PonderScene.WorldEventType.SHOW_SECTION, selection, null, EnumFacing.DOWN);
            rememberIndependentSection(link, selection);
            return link;
        }

        @Override
        public void hideSection(Selection selection, EnumFacing fadeOutDirection) {
            scene.recordOperation("world.hideSection(" + describeSelection(selection) + ", " + fadeOutDirection + ")");
            scene.recordWorldEvent(PonderScene.WorldEventType.HIDE_SECTION, selection, null, fadeOutDirection);
        }

        @Override
        public void hideIndependentSection(ElementLink<WorldSectionElement> link, EnumFacing fadeOutDirection) {
            scene.recordOperation(
                "world.hideIndependentSection(" + describeLink(link) + ", " + fadeOutDirection + ")");
            List<BlockPos> positions = independentSectionPositions.get(link);
            if (positions != null && !positions.isEmpty()) {
                scene.recordWorldEvent(PonderScene.WorldEventType.HIDE_SECTION, positions, null, fadeOutDirection);
            }
        }

        @Override
        public void restoreBlocks(Selection selection) {
            scene.recordOperation("world.restoreBlocks(" + describeSelection(selection) + ")");
            for (BlockPos pos : selection) {
                setTrackedState(pos, scene.getSchematic().getBlocks().get(new BlockPos(pos)));
                setTrackedTileNbt(pos, null);
            }
            scene.recordWorldEvent(PonderScene.WorldEventType.RESTORE_BLOCKS, selection, null);
        }

        @Override
        public ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection) {
            ElementLink<WorldSectionElement> link = new SimpleElementLink<WorldSectionElement>();
            scene.recordOperation(
                "world.makeSectionIndependent(" + describeSelection(selection) + ") -> " + describeLink(link));
            rememberIndependentSection(link, selection);
            return link;
        }

        @Override
        public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
            double zRotation, int duration) {
            scene.recordOperation("world.rotateSection(" + describeLink(link) + ", [" + xRotation + ", " + yRotation
                + ", " + zRotation + "], " + duration + ")");
            List<BlockPos> positions = independentSectionPositions.get(link);
            if (positions != null && !positions.isEmpty()) {
                Vec3d pivot = independentSectionPivots.containsKey(link) ? independentSectionPivots.get(link)
                    : selectionBounds(SelectionImpl.fromTo(positions.get(0), positions.get(0))).getCenter();
                scene.recordSectionRotation(positions, pivot, new Vec3d(xRotation, yRotation, zRotation), duration);
            }
        }

        @Override
        public void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3d anchor) {
            scene.recordOperation("world.configureCenterOfRotation(" + describeLink(link) + ", " + anchor + ")");
            independentSectionPivots.put(link, anchor);
        }

        @Override
        public void configureStabilization(ElementLink<WorldSectionElement> link, Vec3d anchor) {
            scene.recordOperation("world.configureStabilization(" + describeLink(link) + ", " + anchor + ")");
            independentSectionPivots.put(link, anchor);
        }

        @Override
        public void moveSection(ElementLink<WorldSectionElement> link, Vec3d offset, int duration) {
            scene.recordOperation(
                "world.moveSection(" + describeLink(link) + ", " + offset + ", " + duration + ")");
            List<BlockPos> positions = independentSectionPositions.get(link);
            if (positions != null && !positions.isEmpty()) {
                scene.recordSectionMove(positions, offset, duration);
            }
        }

        @Override
        public void setBlocks(Selection selection, IBlockState state, boolean spawnParticles) {
            scene.recordOperation("world.setBlocks(" + describeSelection(selection) + ", " + state + ", "
                + spawnParticles + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.SET_BLOCKS, selection, new UnaryOperator<IBlockState>() {
                @Override
                public IBlockState apply(IBlockState input) {
                    return state;
                }
            });
        }

        @Override
        public void destroyBlock(BlockPos pos) {
            scene.recordOperation("world.destroyBlock(" + pos + ")");
            setTrackedState(pos, Blocks.AIR.getDefaultState());
            setTrackedTileNbt(pos, null);
            scene.recordWorldEvent(PonderScene.WorldEventType.DESTROY_BLOCK, pos, null);
        }

        @Override
        public void setBlock(BlockPos pos, IBlockState state, boolean spawnParticles) {
            scene.recordOperation("world.setBlock(" + pos + ", " + state + ", " + spawnParticles + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.SET_BLOCK, pos, new UnaryOperator<IBlockState>() {
                @Override
                public IBlockState apply(IBlockState input) {
                    return state;
                }
            });
        }

        @Override
        public void replaceBlocks(Selection selection, IBlockState state, boolean spawnParticles) {
            scene.recordOperation("world.replaceBlocks(" + describeSelection(selection) + ", " + state + ", "
                + spawnParticles + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.REPLACE_BLOCKS, selection,
                new UnaryOperator<IBlockState>() {
                    @Override
                    public IBlockState apply(IBlockState input) {
                        return state;
                    }
                });
        }

        @Override
        public void modifyBlock(BlockPos pos, UnaryOperator<IBlockState> stateFunc, boolean spawnParticles) {
            scene.recordOperation("world.modifyBlock(" + pos + ", " + spawnParticles + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.REPLACE_BLOCKS, pos, stateFunc);
        }

        @Override
        public void cycleBlockProperty(BlockPos pos, IProperty<?> property) {
            scene.recordOperation("world.cycleBlockProperty(" + pos + ", " + property + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.REPLACE_BLOCKS, pos, new UnaryOperator<IBlockState>() {
                @Override
                public IBlockState apply(IBlockState input) {
                    return cyclePropertyValue(input, property);
                }
            });
        }

        @Override
        public void modifyBlocks(Selection selection, UnaryOperator<IBlockState> stateFunc, boolean spawnParticles) {
            scene.recordOperation("world.modifyBlocks(" + describeSelection(selection) + ", " + spawnParticles + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.REPLACE_BLOCKS, selection, stateFunc);
        }

        @Override
        public void toggleRedstonePower(Selection selection) {
            scene.recordOperation("world.toggleRedstonePower(" + describeSelection(selection) + ")");
            recordGroupedStateEvent(PonderScene.WorldEventType.REPLACE_BLOCKS, selection,
                new UnaryOperator<IBlockState>() {
                    @Override
                    public IBlockState apply(IBlockState input) {
                        return toggleRedstoneState(input);
                    }
                });
        }

        @Override
        public <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack) {
            scene.recordOperation("world.modifyEntities(" + entityClass.getName() + ", " + entityCallBack + ")");
        }

        @Override
        public <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area,
            Consumer<T> entityCallBack) {
            scene.recordOperation("world.modifyEntitiesInside(" + entityClass.getName() + ", "
                + describeSelection(area) + ", " + entityCallBack + ")");
        }

        @Override
        public void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack) {
            scene.recordOperation("world.modifyEntity(" + describeLink(link) + ", " + entityCallBack + ")");
        }

        @Override
        public ElementLink<EntityElement> createEntity(Function<World, Entity> factory) {
            ElementLink<EntityElement> link = new SimpleElementLink<EntityElement>();
            scene.recordOperation("world.createEntity(" + factory + ") -> " + describeLink(link));
            return link;
        }

        @Override
        public ElementLink<EntityElement> createItemEntity(Vec3d location, Vec3d motion, ItemStack stack) {
            ElementLink<EntityElement> link = new SimpleElementLink<EntityElement>();
            scene.recordOperation("world.createItemEntity(" + location + ", " + motion + ", " + stack + ") -> "
                + describeLink(link));
            int actorId = rememberActor(link, PonderScene.ActorKind.ITEM);
            scene.recordItemActorSpawn(actorId, location, 10, stack == null ? null : stack.getDisplayName(),
                stack == null ? ItemStack.EMPTY : stack.copy());
            if (motion != null && (motion.x * motion.x + motion.y * motion.y + motion.z * motion.z) > 1.0E-8D) {
                scene.recordActorMove(actorId, PonderScene.ActorKind.ITEM, motion.scale(20.0D), 20);
            }
            return link;
        }

        @Override
        public void modifyBlockEntityNBT(Selection selection, Class<? extends TileEntity> teType,
            Consumer<NBTTagCompound> consumer) {
            modifyBlockEntityNBT(selection, teType, consumer, false);
        }

        @Override
        public <T extends TileEntity> void modifyBlockEntity(BlockPos position, Class<T> teType, Consumer<T> consumer) {
            scene.recordOperation("world.modifyBlockEntity(" + position + ", " + teType.getName() + ", " + consumer
                + ")");
            TileEntity tileEntity = createTrackedTileEntity(position, teType);
            if (tileEntity == null || !teType.isInstance(tileEntity)) {
                return;
            }
            T typedTile = teType.cast(tileEntity);
            if (consumer != null) {
                consumer.accept(typedTile);
            }
            String serialized = writeTrackedTileNbt(position, typedTile);
            setTrackedTileNbt(position, serialized);
            scene.recordWorldEvent(PonderScene.WorldEventType.APPLY_NBT, asSinglePosition(position), serialized);
        }

        @Override
        public void modifyBlockEntityNBT(Selection selection, Class<? extends TileEntity> teType,
            Consumer<NBTTagCompound> consumer, boolean reDrawBlocks) {
            scene.recordOperation("world.modifyBlockEntityNBT(" + describeSelection(selection) + ", "
                + teType.getName() + ", reDraw=" + reDrawBlocks + ")");
            for (BlockPos pos : selection) {
                NBTTagCompound tag = parseTrackedTileNbt(getTrackedTileNbt(pos));
                if (consumer != null) {
                    consumer.accept(tag);
                }
                String serialized = describeNbt(tag);
                setTrackedTileNbt(pos, serialized);
                scene.recordWorldEvent(PonderScene.WorldEventType.APPLY_NBT, asSinglePosition(pos), serialized);
            }
        }
    }

    private final class NoOpDebugInstructions implements DebugInstructions {

        @Override
        public void debugSchematic() {
            scene.recordOperation("debug.debugSchematic()");
        }

        @Override
        public void addInstructionInstance(PonderInstruction instruction) {
            scene.recordOperation("debug.addInstructionInstance(" + instruction + ")");
        }

        @Override
        public void enqueueCallback(Consumer<PonderScene> callback) {
            callback.accept(scene);
        }
    }

    private final class NoOpEffectInstructions implements EffectInstructions {

        @Override
        public void emitParticles(Vec3d location, ParticleEmitter emitter, float amountPerCycle, int cycles) {
            scene.recordOperation("effects.emitParticles(" + location + ", amountPerCycle=" + amountPerCycle
                + ", cycles=" + cycles + ")");
            if (emitter instanceof LoggedParticleEmitter) {
                LoggedParticleEmitter logged = (LoggedParticleEmitter) emitter;
                scene.recordParticleEffect(location, amountPerCycle, cycles, logged.particleName, logged.motion,
                    logged.withinBlockSpace);
            } else {
                scene.recordParticleEffect(location, amountPerCycle, cycles, "custom", Vec3d.ZERO, false);
            }
        }

        @Override
        public ParticleEmitter simpleParticleEmitter(EnumParticleTypes data, Vec3d motion) {
            scene.recordOperation("effects.simpleParticleEmitter(" + data + ", " + motion + ")");
            return new LoggedParticleEmitter(data, motion, false);
        }

        @Override
        public ParticleEmitter particleEmitterWithinBlockSpace(EnumParticleTypes data, Vec3d motion) {
            scene.recordOperation("effects.particleEmitterWithinBlockSpace(" + data + ", " + motion + ")");
            return new LoggedParticleEmitter(data, motion, true);
        }

        @Override
        public void indicateRedstone(BlockPos pos) {
            scene.recordOperation("effects.indicateRedstone(" + pos + ")");
            createRedstoneParticles(pos, 0xFF0000, 10);
        }

        @Override
        public void indicateSuccess(BlockPos pos) {
            scene.recordOperation("effects.indicateSuccess(" + pos + ")");
            createRedstoneParticles(pos, 0x80FFAA, 10);
        }

        @Override
        public void createRedstoneParticles(BlockPos pos, int color, int amount) {
            scene.recordOperation(
                "effects.createRedstoneParticles(" + pos + ", " + color + ", " + amount + ")");
            scene.recordParticleEffect(new Vec3d(pos).add(0.5D, 0.5D, 0.5D), amount, 2,
                "REDSTONE:" + Integer.toHexString(color & 0xFFFFFF), Vec3d.ZERO, true);
        }
    }

    private final class NoOpSpecialInstructions implements SpecialInstructions {

        @Override
        public ElementLink<ParrotElement> createBirb(Vec3d location, Supplier<? extends ParrotPose> pose) {
            ElementLink<ParrotElement> link = new SimpleElementLink<ParrotElement>();
            scene.recordOperation("special.createBirb(" + location + ", " + pose + ") -> " + describeLink(link));
            int actorId = rememberActor(link, PonderScene.ActorKind.BIRB);
            ParrotPose poseInstance = pose == null ? null : pose.get();
            scene.recordActorSpawn(actorId, PonderScene.ActorKind.BIRB, location, 10, 0.0F,
                poseInstance == null ? null : poseInstance.getClass().getSimpleName());
            return link;
        }

        @Override
        public void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose) {
            scene.recordOperation("special.changeBirbPose(" + describeLink(birb) + ", " + pose + ")");
            Integer actorId = getActorId(birb);
            if (actorId != null) {
                ParrotPose poseInstance = pose == null ? null : pose.get();
                scene.recordActorPose(actorId.intValue(),
                    poseInstance == null ? null : poseInstance.getClass().getSimpleName());
            }
        }

        @Override
        public void movePointOfInterest(Vec3d location) {
            scene.recordOperation("special.movePointOfInterest(" + location + ")");
            scene.recordPointOfInterest(location);
        }

        @Override
        public void movePointOfInterest(BlockPos location) {
            movePointOfInterest(new Vec3d(location).add(0.5D, 0.5D, 0.5D));
        }

        @Override
        public void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation,
            int duration) {
            scene.recordOperation("special.rotateParrot(" + describeLink(link) + ", [" + xRotation + ", " + yRotation
                + ", " + zRotation + "], " + duration + ")");
            Integer actorId = getActorId(link);
            if (actorId != null) {
                scene.recordActorRotation(actorId.intValue(), PonderScene.ActorKind.BIRB,
                    new Vec3d(xRotation, yRotation, zRotation), duration);
            }
        }

        @Override
        public void moveParrot(ElementLink<ParrotElement> link, Vec3d offset, int duration) {
            scene.recordOperation("special.moveParrot(" + describeLink(link) + ", " + offset + ", " + duration + ")");
            Integer actorId = getActorId(link);
            if (actorId != null) {
                scene.recordActorMove(actorId.intValue(), PonderScene.ActorKind.BIRB, offset, duration);
            }
        }

        @Override
        public ElementLink<MinecartElement> createCart(Vec3d location, float angle,
            MinecartElement.MinecartConstructor type) {
            ElementLink<MinecartElement> link = new SimpleElementLink<MinecartElement>();
            scene.recordOperation("special.createCart(" + location + ", " + angle + ", " + type + ") -> "
                + describeLink(link));
            int actorId = rememberActor(link, PonderScene.ActorKind.CART);
            scene.recordActorSpawn(actorId, PonderScene.ActorKind.CART, location, 10, angle, null);
            return link;
        }

        @Override
        public void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration) {
            scene.recordOperation(
                "special.rotateCart(" + describeLink(link) + ", " + yRotation + ", " + duration + ")");
            Integer actorId = getActorId(link);
            if (actorId != null) {
                scene.recordActorCartRotation(actorId.intValue(), yRotation, duration);
            }
        }

        @Override
        public void moveCart(ElementLink<MinecartElement> link, Vec3d offset, int duration) {
            scene.recordOperation("special.moveCart(" + describeLink(link) + ", " + offset + ", " + duration + ")");
            Integer actorId = getActorId(link);
            if (actorId != null) {
                scene.recordActorMove(actorId.intValue(), PonderScene.ActorKind.CART, offset, duration);
            }
        }

        @Override
        public <T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, EnumFacing direction) {
            scene.recordOperation("special.hideElement(" + describeLink(link) + ", " + direction + ")");
            Integer actorId = getActorId(link);
            PonderScene.ActorKind actorKind = getActorKind(link);
            if (actorId != null && actorKind != null) {
                scene.recordActorHide(actorId.intValue(), actorKind, 15);
            }
        }
    }

    private static final class SimpleElementLink<T extends net.createmod.ponder.api.element.PonderElement>
        implements ElementLink<T> {
    }

    private static final class LoggedParticleEmitter implements ParticleEmitter {
        private final String particleName;
        private final Vec3d motion;
        private final boolean withinBlockSpace;

        private LoggedParticleEmitter(EnumParticleTypes data, Vec3d motion, boolean withinBlockSpace) {
            this.particleName = data == null ? "custom" : data.name();
            this.motion = motion == null ? Vec3d.ZERO : motion;
            this.withinBlockSpace = withinBlockSpace;
        }

        @Override
        public void create(net.createmod.ponder.api.level.PonderLevel world, double x, double y, double z) {
        }
    }
}
