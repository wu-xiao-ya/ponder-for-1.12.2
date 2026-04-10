package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;

final class ThermalMachinePreviewHelper {

    private static final int RESONANT_LEVEL = 4;

    private ThermalMachinePreviewHelper() {
    }

    static void prepare(TileEntity tile) {
        if (tile == null) {
            return;
        }
        if (!tile.getClass().getName().startsWith("cofh.thermalexpansion.block.machine.")) {
            return;
        }

        if (!invokeLevelMethod(tile, "setLevel", RESONANT_LEVEL)) {
            setLevelField(tile, RESONANT_LEVEL);
            invokeNoArg(tile, "setLevelFlags");
        }
    }

    private static boolean invokeLevelMethod(Object target, String methodName, int level) {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, int.class);
                method.setAccessible(true);
                method.invoke(target, Integer.valueOf(level));
                return true;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private static void setLevelField(Object target, int level) {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField("level");
                field.setAccessible(true);
                field.set(target, Byte.valueOf((byte) level));
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    private static void invokeNoArg(Object target, String methodName) {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(target);
                return;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return;
            }
        }
    }
}
