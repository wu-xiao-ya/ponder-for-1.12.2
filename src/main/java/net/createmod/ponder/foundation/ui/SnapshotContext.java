package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;

public record SnapshotContext(float currentTick, @Nullable EntityPlayerSP player, @Nullable World world) {

    public static SnapshotContext of(float currentTick) {
        return new SnapshotContext(currentTick, null, null);
    }

    public static SnapshotContext of(float currentTick, @Nullable EntityPlayerSP player, @Nullable World world) {
        return new SnapshotContext(currentTick, player, world);
    }

    public int tickBucket20() {
        return Math.max(0, (int) (currentTick / 20.0F));
    }

    public int dimension() {
        return world == null || world.provider == null ? 0 : world.provider.getDimension();
    }

    public String cacheScopeKey() {
        StringBuilder builder = new StringBuilder(64);
        builder.append(dimension()).append('|');
        builder.append(player == null ? "-" : player.getUniqueID());
        builder.append('|');
        builder.append(world == null ? "-" : Integer.toHexString(System.identityHashCode(world)));
        return builder.toString();
    }

}
