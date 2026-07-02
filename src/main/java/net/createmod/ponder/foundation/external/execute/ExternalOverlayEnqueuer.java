package net.createmod.ponder.foundation.external.execute;

import java.util.List;
import java.util.function.Consumer;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.external.validate.ExternalJsonValidation;
import net.createmod.ponder.foundation.external.validate.ExternalNbtValidation;
import net.createmod.ponder.foundation.ui.PonderGuiSnapshotRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class ExternalOverlayEnqueuer {

    private ExternalOverlayEnqueuer() {
    }

    static void enqueueTextOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation,
        String operationLog) {
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .text(operation.text)
            .palette(ExternalParseHelper.parsePalette(operation.color))
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .captionOffset(operation.captionOffsetX, operation.captionOffsetY)
            .placeNearTarget(operation.placeNearTarget);
        Vec3d anchor = ExternalSelectionHelper.resolveOverlayAnchor(util, operation);
        if (anchor != null) {
            overlayEvent.pointAt(anchor);
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
        if (operation.captionX != Integer.MIN_VALUE || operation.captionY != Integer.MIN_VALUE) {
            overlayEvent.captionPosition(operation.captionX, operation.captionY);
        }
        scene.getScene().recordOperation(operationLog + ".text(\"" + ExternalParseHelper.previewPayload(operation.text) + "\")");
    }

    static void enqueueGuiTextureOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.texture == null || operation.texture.trim().isEmpty()) {
            throw new IllegalArgumentException("gui_texture operation requires a texture");
        }

        int regionWidth = operation.regionWidth > 0 ? operation.regionWidth
            : operation.displayWidth > 0 ? operation.displayWidth : operation.textureWidth;
        int regionHeight = operation.regionHeight > 0 ? operation.regionHeight
            : operation.displayHeight > 0 ? operation.displayHeight : operation.textureHeight;
        int displayWidth = operation.displayWidth > 0 ? operation.displayWidth : regionWidth;
        int displayHeight = operation.displayHeight > 0 ? operation.displayHeight : regionHeight;
        Vec3d anchor = ExternalSelectionHelper.resolveOverlayAnchor(util, operation);
        ResourceLocation textureLocation =
            ExternalJsonValidation.parseTextureLocation(operation.texture, scene.getScene().getNamespace());
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(ExternalParseHelper.parsePalette(operation.color))
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

    static void enqueueGuiSnapshotOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        Vec3d anchor = ExternalSelectionHelper.resolveOverlayAnchor(util, operation);
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(ExternalParseHelper.parsePalette(operation.color))
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .placeNearTarget(operation.placeNearTarget)
            .offset(operation.offsetX, Math.round(operation.offsetY));
        if (operation.snapshot != null && !operation.snapshot.trim().isEmpty()) {
            ResourceLocation snapshotId =
                ExternalJsonValidation.parseLocation(operation.snapshot, scene.getScene().getNamespace());
            overlayEvent.guiSnapshot(snapshotId);
            if (anchor != null) {
                overlayEvent.pointAt(anchor);
            }
            scene.getScene().recordOperation("overlay.showGuiSnapshot(" + snapshotId + ", " + operation.duration + ")");
        } else {
            if (anchor != null) {
                overlayEvent.pointAt(anchor);
            }
            scene.getScene().recordOperation("overlay.showEmpty(" + operation.duration + ")");
        }
        if (operation.independentY != Integer.MIN_VALUE) {
            overlayEvent.independent(operation.independentY);
        }
    }

    static void enqueueBlockGuiOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.blockGui == null || operation.blockGui.trim().isEmpty()) {
            throw new IllegalArgumentException("block_gui operation requires blockGui");
        }
        if (operation.guiWidth <= 0 || operation.guiHeight <= 0) {
            throw new IllegalArgumentException("block_gui operation requires guiWidth and guiHeight");
        }

        ResourceLocation blockId = ExternalJsonValidation.parseLocation(operation.blockGui, "minecraft");
        int meta = Math.max(0, operation.blockMeta);
        NBTTagCompound tileNbt = ExternalNbtValidation.parseTag(operation.blockNbt, "Invalid componentNbt payload");
        ResourceLocation snapshotId = PonderGuiSnapshotRegistry.registerBlockGuiSnapshot(blockId, meta,
            tileNbt, operation.guiWidth, operation.guiHeight);
        Vec3d anchor = ExternalSelectionHelper.resolveOverlayAnchor(util, operation);
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .palette(ExternalParseHelper.parsePalette(operation.color))
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
            + operation.guiWidth + "x" + operation.guiHeight + (tileNbt == null ? "" : ", nbt") + ", "
            + operation.duration + ")");
    }

    static void enqueueGuiHighlightOverlay(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (operation.parentOverlayId == null || operation.parentOverlayId.trim().isEmpty()) {
            throw new IllegalArgumentException("gui_outline_text operation requires a gui id");
        }
        if (operation.guiWidth <= 0 || operation.guiHeight <= 0) {
            throw new IllegalArgumentException("gui_outline_text operation requires guiWidth and guiHeight");
        }
        PonderScene.OverlayEvent overlayEvent = scene.getScene().createOverlayEvent(operation.duration)
            .text(operation.text)
            .palette(ExternalParseHelper.parsePalette(operation.color))
            .guiHighlight(operation.parentOverlayId, operation.guiX, operation.guiY, operation.guiWidth,
                operation.guiHeight)
            .connectorVisible(operation.connectorVisible)
            .overlayId(operation.overlayId)
            .captionOffset(operation.captionOffsetX, operation.captionOffsetY)
            .placeNearTarget(operation.placeNearTarget);
        Vec3d anchor = ExternalSelectionHelper.resolveOverlayAnchor(util, operation);
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
            + ").text(\"" + ExternalParseHelper.previewPayload(operation.text) + "\")");
    }

    static void enqueueNbtWorldEvent(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation,
        final String operationName) {
        if (operation.blockNbt == null || operation.blockNbt.trim().isEmpty()) {
            return;
        }

        final String nbtPayload = operation.blockNbt;
        final List<BlockPos> targets = ExternalSelectionHelper.collectTargetPositions(util, operation);
        if (targets.isEmpty()) {
            return;
        }

        final String message = "external." + operationName + ".nbt.applied(" + ExternalParseHelper.previewPayload(nbtPayload) + ")";
        scene.debug().enqueueCallback(new Consumer<PonderScene>() {
            @Override
            public void accept(PonderScene ponderScene) {
                ponderScene.recordOperation(message);
                ponderScene.recordWorldEvent(PonderScene.WorldEventType.APPLY_NBT, targets, nbtPayload);
            }
        });
    }
}
