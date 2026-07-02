package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.List;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;

public final class ActorOverlayBuilder {

    private ActorOverlayBuilder() {
    }

    public static List<ActorOverlayItem> buildItems(
        PonderScene scene,
        PreviewBounds bounds,
        PreviewLayout layout,
        float currentTick,
        List<PonderSceneRuntime.ActorRuntimeState> actors,
        ScenePointProjector projector
    ) {
        List<ActorOverlayItem> items = new ArrayList<ActorOverlayItem>(actors.size());
        for (PonderSceneRuntime.ActorRuntimeState actor : actors) {
            if (!actor.visible || actor.fade <= 0.0F) {
                continue;
            }

            SpeechRenderer.Point target = projector.project(scene, bounds, layout, currentTick, actor.position);
            if (target == null) {
                continue;
            }

            items.add(new ActorOverlayItem(
                target.x, target.y,
                actor.kind,
                actor.itemStack,
                actor.fade,
                getActorBaseColor(actor),
                getActorLabel(actor),
                actor.kind == PonderScene.ActorKind.CART ? actor.cartYaw : actor.rotation.y
            ));
        }
        return items;
    }

    private static int getActorBaseColor(PonderSceneRuntime.ActorRuntimeState actor) {
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

    private static String getActorLabel(PonderSceneRuntime.ActorRuntimeState actor) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return "CART";
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return actor.displayName == null || actor.displayName.isEmpty() ? "ITEM" : actor.displayName;
        }
        if (isBirbDancePose(actor)) {
            return "BIRB Dance";
        }
        if (isBirbCursorPose(actor)) {
            return "BIRB Cursor";
        }
        if (isBirbPoiPose(actor)) {
            return "BIRB POI";
        }
        return actor.poseName == null || actor.poseName.isEmpty() ? "BIRB" : "BIRB " + actor.poseName;
    }

    private static boolean isBirbDancePose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("DancePose");
    }

    private static boolean isBirbCursorPose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FaceCursorPose");
    }

    private static boolean isBirbPoiPose(PonderSceneRuntime.ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FacePointOfInterestPose");
    }
}
