package net.createmod.ponder.api.scene;

import java.util.function.Predicate;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface Selection extends Iterable<BlockPos>, Predicate<BlockPos> {

    Selection add(Selection other);

    Selection substract(Selection other);

    Selection copy();

    Vec3d getCenter();
}
