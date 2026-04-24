package net.createmod.ponder.foundation.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.registration.TagBuilder;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.createmod.ponder.foundation.ui.PonderGuiSnapshotRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;

public final class ExternalPonderScenes {

    private static final Map<String, File> SCRIPT_JSON_SOURCES = new LinkedHashMap<String, File>();

    private ExternalPonderScenes() {
    }

    public static synchronized void rememberScriptJson(String path) {
        File resolved = resolveQueuedPath(path);
        if (resolved == null) {
            Ponder.LOGGER.warn("Skipping external ponder json path '{}': file or directory not found", path);
            return;
        }

        try {
            SCRIPT_JSON_SOURCES.put(resolved.getCanonicalPath(), resolved);
        } catch (IOException exception) {
            SCRIPT_JSON_SOURCES.put(resolved.getAbsolutePath(), resolved);
        }
    }

    public static synchronized void clearRememberedScriptJson() {
        SCRIPT_JSON_SOURCES.clear();
    }

    public static void registerLoadedScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ExternalDefinitionSet definitions = loadDefinitions();
        for (SceneDefinition definition : definitions.scenes) {
            try {
                PonderStoryBoard storyBoard = createStoryBoard(definition);
                for (ComponentDefinition component : definition.components) {
                    StoryBoardEntry entry = helper.addStoryBoard(component.componentId,
                        parseLocation(definition.schematic, definition.namespace), storyBoard,
                        definition.tags.toArray(new ResourceLocation[definition.tags.size()]));
                    PonderIndex.registerComponentMatcher(component.componentId, component.componentMatcher);

                    for (String before : definition.orderBefore) {
                        applySceneOrdering(entry, definition.namespace, before, true);
                    }
                    for (String after : definition.orderAfter) {
                        applySceneOrdering(entry, definition.namespace, after, false);
                    }
                }
            } catch (RuntimeException exception) {
                Ponder.LOGGER.error("Failed to register external ponder scene '{}:{}' from {}", definition.namespace,
                    definition.sceneId, definition.source, exception);
            }
        }
    }

    public static void registerLoadedTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ExternalDefinitionSet definitions = loadDefinitions();
        Set<ResourceLocation> registeredTags = new LinkedHashSet<ResourceLocation>();
        for (TagDefinition definition : definitions.tags) {
            if (!registeredTags.add(definition.id)) {
                Ponder.LOGGER.warn("Skipping duplicate external ponder tag definition '{}'", definition.id);
                continue;
            }

            try {
                TagBuilder builder = helper.registerTag(definition.id)
                    .title(definition.title)
                    .description(definition.description);
                if (definition.icon != null) {
                    builder.icon(definition.icon);
                }
                if (!definition.itemStack.isEmpty()) {
                    builder.item(definition.itemStack, definition.useItemAsIcon, definition.useItemAsMainItem);
                }
                if (definition.addToIndex) {
                    builder.addToIndex();
                }
                builder.register();
            } catch (RuntimeException exception) {
                Ponder.LOGGER.error("Failed to register external ponder tag '{}' from {}", definition.id,
                    definition.source, exception);
            }
        }

        Set<String> assignments = new LinkedHashSet<String>();
        for (SceneDefinition definition : definitions.scenes) {
            for (ComponentDefinition component : definition.components) {
                for (ResourceLocation tag : definition.componentTags) {
                    String assignmentKey = component.componentId + "|" + tag;
                    if (assignments.add(assignmentKey)) {
                        helper.addTagToComponent(component.componentId, tag);
                    }
                }
            }
        }
    }

    private static void applySceneOrdering(StoryBoardEntry entry, String namespace, String sceneId, boolean before) {
        if (sceneId == null || sceneId.trim().isEmpty()) {
            return;
        }

        ResourceLocation location = parseLocation(sceneId, namespace);
        if (before) {
            entry.orderBefore(location.getNamespace(), location.getPath());
        } else {
            entry.orderAfter(location.getNamespace(), location.getPath());
        }
    }

    private static PonderStoryBoard createStoryBoard(final SceneDefinition definition) {
        return new PonderStoryBoard() {
            @Override
            public void program(SceneBuilder scene, SceneBuildingUtil util) {
                scene.title(definition.sceneId, definition.title);
                for (SceneOperation operation : definition.operations) {
                    applyOperation(scene, util, operation);
                }
            }
        };
    }

    private static void applyOperation(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        String type = operation.type;
        if ("base_plate".equals(type)) {
            scene.configureBasePlate(operation.xOffset, operation.zOffset, operation.size);
            return;
        }
        if ("show_base_plate".equals(type)) {
            scene.showBasePlate();
            return;
        }
        if ("scene_scale".equals(type)) {
            scene.scaleSceneView(operation.factor);
            return;
        }
        if ("scene_offset_y".equals(type)) {
            scene.setSceneOffsetY(operation.offsetY);
            return;
        }
        if ("remove_shadow".equals(type)) {
            scene.removeShadow();
            return;
        }
        if ("next_up".equals(type)) {
            scene.setNextUpEnabled(operation.enabled);
            return;
        }
        if ("idle".equals(type)) {
            scene.idle(operation.ticks);
            return;
        }
        if ("keyframe".equals(type)) {
            scene.addKeyframe();
            return;
        }
        if ("lazy_keyframe".equals(type)) {
            scene.addLazyKeyframe();
            return;
        }
        if ("mark_finished".equals(type)) {
            scene.markAsFinished();
            return;
        }
        if ("rotate_camera_y".equals(type)) {
            scene.rotateCameraY(operation.degrees);
            return;
        }
        if ("set_blocks".equals(type)) {
            scene.world().setBlocks(toSelection(util, operation),
                parseBlockState(operation.blockState, operation.blockMeta), true);
            enqueueNbtWorldEvent(scene, util, operation, "set_blocks");
            return;
        }
        if ("set_block".equals(type)) {
            scene.world().setBlock(toBlockPos(util, operation.pos),
                parseBlockState(operation.blockState, operation.blockMeta), true);
            enqueueNbtWorldEvent(scene, util, operation, "set_block");
            return;
        }
        if ("replace_blocks".equals(type)) {
            scene.world().replaceBlocks(toSelection(util, operation),
                parseBlockState(operation.blockState, operation.blockMeta), true);
            enqueueNbtWorldEvent(scene, util, operation, "replace_blocks");
            return;
        }
        if ("show_section".equals(type)) {
            scene.world().showSection(toSelection(util, operation), parseFacing(operation.direction));
            return;
        }
        if ("hide_section".equals(type)) {
            scene.world().hideSection(toSelection(util, operation), parseFacing(operation.direction));
            return;
        }
        if ("restore_blocks".equals(type)) {
            scene.world().restoreBlocks(toSelection(util, operation));
            return;
        }
        if ("destroy_block".equals(type)) {
            scene.world().destroyBlock(toBlockPos(util, operation.pos));
            return;
        }
        if ("break_progress".equals(type)) {
            int repeats = Math.max(1, operation.times);
            for (int i = 0; i < repeats; i++) {
                scene.world().incrementBlockBreakingProgress(toBlockPos(util, operation.pos));
            }
            return;
        }
        if ("toggle_redstone_power".equals(type)) {
            scene.world().toggleRedstonePower(toSelection(util, operation));
            return;
        }
        if ("indicate_redstone".equals(type)) {
            scene.effects().indicateRedstone(toBlockPos(util, operation.pos));
            return;
        }
        if ("indicate_success".equals(type)) {
            scene.effects().indicateSuccess(toBlockPos(util, operation.pos));
            return;
        }
        if ("move_section".equals(type)) {
            Vec3d offset = toVec(operation.offset);
            if (offset == null) {
                throw new IllegalArgumentException("move_section operation requires offset");
            }
            scene.getScene().recordOperation("world.moveSection(" + String.valueOf(toSelection(util, operation)) + ", "
                + offset + ", " + operation.duration + ")");
            scene.getScene().recordSectionMove(collectTargetPositions(util, operation), offset, operation.duration);
            return;
        }
        if ("rotate_section".equals(type)) {
            Vec3d rotation = resolveRotation(operation);
            if (rotation == null) {
                throw new IllegalArgumentException("rotate_section operation requires rotation/degrees");
            }
            Vec3d pivot = resolveRotationPivot(util, operation);
            scene.getScene().recordOperation("world.rotateSection(" + String.valueOf(toSelection(util, operation)) + ", "
                + rotation + ", pivot=" + pivot + ", " + operation.duration + ")");
            scene.getScene().recordSectionRotation(collectTargetPositions(util, operation), pivot, rotation,
                operation.duration);
            return;
        }
        if ("text".equals(type)) {
            enqueueTextOverlay(scene, util, operation, "overlay.showText(" + operation.duration + ")");
            return;
        }
        if ("outline_text".equals(type)) {
            enqueueTextOverlay(scene, util, operation,
                "overlay.showOutlineWithText(" + String.valueOf(toSelection(util, operation)) + ", "
                    + operation.duration + ")");
            return;
        }
        if ("gui_texture".equals(type)) {
            enqueueGuiTextureOverlay(scene, util, operation);
            return;
        }
        if ("gui_snapshot".equals(type)) {
            enqueueGuiSnapshotOverlay(scene, util, operation);
            return;
        }
        if ("block_gui".equals(type) || "machine_gui".equals(type)) {
            enqueueBlockGuiOverlay(scene, util, operation);
            return;
        }
        if ("gui_outline_text".equals(type)) {
            enqueueGuiHighlightOverlay(scene, util, operation);
            return;
        }
        if ("gui_interaction".equals(type) || "sequence".equals(type)) {
            for (SceneOperation childOperation : operation.childOperations) {
                applyOperation(scene, util, childOperation);
            }
            return;
        }

        Ponder.LOGGER.warn("Unsupported external ponder operation '{}'", type);
    }

    private static void enqueueTextOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation,
        String operationLog) {
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .text(operation.text)
            .palette(parsePalette(operation.color))
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .captionOffset(operation.captionOffsetX, operation.captionOffsetY)
            .placeNearTarget(operation.placeNearTarget);
        Vec3d anchor = resolveOverlayAnchor(util, operation);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
        if (operation.captionX != Integer.MIN_VALUE || operation.captionY != Integer.MIN_VALUE) {
            overlayEvent.captionPosition(operation.captionX, operation.captionY);
        }

        scene.getScene().recordOperation(operationLog + ".text(\"" + operation.text + "\")");
    }

    private static void enqueueGuiTextureOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.texture == null || operation.texture.trim().isEmpty()) {
            throw new IllegalArgumentException("gui_texture operation requires a texture");
        }

        int regionWidth = operation.regionWidth > 0 ? operation.regionWidth
            : operation.displayWidth > 0 ? operation.displayWidth : operation.textureWidth;
        int regionHeight = operation.regionHeight > 0 ? operation.regionHeight
            : operation.displayHeight > 0 ? operation.displayHeight : operation.textureHeight;
        int displayWidth = operation.displayWidth > 0 ? operation.displayWidth : regionWidth;
        int displayHeight = operation.displayHeight > 0 ? operation.displayHeight : regionHeight;
        Vec3d anchor = resolveOverlayAnchor(util, operation);
        ResourceLocation textureLocation = parseTextureLocation(operation.texture, scene.getScene().getNamespace());

        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(parsePalette(operation.color))
            .guiTexture(textureLocation, operation.textureU, operation.textureV, regionWidth, regionHeight,
                operation.textureWidth, operation.textureHeight, displayWidth, displayHeight, operation.offsetX,
                Math.round(operation.offsetY), operation.framed)
            .guiOrigin(operation.guiX, operation.guiY)
            .parentOverlayId(operation.parentGuiId)
            .scaleToParent(operation.scaleToParent)
            .stretchTexture(operation.stretchTexture, operation.stretchBorder)
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .placeNearTarget(operation.placeNearTarget);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }

        scene.getScene().recordOperation("overlay.showGuiTexture(" + textureLocation + ", " + regionWidth + "x"
            + regionHeight + " -> " + displayWidth + "x" + displayHeight + ", " + operation.duration + ")");
    }

    private static void enqueueGuiSnapshotOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        Vec3d anchor = resolveOverlayAnchor(util, operation);
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(parsePalette(operation.color))
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .placeNearTarget(operation.placeNearTarget)
            .offset(operation.offsetX, Math.round(operation.offsetY));

        if (operation.snapshot != null && !operation.snapshot.trim().isEmpty()) {
            ResourceLocation snapshotId = parseLocation(operation.snapshot, scene.getScene().getNamespace());
            overlayEvent.guiSnapshot(snapshotId);
            if (anchor != null) {
                overlayEvent.pointAt(anchor);
            }
            if (operation.independentY != Integer.MIN_VALUE) {
                overlayEvent.independent(operation.independentY);
            }
            scene.getScene().recordOperation("overlay.showGuiSnapshot(" + snapshotId + ", " + operation.duration + ")");
            return;
        }

        if (operation.texture == null || operation.texture.trim().isEmpty()) {
            throw new IllegalArgumentException("gui_snapshot operation requires snapshot or texture");
        }

        ResourceLocation textureLocation = parseTextureLocation(operation.texture, scene.getScene().getNamespace());
        int width = operation.textureWidth > 0 ? operation.textureWidth : operation.regionWidth;
        int height = operation.textureHeight > 0 ? operation.textureHeight : operation.regionHeight;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("gui_snapshot texture form requires textureWidth and textureHeight");
        }

        overlayEvent.guiTexture(textureLocation, 0, 0, width, height, width, height, width, height, operation.offsetX,
            Math.round(operation.offsetY), operation.framed);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
        scene.getScene().recordOperation("overlay.showGuiSnapshot(" + textureLocation + ", " + width + "x" + height
            + ", " + operation.duration + ")");
    }

    private static void enqueueBlockGuiOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.blockGui == null || operation.blockGui.trim().isEmpty()) {
            throw new IllegalArgumentException("block_gui operation requires blockGui");
        }
        if (operation.guiWidth <= 0 || operation.guiHeight <= 0) {
            throw new IllegalArgumentException("block_gui operation requires guiWidth and guiHeight");
        }

        ResourceLocation blockId = parseLocation(operation.blockGui, "minecraft");
        int meta = Math.max(0, operation.blockMeta);
        ResourceLocation snapshotId = PonderGuiSnapshotRegistry.registerBlockGuiSnapshot(blockId, meta,
            operation.guiWidth, operation.guiHeight);
        Vec3d anchor = resolveOverlayAnchor(util, operation);
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(parsePalette(operation.color))
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .placeNearTarget(operation.placeNearTarget)
            .offset(operation.offsetX, Math.round(operation.offsetY))
            .guiSnapshot(snapshotId);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
        scene.getScene().recordOperation("overlay.showBlockGui(" + blockId + ", meta=" + meta + ", "
            + operation.guiWidth + "x" + operation.guiHeight + ", " + operation.duration + ")");
    }

    private static void enqueueGuiHighlightOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.parentOverlayId == null || operation.parentOverlayId.trim().isEmpty()) {
            throw new IllegalArgumentException("gui_outline_text operation requires a gui id");
        }
        if (operation.guiWidth <= 0 || operation.guiHeight <= 0) {
            throw new IllegalArgumentException("gui_outline_text operation requires guiWidth and guiHeight");
        }

        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .text(operation.text)
            .palette(parsePalette(operation.color))
            .guiHighlight(operation.parentOverlayId, operation.guiX, operation.guiY, operation.guiWidth,
                operation.guiHeight)
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .captionOffset(operation.captionOffsetX, operation.captionOffsetY)
            .placeNearTarget(operation.placeNearTarget);
        Vec3d anchor = resolveOverlayAnchor(util, operation);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
        if (operation.captionX != Integer.MIN_VALUE || operation.captionY != Integer.MIN_VALUE) {
            overlayEvent.captionPosition(operation.captionX, operation.captionY);
        }

        scene.getScene().recordOperation("overlay.showGuiHighlight(" + operation.parentOverlayId + ", "
            + operation.guiX + ", " + operation.guiY + ", " + operation.guiWidth + ", " + operation.guiHeight
            + ").text(\"" + operation.text + "\")");
    }

    private static ExternalDefinitionSet loadDefinitions() {
        List<File> files = collectJsonFiles();
        ExternalDefinitionSet definitions = new ExternalDefinitionSet();
        for (File file : files) {
            mergeDefinitions(definitions, parseDefinitionFile(file));
        }
        return definitions;
    }

    private static List<File> collectJsonFiles() {
        Set<String> seenPaths = new LinkedHashSet<String>();
        List<File> files = new ArrayList<File>();

        for (File root : getAutoScanRoots()) {
            addJsonFiles(root, files, seenPaths);
        }

        Collection<File> queuedSources;
        synchronized (ExternalPonderScenes.class) {
            queuedSources = new ArrayList<File>(SCRIPT_JSON_SOURCES.values());
        }
        for (File source : queuedSources) {
            addJsonFiles(source, files, seenPaths);
        }

        return files;
    }

    private static List<File> getAutoScanRoots() {
        File gameDir = getGameDir();
        if (gameDir == null) {
            return Collections.emptyList();
        }

        List<File> roots = new ArrayList<File>();
        roots.add(new File(gameDir, "scripts/ponder"));
        roots.add(new File(gameDir, "ponder"));
        roots.add(new File(gameDir, "config/ponder"));
        return roots;
    }

    private static void addJsonFiles(File source, List<File> files, Set<String> seenPaths) {
        if (source == null || !source.exists()) {
            return;
        }

        if (source.isFile()) {
            if (source.getName().toLowerCase(Locale.ROOT).endsWith(".json")) {
                rememberFile(files, seenPaths, source);
            }
            return;
        }

        try (Stream<Path> walk = Files.walk(source.toPath())) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                .forEach(path -> rememberFile(files, seenPaths, path.toFile()));
        } catch (IOException exception) {
            Ponder.LOGGER.warn("Failed to scan external ponder scene directory {}", source, exception);
        }
    }

    private static void rememberFile(List<File> files, Set<String> seenPaths, File file) {
        try {
            String canonicalPath = file.getCanonicalPath();
            if (seenPaths.add(canonicalPath)) {
                files.add(file);
            }
        } catch (IOException exception) {
            String absolutePath = file.getAbsolutePath();
            if (seenPaths.add(absolutePath)) {
                files.add(file);
            }
        }
    }

    private static void mergeDefinitions(ExternalDefinitionSet target, ExternalDefinitionSet source) {
        if (target == null || source == null) {
            return;
        }
        target.tags.addAll(source.tags);
        target.scenes.addAll(source.scenes);
        for (Map.Entry<ResourceLocation, InteractionDefinition> entry : source.interactions.entrySet()) {
            if (target.interactions.containsKey(entry.getKey())) {
                Ponder.LOGGER.warn("Skipping duplicate external ponder interaction definition '{}'", entry.getKey());
                continue;
            }
            target.interactions.put(entry.getKey(), entry.getValue());
        }
    }

    private static ExternalDefinitionSet parseDefinitionFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement root = new JsonParser().parse(reader);
            ExternalDefinitionSet definitions = new ExternalDefinitionSet();
            if (root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                String rootNamespace = readString(rootObject, "namespace", "ponder");
                definitions.tags.addAll(parseTagDefinitions(rootObject, file, rootNamespace));
                definitions.interactions.putAll(parseInteractionDefinitions(rootObject, file, rootNamespace));
                if (rootObject.has("scenes") && rootObject.get("scenes").isJsonArray()) {
                    JsonArray scenes = rootObject.getAsJsonArray("scenes");
                    for (JsonElement element : scenes) {
                        if (element.isJsonObject()) {
                            definitions.scenes.add(parseSceneObject(element.getAsJsonObject(), file, rootNamespace,
                                definitions.interactions));
                        }
                    }
                } else if (looksLikeSceneObject(rootObject)) {
                    definitions.scenes.add(parseSceneObject(rootObject, file, rootNamespace, definitions.interactions));
                }
            } else if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        definitions.scenes.add(parseSceneObject(element.getAsJsonObject(), file, "ponder",
                            definitions.interactions));
                    }
                }
            } else {
                throw new JsonParseException("Root element must be an object or array");
            }
            return definitions;
        } catch (RuntimeException | IOException exception) {
            Ponder.LOGGER.error("Failed to parse external ponder scene file {}", file, exception);
            return new ExternalDefinitionSet();
        }
    }

    private static boolean looksLikeSceneObject(JsonObject object) {
        return object.has("component") || object.has("components") || object.has("schematic")
            || object.has("operations");
    }

    private static List<TagDefinition> parseTagDefinitions(JsonObject rootObject, File file, String defaultNamespace) {
        if (!rootObject.has("tagDefinitions") || !rootObject.get("tagDefinitions").isJsonArray()) {
            return Collections.emptyList();
        }

        List<TagDefinition> definitions = new ArrayList<TagDefinition>();
        for (JsonElement element : rootObject.getAsJsonArray("tagDefinitions")) {
            if (!element.isJsonObject()) {
                continue;
            }
            definitions.add(parseTagDefinition(element.getAsJsonObject(), file, defaultNamespace));
        }
        return definitions;
    }

    private static Map<ResourceLocation, InteractionDefinition> parseInteractionDefinitions(JsonObject rootObject,
        File file, String defaultNamespace) {
        JsonElement definitionsElement = hasField(rootObject, "interactionDefinitions")
            ? rootObject.get("interactionDefinitions")
            : hasField(rootObject, "guiInteractions") ? rootObject.get("guiInteractions") : null;
        if (definitionsElement == null || definitionsElement.isJsonNull()) {
            return Collections.emptyMap();
        }

        Map<ResourceLocation, InteractionDefinition> definitions = new LinkedHashMap<ResourceLocation, InteractionDefinition>();
        if (definitionsElement.isJsonArray()) {
            for (JsonElement element : definitionsElement.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                InteractionDefinition definition =
                    parseInteractionDefinition(element.getAsJsonObject(), null, file, defaultNamespace);
                if (definition != null && !definitions.containsKey(definition.id)) {
                    definitions.put(definition.id, definition);
                }
            }
            return definitions;
        }

        if (definitionsElement.isJsonObject()) {
            JsonObject object = definitionsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                InteractionDefinition definition =
                    parseInteractionDefinition(entry.getValue().getAsJsonObject(), entry.getKey(), file,
                        defaultNamespace);
                if (definition != null && !definitions.containsKey(definition.id)) {
                    definitions.put(definition.id, definition);
                }
            }
        }
        return definitions;
    }

    private static InteractionDefinition parseInteractionDefinition(JsonObject object, String fallbackId, File file,
        String defaultNamespace) {
        JsonArray stepsArray = extractInteractionSteps(object);
        if (stepsArray.size() == 0) {
            return null;
        }

        InteractionDefinition definition = new InteractionDefinition();
        definition.source = file.getAbsolutePath();
        definition.namespace = readString(object, "namespace", defaultNamespace);
        String interactionId = fallbackId == null ? readRequiredString(object, "id") : readString(object, "id", fallbackId);
        definition.id = parseLocation(interactionId, definition.namespace);
        definition.defaults = hasField(object, "defaults") && object.get("defaults").isJsonObject()
            ? copyJsonObject(object.getAsJsonObject("defaults"))
            : new JsonObject();
        for (JsonElement step : stepsArray) {
            if (step.isJsonObject()) {
                definition.steps.add(copyJsonObject(step.getAsJsonObject()));
            }
        }
        return definition;
    }

    private static TagDefinition parseTagDefinition(JsonObject object, File file, String defaultNamespace) {
        TagDefinition definition = new TagDefinition();
        definition.source = file.getAbsolutePath();
        definition.namespace = readString(object, "namespace", defaultNamespace);
        definition.id = parseLocation(readRequiredString(object, "id"), definition.namespace);
        definition.title = readString(object, "title", definition.id.toString());
        definition.description = readString(object, "description", "");
        definition.addToIndex = readBoolean(object, "addToIndex", true);
        definition.icon = readOptionalString(object, "icon");
        definition.itemStack = parseItemStack(readOptionalString(object, "item"),
            object.has("meta") ? readInt(object, "meta", 0) : 0, readNbtPayload(object.get("nbt")));
        definition.useItemAsIcon = readBoolean(object, "useItemAsIcon", true);
        definition.useItemAsMainItem = readBoolean(object, "useItemAsMainItem", true);
        return definition;
    }

    private static SceneDefinition parseSceneObject(JsonObject object, File file, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions) {
        SceneDefinition definition = new SceneDefinition();
        definition.source = file.getAbsolutePath();
        definition.namespace = readString(object, "namespace", defaultNamespace);
        definition.components = parseComponents(object, definition.namespace);
        definition.schematic = readRequiredString(object, "schematic");
        definition.sceneId = normalizeSceneId(readOptionalString(object, "sceneId"),
            stripJsonExtension(file.getName()));
        definition.title = readString(object, "title", definition.sceneId);
        definition.tags = parseLocations(object.get("tags"), definition.namespace);
        definition.componentTags = parseLocations(firstPresent(object, "componentTags", "groups"), definition.namespace);
        definition.orderBefore = parseStrings(object.get("orderBefore"));
        definition.orderAfter = parseStrings(object.get("orderAfter"));

        JsonArray operations = object.has("operations") && object.get("operations").isJsonArray()
            ? object.getAsJsonArray("operations")
            : new JsonArray();
        for (JsonElement operationElement : operations) {
            if (operationElement.isJsonObject()) {
                definition.operations.add(parseOperation(operationElement.getAsJsonObject(), definition.namespace,
                    interactions));
            }
        }
        return definition;
    }

    private static List<ComponentDefinition> parseComponents(JsonObject object, String namespace) {
        List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();
        if (object.has("components") && object.get("components").isJsonArray()) {
            for (JsonElement element : object.getAsJsonArray("components")) {
                if (element.isJsonObject()) {
                    definitions.add(parseComponentObject(element.getAsJsonObject(), object, namespace));
                } else if (element.isJsonPrimitive()) {
                    definitions.add(parseComponentObject(buildPrimitiveComponentObject(element.getAsString()), object,
                        namespace));
                }
            }
        } else {
            definitions.add(parseComponentObject(object, null, namespace));
        }
        return definitions;
    }

    private static JsonObject buildPrimitiveComponentObject(String component) {
        JsonObject object = new JsonObject();
        object.addProperty("component", component);
        return object;
    }

    private static ComponentDefinition parseComponentObject(JsonObject object, JsonObject defaults, String namespace) {
        ComponentDefinition definition = new ComponentDefinition();
        definition.componentItem = parseLocation(readRequiredString(object, defaults, "component"), "minecraft");
        definition.componentMeta = hasField(object, "componentMeta") ? readInt(object, "componentMeta", 0)
            : hasField(defaults, "componentMeta") ? readInt(defaults, "componentMeta", 0) : -1;
        definition.componentNbt = readNbtPayload(getElement(object, defaults, "componentNbt"));
        definition.componentDisplayNbt = readNbtPayload(getElement(object, defaults, "componentDisplayNbt"));
        definition.componentId = buildComponentId(definition.componentItem,
            readOptionalString(object, defaults, "componentKey"), definition.componentMeta, definition.componentNbt);
        definition.componentMatcher = PonderComponentMatcher.exact(definition.componentItem, definition.componentMeta,
            parseMatcherNbt(definition.componentNbt), parseMatcherNbt(definition.componentDisplayNbt));
        return definition;
    }

    private static ItemStack parseItemStack(String itemId, int meta, String rawNbt) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item item = Item.REGISTRY.getObject(parseLocation(itemId, "minecraft"));
        if (item == null) {
            Ponder.LOGGER.warn("Unknown item id '{}' in external ponder tag definition", itemId);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, 1, Math.max(0, meta));
        if (rawNbt != null && !rawNbt.trim().isEmpty()) {
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(rawNbt));
            } catch (NBTException exception) {
                Ponder.LOGGER.warn("Invalid item NBT payload in external ponder tag definition: {}", rawNbt,
                    exception);
            }
        }
        return stack;
    }

    private static SceneOperation parseOperation(JsonObject object, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions) {
        return parseOperation(object, defaultNamespace, interactions, new LinkedHashSet<ResourceLocation>());
    }

    private static SceneOperation parseOperation(JsonObject object, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions, Set<ResourceLocation> activeInteractions) {
        SceneOperation operation = new SceneOperation();
        operation.type = readRequiredString(object, "type").toLowerCase(Locale.ROOT);
        if ("gui_interaction".equals(operation.type) || "sequence".equals(operation.type)) {
            parseInteractionOperation(operation, object, defaultNamespace, interactions, activeInteractions);
            return operation;
        }
        operation.xOffset = readInt(object, "xOffset", 0);
        operation.zOffset = readInt(object, "zOffset", 0);
        operation.size = readInt(object, "size", 5);
        operation.duration = readInt(object, "duration", 40);
        operation.ticks = readInt(object, "ticks", 1);
        operation.times = readInt(object, "times", 1);
        operation.independentY = object.has("independentY") ? readInt(object, "independentY", 0) : Integer.MIN_VALUE;
        operation.factor = readFloat(object, "factor", 1.0F);
        operation.degrees = readFloat(object, "degrees", 0.0F);
        operation.offsetY = readFloat(object, "offsetY", 0.0F);
        operation.enabled = readBoolean(object, "enabled", true);
        operation.text = readString(object, "text", "");
        operation.color = readString(object, "color", "WHITE");
        operation.overlayId = readOptionalString(object, "id");
        operation.parentOverlayId = readOptionalString(object, "gui");
        operation.parentGuiId = readOptionalString(object, "parentGui");
        operation.blockGui = readOptionalString(object, "blockGui");
        operation.blockState = readOptionalString(object, "state");
        operation.blockMeta = object.has("meta") ? readInt(object, "meta", 0) : -1;
        operation.blockNbt = readNbtPayload(object.get("nbt"));
        operation.direction = readOptionalString(object, "direction");
        operation.captionX = object.has("captionX") ? readInt(object, "captionX", 0) : Integer.MIN_VALUE;
        operation.captionY = object.has("captionY") ? readInt(object, "captionY", 0) : Integer.MIN_VALUE;
        operation.captionOffsetX = readInt(object, "captionOffsetX", 0);
        operation.captionOffsetY = readInt(object, "captionOffsetY", 0);
        operation.connectorVisible = readBoolean(object, "connector", true);
        operation.placeNearTarget = readBoolean(object, "placeNearTarget", false);
        operation.pointMode = readString(object, "pointMode", "top");
        operation.snapshot = readOptionalString(object, "snapshot");
        operation.texture = readOptionalString(object, "texture");
        operation.textureU = readInt(object, "u", 0);
        operation.textureV = readInt(object, "v", 0);
        operation.regionWidth = object.has("regionWidth") ? readInt(object, "regionWidth", 0) : -1;
        operation.regionHeight = object.has("regionHeight") ? readInt(object, "regionHeight", 0) : -1;
        operation.displayWidth = object.has("displayWidth") ? readInt(object, "displayWidth", 0) : -1;
        operation.displayHeight = object.has("displayHeight") ? readInt(object, "displayHeight", 0) : -1;
        operation.textureWidth = readInt(object, "textureWidth", 256);
        operation.textureHeight = readInt(object, "textureHeight", 256);
        operation.offsetX = readInt(object, "offsetX", 0);
        operation.framed = readBoolean(object, "framed", true);
        operation.scaleToParent = readBoolean(object, "scaleToParent", operation.parentGuiId != null);
        operation.stretchTexture = readBoolean(object, "stretch", false);
        operation.stretchBorder = readInt(object, "stretchBorder", 4);
        operation.guiX = readInt(object, "guiX", 0);
        operation.guiY = readInt(object, "guiY", 0);
        operation.guiWidth = readInt(object, "guiWidth", -1);
        operation.guiHeight = readInt(object, "guiHeight", -1);
        operation.pos = parseIntArray(object.get("pos"), 3);
        operation.from = parseIntArray(object.get("from"), 3);
        operation.to = parseIntArray(object.get("to"), 3);
        operation.offset = parseDoubleArray(object.get("offset"), 3);
        operation.rotation = parseDoubleArray(firstPresent(object, "rotation", "rot"), 3);
        operation.pivot = parseDoubleArray(object.get("pivot"), 3);
        operation.rotX = readFloat(object, "rotX", 0.0F);
        operation.rotY = readFloat(object, "rotY", 0.0F);
        operation.rotZ = readFloat(object, "rotZ", 0.0F);
        operation.pointAt = parseDoubleArray(object.get("pointAt"), 3);
        return operation;
    }

    private static void parseInteractionOperation(SceneOperation operation, JsonObject object, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions, Set<ResourceLocation> activeInteractions) {
        if (!readBoolean(object, "enabled", true)) {
            operation.childOperations = Collections.emptyList();
            return;
        }

        JsonArray inlineSteps = extractInteractionSteps(object);
        InteractionDefinition referencedDefinition = null;
        String interactionName = readOptionalString(object, "interaction");
        if (interactionName != null && !interactionName.trim().isEmpty()) {
            ResourceLocation interactionId = parseLocation(interactionName, defaultNamespace);
            referencedDefinition = interactions.get(interactionId);
            if (referencedDefinition == null) {
                throw new IllegalArgumentException("Unknown gui interaction definition: " + interactionId);
            }
            if (!activeInteractions.add(interactionId)) {
                throw new IllegalArgumentException("Recursive gui interaction definition detected: " + interactionId);
            }
        }

        try {
            JsonObject mergedDefaults = referencedDefinition == null ? new JsonObject()
                : copyJsonObject(referencedDefinition.defaults);
            if (hasField(object, "defaults") && object.get("defaults").isJsonObject()) {
                mergedDefaults = mergeJsonObjects(mergedDefaults, object.getAsJsonObject("defaults"));
            }
            mergedDefaults = mergeJsonObjects(mergedDefaults, extractInteractionInvocationDefaults(object));

            List<SceneOperation> childOperations = new ArrayList<SceneOperation>();
            List<JsonObject> rawSteps = new ArrayList<JsonObject>();
            if (referencedDefinition != null) {
                rawSteps.addAll(referencedDefinition.steps);
            }
            for (JsonElement step : inlineSteps) {
                if (step.isJsonObject()) {
                    rawSteps.add(step.getAsJsonObject());
                }
            }

            String stepNamespace = referencedDefinition == null ? defaultNamespace : referencedDefinition.namespace;
            String scopeId = readOptionalString(object, "id");
            for (JsonObject rawStep : rawSteps) {
                JsonObject mergedStep = mergeJsonObjects(mergedDefaults, rawStep);
                if (scopeId != null && !scopeId.trim().isEmpty()) {
                    mergedStep = applyInteractionScope(mergedStep, scopeId.trim());
                }
                childOperations.add(parseOperation(mergedStep, stepNamespace, interactions, activeInteractions));
            }
            operation.childOperations = childOperations;
        } finally {
            if (referencedDefinition != null) {
                activeInteractions.remove(referencedDefinition.id);
            }
        }
    }

    private static JsonArray extractInteractionSteps(JsonObject object) {
        JsonElement stepsElement = hasField(object, "steps") ? object.get("steps")
            : hasField(object, "operations") ? object.get("operations") : null;
        if (stepsElement == null || !stepsElement.isJsonArray()) {
            return new JsonArray();
        }
        return stepsElement.getAsJsonArray();
    }

    private static JsonObject extractInteractionInvocationDefaults(JsonObject object) {
        JsonObject defaults = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            if ("type".equals(key) || "interaction".equals(key) || "steps".equals(key) || "operations".equals(key)
                || "defaults".equals(key) || "id".equals(key)) {
                continue;
            }
            defaults.add(key, entry.getValue());
        }
        return defaults;
    }

    private static JsonObject mergeJsonObjects(JsonObject defaults, JsonObject overrides) {
        JsonObject merged = copyJsonObject(defaults);
        if (overrides == null) {
            return merged;
        }
        for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
            merged.add(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    private static JsonObject copyJsonObject(JsonObject source) {
        if (source == null) {
            return new JsonObject();
        }
        return new JsonParser().parse(source.toString()).getAsJsonObject();
    }

    private static JsonObject applyInteractionScope(JsonObject stepObject, String scopeId) {
        JsonObject scoped = copyJsonObject(stepObject);
        prefixScopedField(scoped, "id", scopeId);
        prefixScopedField(scoped, "gui", scopeId);
        prefixScopedField(scoped, "parentGui", scopeId);
        return scoped;
    }

    private static void prefixScopedField(JsonObject object, String key, String scopeId) {
        if (!hasField(object, key) || !object.get(key).isJsonPrimitive()) {
            return;
        }

        String value = object.get(key).getAsString();
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        object.addProperty(key, scopeId + "." + value.trim());
    }

    private static Selection toSelection(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.from != null && operation.to != null) {
            return util.select().fromTo(operation.from[0], operation.from[1], operation.from[2], operation.to[0],
                operation.to[1], operation.to[2]);
        }
        if (operation.pos != null) {
            return util.select().position(operation.pos[0], operation.pos[1], operation.pos[2]);
        }
        throw new IllegalArgumentException("Selection operation requires either from/to or pos");
    }

    private static BlockPos toBlockPos(SceneBuildingUtil util, int[] pos) {
        if (pos == null) {
            throw new IllegalArgumentException("Block position is required");
        }
        return util.grid().at(pos[0], pos[1], pos[2]);
    }

    private static Vec3d toVec(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.pointAt == null) {
            return null;
        }

        if ("center".equalsIgnoreCase(operation.pointMode)) {
            return util.vector().centerOf((int) Math.round(operation.pointAt[0]), (int) Math.round(operation.pointAt[1]),
                (int) Math.round(operation.pointAt[2]));
        }
        if ("vector".equalsIgnoreCase(operation.pointMode)) {
            return util.vector().of(operation.pointAt[0], operation.pointAt[1], operation.pointAt[2]);
        }
        return util.vector().topOf((int) Math.round(operation.pointAt[0]), (int) Math.round(operation.pointAt[1]),
            (int) Math.round(operation.pointAt[2]));
    }

    private static Vec3d toVec(double[] values) {
        if (values == null || values.length < 3) {
            return null;
        }
        return new Vec3d(values[0], values[1], values[2]);
    }

    private static Vec3d resolveOverlayAnchor(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.pointAt != null) {
            return toVec(util, operation);
        }

        if (operation.pos != null) {
            return util.vector().centerOf(operation.pos[0], operation.pos[1], operation.pos[2]);
        }

        if (operation.from != null && operation.to != null) {
            int minX = Math.min(operation.from[0], operation.to[0]);
            int maxX = Math.max(operation.from[0], operation.to[0]);
            int minZ = Math.min(operation.from[2], operation.to[2]);
            int maxZ = Math.max(operation.from[2], operation.to[2]);
            int minY = Math.min(operation.from[1], operation.to[1]);
            int maxY = Math.max(operation.from[1], operation.to[1]);
            int midX = (minX + maxX) / 2;
            int midY = (minY + maxY) / 2;
            int midZ = (minZ + maxZ) / 2;
            return util.vector().centerOf(midX, midY, midZ);
        }

        return null;
    }

    private static Vec3d resolveRotationPivot(SceneBuildingUtil util, SceneOperation operation) {
        Vec3d explicit = toVec(operation.pivot);
        if (explicit != null) {
            return explicit;
        }

        if (operation.pos != null) {
            return util.vector().centerOf(operation.pos[0], operation.pos[1], operation.pos[2]);
        }

        if (operation.from != null && operation.to != null) {
            int minX = Math.min(operation.from[0], operation.to[0]);
            int maxX = Math.max(operation.from[0], operation.to[0]);
            int minY = Math.min(operation.from[1], operation.to[1]);
            int maxY = Math.max(operation.from[1], operation.to[1]);
            int minZ = Math.min(operation.from[2], operation.to[2]);
            int maxZ = Math.max(operation.from[2], operation.to[2]);
            return new Vec3d(
                (minX + maxX + 1) / 2.0D,
                (minY + maxY + 1) / 2.0D,
                (minZ + maxZ + 1) / 2.0D);
        }

        return new Vec3d(0.5D, 0.5D, 0.5D);
    }

    private static Vec3d resolveRotation(SceneOperation operation) {
        Vec3d vector = toVec(operation.rotation);
        if (vector != null) {
            return vector;
        }

        double x = operation.rotX;
        double y = operation.rotY;
        double z = operation.rotZ;
        if (Math.abs(x) >= 1.0E-4D || Math.abs(y) >= 1.0E-4D || Math.abs(z) >= 1.0E-4D) {
            return new Vec3d(x, y, z);
        }

        if (Math.abs(operation.degrees) >= 1.0E-4F) {
            return new Vec3d(0.0D, operation.degrees, 0.0D);
        }

        return null;
    }

    private static IBlockState parseBlockState(String blockId, int meta) {
        if (blockId == null || blockId.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing block state id");
        }

        Block block = Block.REGISTRY.getObject(new ResourceLocation(blockId));
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockId);
        }

        if (meta < 0) {
            return block.getDefaultState();
        }

        try {
            return block.getStateFromMeta(meta);
        } catch (RuntimeException ignored) {
            return block.getDefaultState();
        }
    }

    private static String readNbtPayload(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        String nbtRaw;
        if (element.isJsonObject() || element.isJsonArray()) {
            nbtRaw = element.toString();
        } else if (element.isJsonPrimitive()) {
            nbtRaw = element.getAsString();
        } else {
            return null;
        }

        if (nbtRaw.trim().isEmpty()) {
            return null;
        }

        try {
            NBTTagCompound unused = JsonToNBT.getTagFromJson(nbtRaw);
            return nbtRaw;
        } catch (NBTException exception) {
            Ponder.LOGGER.warn("Invalid NBT payload in external ponder scene json: {}", nbtRaw, exception);
            return null;
        }
    }

    private static NBTTagCompound parseMatcherNbt(String rawNbt) {
        if (rawNbt == null || rawNbt.trim().isEmpty()) {
            return null;
        }

        try {
            return JsonToNBT.getTagFromJson(rawNbt);
        } catch (NBTException exception) {
            throw new IllegalArgumentException("Invalid componentNbt payload: " + rawNbt, exception);
        }
    }

    private static ResourceLocation buildComponentId(ResourceLocation componentItem, String explicitComponentKey,
        int componentMeta, String componentNbt) {
        if (explicitComponentKey != null && !explicitComponentKey.trim().isEmpty()) {
            return parseLocation(explicitComponentKey, componentItem.getNamespace());
        }

        if (componentMeta < 0 && (componentNbt == null || componentNbt.trim().isEmpty())) {
            return componentItem;
        }

        StringBuilder path = new StringBuilder(componentItem.getPath());
        if (componentMeta >= 0) {
            path.append("__m").append(componentMeta);
        }
        if (componentNbt != null && !componentNbt.trim().isEmpty()) {
            path.append("__n").append(Integer.toHexString(componentNbt.hashCode()));
        }
        return new ResourceLocation(componentItem.getNamespace(), path.toString());
    }

    private static void enqueueNbtWorldEvent(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation,
        final String operationName) {
        if (operation.blockNbt == null || operation.blockNbt.trim().isEmpty()) {
            return;
        }

        final String nbtPayload = operation.blockNbt;
        final List<BlockPos> targets = collectTargetPositions(util, operation);
        if (targets.isEmpty()) {
            return;
        }

        final String message = "external." + operationName + ".nbt.applied(" + nbtPayload + ")";
        scene.debug().enqueueCallback(new Consumer<net.createmod.ponder.foundation.PonderScene>() {
            @Override
            public void accept(net.createmod.ponder.foundation.PonderScene ponderScene) {
                ponderScene.recordOperation(message);
                ponderScene.recordWorldEvent(net.createmod.ponder.foundation.PonderScene.WorldEventType.APPLY_NBT,
                    targets, nbtPayload);
            }
        });
    }

    private static List<BlockPos> collectTargetPositions(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.pos != null) {
            return Collections.singletonList(util.grid().at(operation.pos[0], operation.pos[1], operation.pos[2]));
        }

        if (operation.from == null || operation.to == null) {
            return Collections.emptyList();
        }

        int minX = Math.min(operation.from[0], operation.to[0]);
        int maxX = Math.max(operation.from[0], operation.to[0]);
        int minY = Math.min(operation.from[1], operation.to[1]);
        int maxY = Math.max(operation.from[1], operation.to[1]);
        int minZ = Math.min(operation.from[2], operation.to[2]);
        int maxZ = Math.max(operation.from[2], operation.to[2]);
        List<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(util.grid().at(x, y, z));
                }
            }
        }
        return positions;
    }

    private static PonderPalette parsePalette(String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            return PonderPalette.WHITE;
        }

        try {
            return PonderPalette.valueOf(colorName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return PonderPalette.WHITE;
        }
    }

    private static EnumFacing parseFacing(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return EnumFacing.UP;
        }
        try {
            return EnumFacing.valueOf(direction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return EnumFacing.UP;
        }
    }

    private static List<ResourceLocation> parseLocations(JsonElement element, String defaultNamespace) {
        if (element == null || element.isJsonNull()) {
            return Collections.emptyList();
        }

        List<ResourceLocation> locations = new ArrayList<ResourceLocation>();
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.isJsonPrimitive()) {
                    locations.add(parseLocation(arrayElement.getAsString(), defaultNamespace));
                }
            }
        } else if (element.isJsonPrimitive()) {
            locations.add(parseLocation(element.getAsString(), defaultNamespace));
        }
        return locations;
    }

    private static List<String> parseStrings(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<String>();
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.isJsonPrimitive()) {
                    strings.add(arrayElement.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            strings.add(element.getAsString());
        }
        return strings;
    }

    private static int[] parseIntArray(JsonElement element, int expectedSize) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        if (array.size() < expectedSize) {
            return null;
        }

        int[] values = new int[expectedSize];
        for (int i = 0; i < expectedSize; i++) {
            values[i] = array.get(i).getAsInt();
        }
        return values;
    }

    private static double[] parseDoubleArray(JsonElement element, int expectedSize) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        if (array.size() < expectedSize) {
            return null;
        }

        double[] values = new double[expectedSize];
        for (int i = 0; i < expectedSize; i++) {
            values[i] = array.get(i).getAsDouble();
        }
        return values;
    }

    private static ResourceLocation parseLocation(String value, String defaultNamespace) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource location cannot be empty");
        }

        String trimmed = value.trim();
        if (trimmed.indexOf(':') >= 0) {
            return new ResourceLocation(trimmed);
        }
        return new ResourceLocation(defaultNamespace, trimmed);
    }

    private static ResourceLocation parseTextureLocation(String value, String defaultNamespace) {
        ResourceLocation location = parseLocation(value, defaultNamespace);
        String path = location.getPath();
        if (!path.startsWith("textures/")) {
            path = "textures/" + path;
        }
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return new ResourceLocation(location.getNamespace(), path);
    }

    private static String normalizeSceneId(String sceneId, String fallback) {
        if (sceneId == null || sceneId.trim().isEmpty()) {
            return fallback;
        }

        String trimmed = sceneId.trim();
        int separatorIndex = trimmed.indexOf(':');
        return separatorIndex >= 0 ? trimmed.substring(separatorIndex + 1) : trimmed;
    }

    private static String stripJsonExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private static String readRequiredString(JsonObject object, String key) {
        String value = readOptionalString(object, key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required json string field '" + key + "'");
        }
        return value;
    }

    private static String readRequiredString(JsonObject object, JsonObject fallback, String key) {
        String value = readOptionalString(object, fallback, key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required json string field '" + key + "'");
        }
        return value;
    }

    private static String readOptionalString(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }

    private static String readOptionalString(JsonObject object, JsonObject fallback, String key) {
        String primary = readOptionalString(object, key);
        if (primary != null) {
            return primary;
        }
        return fallback == null ? null : readOptionalString(fallback, key);
    }

    private static String readString(JsonObject object, String key, String fallback) {
        String value = readOptionalString(object, key);
        return value == null ? fallback : value;
    }

    private static JsonElement getElement(JsonObject object, JsonObject fallback, String key) {
        if (hasField(object, key)) {
            return object.get(key);
        }
        return hasField(fallback, key) ? fallback.get(key) : null;
    }

    private static boolean hasField(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull();
    }

    private static JsonElement firstPresent(JsonObject object, String primaryKey, String secondaryKey) {
        if (hasField(object, primaryKey)) {
            return object.get(primaryKey);
        }
        return hasField(object, secondaryKey) ? object.get(secondaryKey) : null;
    }

    private static int readInt(JsonObject object, String key, int fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsInt() : fallback;
    }

    private static float readFloat(JsonObject object, String key, float fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsFloat() : fallback;
    }

    private static boolean readBoolean(JsonObject object, String key, boolean fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsBoolean() : fallback;
    }

    private static File resolveQueuedPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }

        File direct = new File(rawPath);
        if (direct.exists()) {
            return direct;
        }

        File gameDir = getGameDir();
        if (gameDir == null) {
            return null;
        }

        File[] candidates = new File[] {
            new File(gameDir, rawPath),
            new File(new File(gameDir, "scripts"), rawPath),
            new File(new File(gameDir, "scripts/ponder"), rawPath),
            new File(new File(gameDir, "config/ponder"), rawPath),
            new File(new File(gameDir, "ponder"), rawPath)
        };

        for (File candidate : candidates) {
            if (candidate.exists()) {
                return candidate;
            }
        }

        return null;
    }

    private static File getGameDir() {
        File configDir = Loader.instance().getConfigDir();
        return configDir == null ? null : configDir.getParentFile();
    }

    private static final class ExternalDefinitionSet {
        private final List<TagDefinition> tags = new ArrayList<TagDefinition>();
        private final List<SceneDefinition> scenes = new ArrayList<SceneDefinition>();
        private final Map<ResourceLocation, InteractionDefinition> interactions =
            new LinkedHashMap<ResourceLocation, InteractionDefinition>();
    }

    private static final class TagDefinition {
        private String source;
        private String namespace;
        private ResourceLocation id;
        private String title;
        private String description;
        private String icon;
        private ItemStack itemStack = ItemStack.EMPTY;
        private boolean useItemAsIcon = true;
        private boolean useItemAsMainItem = true;
        private boolean addToIndex = true;
    }

    private static final class ComponentDefinition {
        private ResourceLocation componentId;
        private ResourceLocation componentItem;
        private int componentMeta = -1;
        private String componentNbt;
        private String componentDisplayNbt;
        private PonderComponentMatcher componentMatcher;
    }

    private static final class SceneDefinition {
        private String source;
        private String namespace;
        private List<ComponentDefinition> components = Collections.emptyList();
        private String schematic;
        private String sceneId;
        private String title;
        private List<ResourceLocation> tags = Collections.emptyList();
        private List<ResourceLocation> componentTags = Collections.emptyList();
        private List<String> orderBefore = Collections.emptyList();
        private List<String> orderAfter = Collections.emptyList();
        private final List<SceneOperation> operations = new ArrayList<SceneOperation>();
    }

    private static final class InteractionDefinition {
        private String source;
        private String namespace;
        private ResourceLocation id;
        private JsonObject defaults = new JsonObject();
        private final List<JsonObject> steps = new ArrayList<JsonObject>();
    }

    private static final class SceneOperation {
        private String type;
        private int xOffset;
        private int zOffset;
        private int size;
        private int duration;
        private int ticks;
        private int times;
        private int independentY = Integer.MIN_VALUE;
        private float factor;
        private float degrees;
        private float offsetY;
        private boolean enabled = true;
        private String text;
        private String color;
        private String overlayId;
        private String parentOverlayId;
        private String parentGuiId;
        private String blockGui;
        private String blockState;
        private int blockMeta = -1;
        private String blockNbt;
        private String direction;
        private int captionX = Integer.MIN_VALUE;
        private int captionY = Integer.MIN_VALUE;
        private int captionOffsetX;
        private int captionOffsetY;
        private boolean connectorVisible = true;
        private boolean placeNearTarget;
        private String pointMode;
        private String snapshot;
        private String texture;
        private int textureU;
        private int textureV;
        private int regionWidth = -1;
        private int regionHeight = -1;
        private int displayWidth = -1;
        private int displayHeight = -1;
        private int textureWidth = 256;
        private int textureHeight = 256;
        private int offsetX;
        private boolean framed = true;
        private boolean scaleToParent;
        private boolean stretchTexture;
        private int stretchBorder = 4;
        private int guiX;
        private int guiY;
        private int guiWidth = -1;
        private int guiHeight = -1;
        private int[] pos;
        private int[] from;
        private int[] to;
        private double[] offset;
        private double[] rotation;
        private double[] pivot;
        private double[] pointAt;
        private float rotX;
        private float rotY;
        private float rotZ;
        private List<SceneOperation> childOperations = Collections.emptyList();
    }
}
