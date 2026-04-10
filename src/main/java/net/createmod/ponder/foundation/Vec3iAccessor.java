package net.createmod.ponder.foundation;

import java.lang.reflect.Method;

import net.minecraft.util.math.Vec3i;

public final class Vec3iAccessor {

    private static final Method GET_X = resolve("getX", "func_177958_n");
    private static final Method GET_Y = resolve("getY", "func_177956_o");
    private static final Method GET_Z = resolve("getZ", "func_177952_p");

    private Vec3iAccessor() {
    }

    public static int x(Vec3i vec) {
        return invoke(GET_X, vec, "x");
    }

    public static int y(Vec3i vec) {
        return invoke(GET_Y, vec, "y");
    }

    public static int z(Vec3i vec) {
        return invoke(GET_Z, vec, "z");
    }

    private static Method resolve(String deobfName, String srgName) {
        try {
            return Vec3i.class.getMethod(deobfName);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            return Vec3i.class.getMethod(srgName);
        } catch (NoSuchMethodException ignored) {
        }

        throw new IllegalStateException("Unable to resolve Vec3i accessor " + deobfName + "/" + srgName);
    }

    private static int invoke(Method method, Vec3i vec, String axis) {
        if (vec == null) {
            throw new NullPointerException("Vec3i was null while reading " + axis);
        }

        try {
            return ((Integer) method.invoke(vec)).intValue();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read Vec3i " + axis + " coordinate", exception);
        }
    }
}
