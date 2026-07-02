package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.item.ItemStack;

public final class ActorOverlayItem {

    public final int targetX;
    public final int targetY;
    public final PonderScene.ActorKind kind;
    public final ItemStack itemStack;
    public final float actorFade;
    public final int baseColor;
    public final String label;
    public final double yawDegrees;

    public ActorOverlayItem(int targetX, int targetY, PonderScene.ActorKind kind,
        @Nullable ItemStack itemStack, float actorFade, int baseColor, String label, double yawDegrees) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.kind = kind;
        this.itemStack = itemStack;
        this.actorFade = actorFade;
        this.baseColor = baseColor;
        this.label = label;
        this.yawDegrees = yawDegrees;
    }
}