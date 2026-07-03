package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class SnapshotGuiReflectionHelper {

    private SnapshotGuiReflectionHelper() {
    }

    static void setPossibleField(Object target, String fieldName, Object value) {
        if (target == null) {
            return;
        }
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    static Object getFieldValue(Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    static int getIntField(Object target, String primaryField, String secondaryField) {
        Object primary = getFieldValue(target, primaryField);
        if (primary instanceof Integer integer) {
            return integer.intValue();
        }
        Object secondary = getFieldValue(target, secondaryField);
        return secondary instanceof Integer integer ? integer.intValue() : 0;
    }

    static void invokeNoArg(Object target, String methodName) {
        if (target == null) {
            return;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            method.invoke(target);
        } catch (Throwable ignored) {
        }
    }

    static void invokeBoolean(Object target, String methodName, boolean value) {
        if (target == null) {
            return;
        }
        try {
            Method method = target.getClass().getMethod(methodName, boolean.class);
            method.invoke(target, Boolean.valueOf(value));
        } catch (Throwable ignored) {
        }
    }
}
