package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;

public record SnapshotContext(float currentTick, @Nullable EntityPlayerSP player, @Nullable World world) {

    public static SnapshotContext of(float currentTick) {
        return new SnapshotContext(currentTick, null, null);
    }

    public int tickBucket20() {
        return Math.max(0, (int) (currentTick / 20.0F));
    }

    public int dimension() {
        return world == null || world.provider == null ? 0 : world.provider.getDimension();
    }

}
