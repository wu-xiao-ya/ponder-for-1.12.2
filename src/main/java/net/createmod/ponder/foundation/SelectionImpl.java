package net.createmod.ponder.foundation;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import net.createmod.ponder.api.scene.Selection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class SelectionImpl {

    private SelectionImpl() {
    }

    public static Selection fromTo(BlockPos first, BlockPos second) {
        return new BoxSelection(first, second);
    }

    private static final class BoxSelection implements Selection {

        private final BlockPos min;
        private final BlockPos max;
        private final Iterable<BlockPos> iterable;

        private BoxSelection(BlockPos first, BlockPos second) {
            this.min = new BlockPos(Math.min(Vec3iAccessor.x(first), Vec3iAccessor.x(second)),
                Math.min(Vec3iAccessor.y(first), Vec3iAccessor.y(second)),
                Math.min(Vec3iAccessor.z(first), Vec3iAccessor.z(second)));
            this.max = new BlockPos(Math.max(Vec3iAccessor.x(first), Vec3iAccessor.x(second)),
                Math.max(Vec3iAccessor.y(first), Vec3iAccessor.y(second)),
                Math.max(Vec3iAccessor.z(first), Vec3iAccessor.z(second)));
            this.iterable = BlockPos.getAllInBox(min, max);
        }

        @Override
        public boolean test(BlockPos pos) {
            return Vec3iAccessor.x(pos) >= Vec3iAccessor.x(min) && Vec3iAccessor.x(pos) <= Vec3iAccessor.x(max)
                && Vec3iAccessor.y(pos) >= Vec3iAccessor.y(min) && Vec3iAccessor.y(pos) <= Vec3iAccessor.y(max)
                && Vec3iAccessor.z(pos) >= Vec3iAccessor.z(min) && Vec3iAccessor.z(pos) <= Vec3iAccessor.z(max);
        }

        @Override
        public Selection add(Selection other) {
            return new CompoundSelection(this).add(other);
        }

        @Override
        public Selection substract(Selection other) {
            return new CompoundSelection(this).substract(other);
        }

        @Override
        public Selection copy() {
            return new BoxSelection(min, max);
        }

        @Override
        public Vec3d getCenter() {
            return new Vec3d((Vec3iAccessor.x(min) + Vec3iAccessor.x(max) + 1) / 2.0D,
                (Vec3iAccessor.y(min) + Vec3iAccessor.y(max) + 1) / 2.0D,
                (Vec3iAccessor.z(min) + Vec3iAccessor.z(max) + 1) / 2.0D);
        }

        @Override
        public Iterator<BlockPos> iterator() {
            return iterable.iterator();
        }

        @Override
        public String toString() {
            if (min.equals(max)) {
                return "Selection[" + min + "]";
            }
            return "Selection[" + min + " -> " + max + "]";
        }
    }

    private static final class CompoundSelection implements Selection {

        private final Set<BlockPos> positions;

        private CompoundSelection(Selection seed) {
            this.positions = new LinkedHashSet<BlockPos>();
            add(seed);
        }

        private CompoundSelection(Set<BlockPos> positions) {
            this.positions = new LinkedHashSet<BlockPos>(positions);
        }

        @Override
        public boolean test(BlockPos pos) {
            return positions.contains(pos);
        }

        @Override
        public Selection add(Selection other) {
            for (BlockPos pos : other) {
                positions.add(new BlockPos(pos));
            }
            return this;
        }

        @Override
        public Selection substract(Selection other) {
            for (BlockPos pos : other) {
                positions.remove(pos);
            }
            return this;
        }

        @Override
        public Selection copy() {
            return new CompoundSelection(positions);
        }

        @Override
        public Vec3d getCenter() {
            if (positions.isEmpty()) {
                return Vec3d.ZERO;
            }

            double totalX = 0.0D;
            double totalY = 0.0D;
            double totalZ = 0.0D;
            for (BlockPos pos : positions) {
                totalX += Vec3iAccessor.x(pos) + 0.5D;
                totalY += Vec3iAccessor.y(pos) + 0.5D;
                totalZ += Vec3iAccessor.z(pos) + 0.5D;
            }
            double size = positions.size();
            return new Vec3d(totalX / size, totalY / size, totalZ / size);
        }

        @Override
        public Iterator<BlockPos> iterator() {
            return positions.iterator();
        }

        @Override
        public String toString() {
            return "Selection[" + positions.size() + " blocks]";
        }
    }
}
