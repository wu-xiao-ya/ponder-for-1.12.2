package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.util.math.MathHelper;

final class ActorPreviewAppearance {

    private ActorPreviewAppearance() {
    }

    static ActorPreviewRenderData resolve(PonderSceneRuntime.ActorRuntimeState actor, float currentTick) {
        float alpha = MathHelper.clamp(actor.fade, 0.0F, 1.0F);
        BirbPoseKind birbPoseKind = BirbPoseKind.fromActor(actor);
        int color = getActorBaseColor(actor, birbPoseKind);
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float bobOffset = getActorBobOffset(actor, currentTick, birbPoseKind);
        float yaw = actor.kind == PonderScene.ActorKind.CART ? actor.cartYaw : (float) actor.rotation.y;
        yaw += getActorYawOffset(actor, currentTick, birbPoseKind);
        return new ActorPreviewRenderData(alpha, red, green, blue, bobOffset, yaw, currentTick, birbPoseKind);
    }

    private static int getActorBaseColor(PonderSceneRuntime.ActorRuntimeState actor, BirbPoseKind birbPoseKind) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return 0xD9C27A;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return 0xE0D6A8;
        }
        switch (birbPoseKind) {
            case DANCE:
                return 0xE58ACF;
            case FACE_CURSOR:
                return 0x7FCDE0;
            case FACE_POINT_OF_INTEREST:
                return 0x82D173;
            default:
                return 0xA8D89A;
        }
    }

    private static float getActorBobOffset(PonderSceneRuntime.ActorRuntimeState actor, float currentTick,
        BirbPoseKind birbPoseKind) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return MathHelper.sin(currentTick * 0.15F + actor.actorId) * 0.01F;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return Math.abs(MathHelper.sin(currentTick * 0.22F + actor.actorId * 0.4F)) * 0.05F;
        }
        if (birbPoseKind == BirbPoseKind.DANCE) {
            return Math.abs(MathHelper.sin(currentTick * 0.35F + actor.actorId * 0.5F)) * 0.10F;
        }
        return Math.abs(MathHelper.sin(currentTick * 0.18F + actor.actorId * 0.35F)) * 0.04F;
    }

    private static float getActorYawOffset(PonderSceneRuntime.ActorRuntimeState actor, float currentTick,
        BirbPoseKind birbPoseKind) {
        if (actor.kind != PonderScene.ActorKind.BIRB) {
            return 0.0F;
        }
        if (birbPoseKind == BirbPoseKind.FACE_CURSOR) {
            return MathHelper.sin(currentTick * 0.30F + actor.actorId) * 18.0F;
        }
        if (birbPoseKind == BirbPoseKind.DANCE) {
            return MathHelper.sin(currentTick * 0.45F + actor.actorId) * 10.0F;
        }
        return 0.0F;
    }

    static final class ActorPreviewRenderData {
        final float alpha;
        final float red;
        final float green;
        final float blue;
        final float bobOffset;
        final float yaw;
        final float currentTick;
        final BirbPoseKind birbPoseKind;

        private ActorPreviewRenderData(float alpha, float red, float green, float blue, float bobOffset, float yaw,
            float currentTick, BirbPoseKind birbPoseKind) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.bobOffset = bobOffset;
            this.yaw = yaw;
            this.currentTick = currentTick;
            this.birbPoseKind = birbPoseKind;
        }
    }

    enum BirbPoseKind {
        DANCE,
        FACE_CURSOR,
        FACE_POINT_OF_INTEREST,
        DEFAULT;

        private static BirbPoseKind fromActor(PonderSceneRuntime.ActorRuntimeState actor) {
            if (actor.kind != PonderScene.ActorKind.BIRB) {
                return DEFAULT;
            }
            String poseName = actor.poseName;
            if (poseName == null || poseName.isEmpty()) {
                return DEFAULT;
            }
            if (poseName.contains("DancePose")) {
                return DANCE;
            }
            if (poseName.contains("FaceCursorPose")) {
                return FACE_CURSOR;
            }
            if (poseName.contains("FacePointOfInterestPose")) {
                return FACE_POINT_OF_INTEREST;
            }
            return DEFAULT;
        }
    }
}
