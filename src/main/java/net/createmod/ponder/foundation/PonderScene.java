package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.ui.PonderGuiSnapshotRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PonderScene {

    public static final String TITLE_KEY = "header";

    public static final class RecordedOperation {
        private final int tick;
        private final String description;

        public RecordedOperation(int tick, String description) {
            this.tick = tick;
            this.description = description;
        }

        public int getTick() {
            return tick;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum WorldEventType {
        SHOW_SECTION,
        HIDE_SECTION,
        RESTORE_BLOCKS,
        SET_BLOCKS,
        SET_BLOCK,
        REPLACE_BLOCKS,
        APPLY_NBT,
        DESTROY_BLOCK,
        BREAK_PROGRESS,
        MOVE_SECTION,
        ROTATE_SECTION
    }

    public static final class WorldEvent {
        private final int tick;
        private final WorldEventType type;
        private final List<BlockPos> positions;
        private final String stateDescription;
        private final EnumFacing direction;
        private final int duration;
        private final Vec3d offset;
        private final Vec3d rotation;
        private final Vec3d pivot;

        public WorldEvent(int tick, WorldEventType type, List<BlockPos> positions, String stateDescription,
            EnumFacing direction) {
            this(tick, type, positions, stateDescription, direction, 0, null, null, null);
        }

        public WorldEvent(int tick, WorldEventType type, List<BlockPos> positions, String stateDescription,
            EnumFacing direction, int duration, Vec3d offset, Vec3d rotation, Vec3d pivot) {
            this.tick = tick;
            this.type = type;
            this.positions = Collections.unmodifiableList(positions);
            this.stateDescription = stateDescription;
            this.direction = direction;
            this.duration = Math.max(0, duration);
            this.offset = offset;
            this.rotation = rotation;
            this.pivot = pivot;
        }

        public int getTick() {
            return tick;
        }

        public WorldEventType getType() {
            return type;
        }

        public List<BlockPos> getPositions() {
            return positions;
        }

        public String getStateDescription() {
            return stateDescription;
        }

        public EnumFacing getDirection() {
            return direction;
        }

        public int getDuration() {
            return duration;
        }

        public Vec3d getOffset() {
            return offset;
        }

        public Vec3d getRotation() {
            return rotation;
        }

        public Vec3d getPivot() {
            return pivot;
        }
    }

    public static final class CameraEvent {
        private final int tick;
        private final float yawDegrees;

        public CameraEvent(int tick, float yawDegrees) {
            this.tick = tick;
            this.yawDegrees = yawDegrees;
        }

        public int getTick() {
            return tick;
        }

        public float getYawDegrees() {
            return yawDegrees;
        }
    }

    public static final class PoiEvent {
        private final int tick;
        private final Vec3d location;

        public PoiEvent(int tick, Vec3d location) {
            this.tick = tick;
            this.location = location;
        }

        public int getTick() {
            return tick;
        }

        public Vec3d getLocation() {
            return location;
        }
    }

    public static final class ParticleEffectEvent {
        private final int tick;
        private final Vec3d location;
        private final float amountPerCycle;
        private final int cycles;
        private final String particleName;
        private final Vec3d motion;
        private final boolean withinBlockSpace;

        public ParticleEffectEvent(int tick, Vec3d location, float amountPerCycle, int cycles, String particleName,
            Vec3d motion, boolean withinBlockSpace) {
            this.tick = tick;
            this.location = location;
            this.amountPerCycle = amountPerCycle;
            this.cycles = cycles;
            this.particleName = particleName;
            this.motion = motion;
            this.withinBlockSpace = withinBlockSpace;
        }

        public int getTick() {
            return tick;
        }

        public Vec3d getLocation() {
            return location;
        }

        public float getAmountPerCycle() {
            return amountPerCycle;
        }

        public int getCycles() {
            return cycles;
        }

        public String getParticleName() {
            return particleName;
        }

        public Vec3d getMotion() {
            return motion;
        }

        public boolean isWithinBlockSpace() {
            return withinBlockSpace;
        }
    }

    public enum ActorKind {
        BIRB,
        CART,
        ITEM
    }

    public enum ActorEventType {
        SPAWN,
        MOVE,
        ROTATE,
        HIDE,
        POSE
    }

    public static final class ActorEvent {
        private final int tick;
        private final int duration;
        private final int actorId;
        private final ActorKind actorKind;
        private final ActorEventType type;
        private final Vec3d location;
        private final Vec3d offset;
        private final Vec3d rotation;
        private final float angle;
        private final String poseName;
        private final String displayName;
        private final ItemStack itemStack;

        public ActorEvent(int tick, int duration, int actorId, ActorKind actorKind, ActorEventType type,
            Vec3d location, Vec3d offset, Vec3d rotation, float angle, String poseName, String displayName,
            ItemStack itemStack) {
            this.tick = tick;
            this.duration = duration;
            this.actorId = actorId;
            this.actorKind = actorKind;
            this.type = type;
            this.location = location;
            this.offset = offset;
            this.rotation = rotation;
            this.angle = angle;
            this.poseName = poseName;
            this.displayName = displayName;
            this.itemStack = itemStack == null ? ItemStack.EMPTY : itemStack.copy();
        }

        public int getTick() {
            return tick;
        }

        public int getDuration() {
            return duration;
        }

        public int getActorId() {
            return actorId;
        }

        public ActorKind getActorKind() {
            return actorKind;
        }

        public ActorEventType getType() {
            return type;
        }

        public Vec3d getLocation() {
            return location;
        }

        public Vec3d getOffset() {
            return offset;
        }

        public Vec3d getRotation() {
            return rotation;
        }

        public float getAngle() {
            return angle;
        }

        public String getPoseName() {
            return poseName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ItemStack getItemStack() {
            return itemStack == null ? ItemStack.EMPTY : itemStack;
        }
    }

    public enum OverlayEventType {
        TEXT,
        SCENE_OUTLINE,
        SCENE_LINE,
        VALUE_BOX,
        CONTROLS,
        GUI_TEXTURE,
        GUI_HIGHLIGHT
    }

    public static final class OverlayEvent {
        private final int tick;
        private final int duration;
        OverlayEventType type = OverlayEventType.TEXT;
        String text = "";
        PonderPalette palette = PonderPalette.WHITE;
        Vec3d pointAt;
        int independentY = -1;
        boolean placeNearTarget;
        boolean connectorVisible = true;
        String overlayId;
        String parentOverlayId;
        ResourceLocation textureLocation;
        ResourceLocation guiSnapshotId;
        int textureU;
        int textureV;
        int regionWidth;
        int regionHeight;
        int textureWidth = 256;
        int textureHeight = 256;
        int displayWidth;
        int displayHeight;
        int offsetX;
        int offsetY;
        boolean framed = true;
        boolean scaleToParent;
        boolean stretchTexture;
        int stretchBorder = 4;
        int captionX = Integer.MIN_VALUE;
        int captionY = Integer.MIN_VALUE;
        int captionOffsetX;
        int captionOffsetY;
        int guiX;
        int guiY;
        int guiWidth;
        int guiHeight;
        AxisAlignedBB sceneBounds;
        Pointing controlDirection;
        ItemStack controlItem = ItemStack.EMPTY;
        boolean controlLeftClick;
        boolean controlRightClick;
        boolean controlScroll;
        boolean controlSneaking;
        boolean controlCtrl;
        Vec3d lineStart;
        Vec3d lineEnd;
        boolean lineWide;
        Vec3d valueBoxCenter;
        Vec3d valueBoxExpand;

        OverlayEvent(int tick, int duration) {
            this.tick = tick;
            this.duration = duration;
        }

        public int getTick() {
            return tick;
        }

        public int getDuration() {
            return duration;
        }

        public OverlayEventType getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public int getColor() {
            return palette.getColor();
        }

        public PonderPalette getPalette() {
            return palette;
        }

        public Vec3d getPointAt() {
            return pointAt;
        }

        public int getIndependentY() {
            return independentY;
        }

        public boolean isPlaceNearTarget() {
            return placeNearTarget;
        }

        public boolean isConnectorVisible() {
            return connectorVisible;
        }

        public String getOverlayId() {
            return overlayId;
        }

        public String getParentOverlayId() {
            return parentOverlayId;
        }

        public ResourceLocation getTextureLocation() {
            return textureLocation;
        }

        public ResourceLocation getGuiSnapshotId() {
            return guiSnapshotId;
        }

        public int getTextureU() {
            return textureU;
        }

        public int getTextureV() {
            return textureV;
        }

        public int getRegionWidth() {
            return regionWidth;
        }

        public int getRegionHeight() {
            return regionHeight;
        }

        public int getTextureWidth() {
            return textureWidth;
        }

        public int getTextureHeight() {
            return textureHeight;
        }

        public int getDisplayWidth() {
            return displayWidth;
        }

        public int getDisplayHeight() {
            return displayHeight;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }

        public boolean isFramed() {
            return framed;
        }

        public boolean isScaleToParent() {
            return scaleToParent;
        }

        public boolean isStretchTexture() {
            return stretchTexture;
        }

        public int getStretchBorder() {
            return stretchBorder;
        }

        public int getCaptionX() {
            return captionX;
        }

        public int getCaptionY() {
            return captionY;
        }

        public int getCaptionOffsetX() {
            return captionOffsetX;
        }

        public int getCaptionOffsetY() {
            return captionOffsetY;
        }

        public int getGuiX() {
            return guiX;
        }

        public int getGuiY() {
            return guiY;
        }

        public int getGuiWidth() {
            return guiWidth;
        }

        public int getGuiHeight() {
            return guiHeight;
        }

        public AxisAlignedBB getSceneBounds() {
            return sceneBounds;
        }

        public Pointing getControlDirection() {
            return controlDirection;
        }

        public ItemStack getControlItem() {
            return controlItem == null ? ItemStack.EMPTY : controlItem;
        }

        public boolean isControlLeftClick() {
            return controlLeftClick;
        }

        public boolean isControlRightClick() {
            return controlRightClick;
        }

        public boolean isControlScroll() {
            return controlScroll;
        }

        public boolean isControlSneaking() {
            return controlSneaking;
        }

        public boolean isControlCtrl() {
            return controlCtrl;
        }

        public Vec3d getLineStart() {
            return lineStart;
        }

        public Vec3d getLineEnd() {
            return lineEnd;
        }

        public boolean isLineWide() {
            return lineWide;
        }

        public Vec3d getValueBoxCenter() {
            return valueBoxCenter;
        }

        public Vec3d getValueBoxExpand() {
            return valueBoxExpand;
        }

        public OverlayEvent text(String text) {
            this.text = text == null ? "" : text;
            return this;
        }

        public OverlayEvent palette(PonderPalette palette) {
            this.palette = palette == null ? PonderPalette.WHITE : palette;
            return this;
        }

        public OverlayEvent pointAt(Vec3d pointAt) {
            this.pointAt = pointAt;
            return this;
        }

        public OverlayEvent independent(int independentY) {
            this.independentY = independentY;
            return this;
        }

        public OverlayEvent placeNearTarget(boolean placeNearTarget) {
            this.placeNearTarget = placeNearTarget;
            return this;
        }

        public OverlayEvent connectorVisible(boolean connectorVisible) {
            this.connectorVisible = connectorVisible;
            return this;
        }

        public OverlayEvent overlayId(String overlayId) {
            this.overlayId = overlayId;
            return this;
        }

        public OverlayEvent parentOverlayId(String parentOverlayId) {
            this.parentOverlayId = parentOverlayId;
            return this;
        }

        public OverlayEvent guiOrigin(int guiX, int guiY) {
            this.guiX = guiX;
            this.guiY = guiY;
            return this;
        }

        public OverlayEvent captionPosition(int captionX, int captionY) {
            this.captionX = captionX;
            this.captionY = captionY;
            return this;
        }

        public OverlayEvent captionOffset(int captionOffsetX, int captionOffsetY) {
            this.captionOffsetX = captionOffsetX;
            this.captionOffsetY = captionOffsetY;
            return this;
        }

        public OverlayEvent guiTexture(ResourceLocation textureLocation, int textureU, int textureV, int regionWidth,
            int regionHeight, int textureWidth, int textureHeight, int displayWidth, int displayHeight, int offsetX,
            int offsetY, boolean framed) {
            this.type = OverlayEventType.GUI_TEXTURE;
            this.textureLocation = textureLocation;
            this.textureU = textureU;
            this.textureV = textureV;
            this.regionWidth = Math.max(1, regionWidth);
            this.regionHeight = Math.max(1, regionHeight);
            this.textureWidth = Math.max(1, textureWidth);
            this.textureHeight = Math.max(1, textureHeight);
            this.displayWidth = Math.max(1, displayWidth);
            this.displayHeight = Math.max(1, displayHeight);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.framed = framed;
            return this;
        }

        public OverlayEvent guiSnapshot(ResourceLocation snapshotId) {
            this.type = OverlayEventType.GUI_TEXTURE;
            this.guiSnapshotId = snapshotId;
            return this;
        }

        public OverlayEvent applySnapshot(PonderGuiSnapshotRegistry.Snapshot snapshot, int offsetX, int offsetY) {
            if (snapshot == null) {
                return this;
            }
            this.textureLocation = snapshot.texture;
            this.textureU = snapshot.u;
            this.textureV = snapshot.v;
            this.regionWidth = Math.max(1, snapshot.regionWidth);
            this.regionHeight = Math.max(1, snapshot.regionHeight);
            this.textureWidth = Math.max(1, snapshot.textureWidth);
            this.textureHeight = Math.max(1, snapshot.textureHeight);
            this.displayWidth = Math.max(1, snapshot.displayWidth);
            this.displayHeight = Math.max(1, snapshot.displayHeight);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.framed = snapshot.framed;
            return this;
        }

        public OverlayEvent scaleToParent(boolean scaleToParent) {
            this.scaleToParent = scaleToParent;
            return this;
        }

        public OverlayEvent stretchTexture(boolean stretchTexture, int stretchBorder) {
            this.stretchTexture = stretchTexture;
            this.stretchBorder = Math.max(1, stretchBorder);
            return this;
        }

        public OverlayEvent guiHighlight(String parentOverlayId, int guiX, int guiY, int guiWidth, int guiHeight) {
            this.type = OverlayEventType.GUI_HIGHLIGHT;
            this.parentOverlayId = parentOverlayId;
            this.guiX = guiX;
            this.guiY = guiY;
            this.guiWidth = Math.max(1, guiWidth);
            this.guiHeight = Math.max(1, guiHeight);
            return this;
        }

        public OverlayEvent offset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }

        public OverlayEvent sceneOutline(AxisAlignedBB sceneBounds) {
            this.type = OverlayEventType.SCENE_OUTLINE;
            this.sceneBounds = sceneBounds;
            return this;
        }

        public OverlayEvent controls(Vec3d pointAt, Pointing direction) {
            this.type = OverlayEventType.CONTROLS;
            this.pointAt = pointAt;
            this.controlDirection = direction;
            return this;
        }

        public OverlayEvent sceneLine(Vec3d start, Vec3d end, boolean wide) {
            this.type = OverlayEventType.SCENE_LINE;
            this.lineStart = start;
            this.lineEnd = end;
            this.lineWide = wide;
            return this;
        }

        public OverlayEvent valueBox(Vec3d center, Vec3d expand) {
            this.type = OverlayEventType.VALUE_BOX;
            this.valueBoxCenter = center;
            this.valueBoxExpand = expand;
            this.pointAt = center;
            return this;
        }

        public OverlayEvent controlItem(ItemStack stack) {
            this.controlItem = stack == null ? ItemStack.EMPTY : stack.copy();
            return this;
        }

        public OverlayEvent controlLeftClick() {
            this.controlLeftClick = true;
            return this;
        }

        public OverlayEvent controlRightClick() {
            this.controlRightClick = true;
            return this;
        }

        public OverlayEvent controlScroll() {
            this.controlScroll = true;
            return this;
        }

        public OverlayEvent controlSneaking() {
            this.controlSneaking = true;
            return this;
        }

        public OverlayEvent controlCtrl() {
            this.controlCtrl = true;
            return this;
        }
    }

    private final StoryBoardEntry entry;
    private final List<ResourceLocation> tags;
    private final List<StoryBoardEntry.SceneOrderingEntry> orderingEntries;
    private final List<RecordedOperation> recordedOperations = new ArrayList<RecordedOperation>();
    private final List<WorldEvent> worldEvents = new ArrayList<WorldEvent>();
    private final List<CameraEvent> cameraEvents = new ArrayList<CameraEvent>();
    private final List<PoiEvent> poiEvents = new ArrayList<PoiEvent>();
    private final List<ParticleEffectEvent> particleEvents = new ArrayList<ParticleEffectEvent>();
    private final List<ActorEvent> actorEvents = new ArrayList<ActorEvent>();
    private final List<OverlayEvent> overlayEvents = new ArrayList<OverlayEvent>();
    private final PonderSceneBuildingUtil sceneBuildingUtil;
    private final PonderLocalization localization;
    private PonderSchematic schematic = PonderSchematic.empty();
    private ResourceLocation sceneId;
    private String title = "";
    private int basePlateOffsetX;
    private int basePlateOffsetZ;
    private int basePlateSize = 5;
    private float scaleFactor = 1.0F;
    private boolean hidePlatformShadow;
    private float sceneOffsetY;
    private boolean nextUpEnabled = true;
    private boolean finished;
    private int totalIdleTicks;
    private int keyframeCount;
    private int lazyKeyframeCount;
    private float accumulatedCameraYaw;

    public PonderScene(StoryBoardEntry entry, PonderLocalization localization) {
        this.entry = entry;
        this.localization = localization;
        this.tags = Collections.unmodifiableList(new ArrayList<ResourceLocation>(entry.getTags()));
        this.orderingEntries = Collections.unmodifiableList(
            new ArrayList<StoryBoardEntry.SceneOrderingEntry>(entry.getOrderingEntries()));
        this.sceneId = entry.getSchematicLocation();
        this.sceneBuildingUtil = new PonderSceneBuildingUtil(this);
    }

    public StoryBoardEntry getEntry() {
        return entry;
    }

    public String getNamespace() {
        return entry.getNamespace();
    }

    public ResourceLocation getComponent() {
        return entry.getComponent();
    }

    public ResourceLocation getSchematicLocation() {
        return entry.getSchematicLocation();
    }

    public List<ResourceLocation> getTags() {
        return tags;
    }

    public List<StoryBoardEntry.SceneOrderingEntry> getOrderingEntries() {
        return orderingEntries;
    }

    public ResourceLocation getSceneId() {
        return sceneId;
    }

    public void setSceneId(ResourceLocation sceneId) {
        this.sceneId = sceneId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBasePlateOffsetX() {
        return basePlateOffsetX;
    }

    public void setBasePlateOffsetX(int basePlateOffsetX) {
        this.basePlateOffsetX = basePlateOffsetX;
    }

    public int getBasePlateOffsetZ() {
        return basePlateOffsetZ;
    }

    public void setBasePlateOffsetZ(int basePlateOffsetZ) {
        this.basePlateOffsetZ = basePlateOffsetZ;
    }

    public int getBasePlateSize() {
        return basePlateSize;
    }

    public void setBasePlateSize(int basePlateSize) {
        this.basePlateSize = basePlateSize;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean isHidePlatformShadow() {
        return hidePlatformShadow;
    }

    public void setHidePlatformShadow(boolean hidePlatformShadow) {
        this.hidePlatformShadow = hidePlatformShadow;
    }

    public float getSceneOffsetY() {
        return sceneOffsetY;
    }

    public void setSceneOffsetY(float sceneOffsetY) {
        this.sceneOffsetY = sceneOffsetY;
    }

    public boolean isNextUpEnabled() {
        return nextUpEnabled;
    }

    public void setNextUpEnabled(boolean nextUpEnabled) {
        this.nextUpEnabled = nextUpEnabled;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getTotalIdleTicks() {
        return totalIdleTicks;
    }

    public void addIdleTicks(int ticks) {
        totalIdleTicks += Math.max(0, ticks);
    }

    public int getKeyframeCount() {
        return keyframeCount;
    }

    public void incrementKeyframeCount() {
        keyframeCount++;
    }

    public int getLazyKeyframeCount() {
        return lazyKeyframeCount;
    }

    public void incrementLazyKeyframeCount() {
        lazyKeyframeCount++;
    }

    public float getAccumulatedCameraYaw() {
        return accumulatedCameraYaw;
    }

    public void addCameraYaw(float degrees) {
        accumulatedCameraYaw += degrees;
    }

    public void recordCameraYaw(float degrees) {
        if (degrees == 0.0F) {
            return;
        }
        cameraEvents.add(new CameraEvent(totalIdleTicks, degrees));
    }

    public void recordPointOfInterest(Vec3d location) {
        if (location == null) {
            return;
        }
        poiEvents.add(new PoiEvent(totalIdleTicks, location));
    }

    public void recordParticleEffect(Vec3d location, float amountPerCycle, int cycles, String particleName,
        Vec3d motion, boolean withinBlockSpace) {
        if (location == null || cycles <= 0) {
            return;
        }
        particleEvents.add(new ParticleEffectEvent(totalIdleTicks, location, amountPerCycle, cycles,
            particleName == null ? "custom" : particleName, motion == null ? Vec3d.ZERO : motion,
            withinBlockSpace));
    }

    public void recordActorSpawn(int actorId, ActorKind kind, Vec3d location, int duration, float angle, String poseName) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, kind, ActorEventType.SPAWN,
            location, null, null, angle, poseName, null, ItemStack.EMPTY));
    }

    public void recordItemActorSpawn(int actorId, Vec3d location, int duration, String displayName, ItemStack stack) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, ActorKind.ITEM,
            ActorEventType.SPAWN, location, null, null, 0.0F, null, displayName, stack));
    }

    public void recordActorMove(int actorId, ActorKind kind, Vec3d offset, int duration) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, kind, ActorEventType.MOVE, null,
            offset, null, 0.0F, null, null, ItemStack.EMPTY));
    }

    public void recordActorRotation(int actorId, ActorKind kind, Vec3d rotation, int duration) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, kind, ActorEventType.ROTATE,
            null, null, rotation, 0.0F, null, null, ItemStack.EMPTY));
    }

    public void recordActorCartRotation(int actorId, float angle, int duration) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, ActorKind.CART,
            ActorEventType.ROTATE, null, null, null, angle, null, null, ItemStack.EMPTY));
    }

    public void recordActorHide(int actorId, ActorKind kind, int duration) {
        actorEvents.add(new ActorEvent(totalIdleTicks, Math.max(1, duration), actorId, kind, ActorEventType.HIDE,
            null, null, null, 0.0F, null, null, ItemStack.EMPTY));
    }

    public void recordActorPose(int actorId, String poseName) {
        actorEvents.add(new ActorEvent(totalIdleTicks, 1, actorId, ActorKind.BIRB, ActorEventType.POSE, null, null,
            null, 0.0F, poseName, null, ItemStack.EMPTY));
    }

    public OverlayEvent createOverlayEvent(int duration) {
        OverlayEvent event = new OverlayEvent(totalIdleTicks, Math.max(1, duration));
        overlayEvents.add(event);
        return event;
    }

    public PonderSceneBuildingUtil getSceneBuildingUtil() {
        return sceneBuildingUtil;
    }

    public PonderLocalization getLocalization() {
        return localization;
    }

    public PonderSchematic getSchematic() {
        return schematic;
    }

    public void setSchematic(PonderSchematic schematic) {
        this.schematic = schematic == null ? PonderSchematic.empty() : schematic;
    }

    public void recordOperation(String operation) {
        recordedOperations.add(new RecordedOperation(totalIdleTicks, operation));
    }

    public List<RecordedOperation> getRecordedOperations() {
        return Collections.unmodifiableList(recordedOperations);
    }

    public void recordWorldEvent(WorldEventType type, Iterable<BlockPos> positions, String stateDescription) {
        recordWorldEvent(type, positions, stateDescription, null);
    }

    public void recordWorldEvent(WorldEventType type, Iterable<BlockPos> positions, String stateDescription,
        EnumFacing direction) {
        recordWorldEvent(type, positions, stateDescription, direction, 0, null, null, null);
    }

    public void recordWorldEvent(WorldEventType type, Iterable<BlockPos> positions, String stateDescription,
        EnumFacing direction, int duration, Vec3d offset, Vec3d rotation, Vec3d pivot) {
        List<BlockPos> copiedPositions = new ArrayList<BlockPos>();
        for (BlockPos pos : positions) {
            copiedPositions.add(new BlockPos(pos));
        }

        if (copiedPositions.isEmpty()) {
            return;
        }

        worldEvents.add(new WorldEvent(totalIdleTicks, type, copiedPositions, stateDescription, direction, duration,
            offset, rotation, pivot));
    }

    public void recordWorldEvent(WorldEventType type, BlockPos pos, String stateDescription) {
        recordWorldEvent(type, pos, stateDescription, null);
    }

    public void recordWorldEvent(WorldEventType type, BlockPos pos, String stateDescription, EnumFacing direction) {
        List<BlockPos> copiedPositions = new ArrayList<BlockPos>(1);
        copiedPositions.add(new BlockPos(pos));
        worldEvents.add(new WorldEvent(totalIdleTicks, type, copiedPositions, stateDescription, direction));
    }

    public void recordSectionMove(Iterable<BlockPos> positions, Vec3d offset, int duration) {
        recordWorldEvent(WorldEventType.MOVE_SECTION, positions, null, null, duration, offset, null, null);
    }

    public void recordSectionRotation(Iterable<BlockPos> positions, Vec3d pivot, Vec3d rotation, int duration) {
        recordWorldEvent(WorldEventType.ROTATE_SECTION, positions, null, null, duration, null, rotation, pivot);
    }

    public List<WorldEvent> getWorldEvents() {
        return Collections.unmodifiableList(worldEvents);
    }

    public List<CameraEvent> getCameraEvents() {
        return Collections.unmodifiableList(cameraEvents);
    }

    public List<PoiEvent> getPointOfInterestEvents() {
        return Collections.unmodifiableList(poiEvents);
    }

    public List<ParticleEffectEvent> getParticleEvents() {
        return Collections.unmodifiableList(particleEvents);
    }

    public List<ActorEvent> getActorEvents() {
        return Collections.unmodifiableList(actorEvents);
    }

    public List<OverlayEvent> getOverlayEvents() {
        return Collections.unmodifiableList(overlayEvents);
    }

    public List<String> getOperationLog() {
        List<String> operationLog = new ArrayList<String>(recordedOperations.size());
        for (RecordedOperation recordedOperation : recordedOperations) {
            operationLog.add(recordedOperation.getDescription());
        }
        return Collections.unmodifiableList(operationLog);
    }

    public void program(SceneBuilder scene, SceneBuildingUtil util) {
        entry.getBoard().program(scene, util);
    }
}
