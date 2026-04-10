package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.createmod.ponder.PonderBackportMod;
import net.minecraft.client.gui.GuiScreen;

public final class JeiScreenCompat {

    private static final Class<?>[] PONDER_SCREEN_TYPES = {
        PonderDebugScreen.class,
        PonderUI.class,
        PonderIndexScreen.class,
        PonderTagScreen.class
    };

    private static boolean installed;
    private static boolean unavailable;
    private static boolean overlaySuppressedByPonder;
    private static boolean configButtonSuppressedByPonder;
    private static boolean bookmarkButtonSuppressedByPonder;

    private JeiScreenCompat() {
    }

    public static void tryInstall() {
        if (installed || unavailable) {
            return;
        }

        try {
            Class<?> internalClass = Class.forName("mezz.jei.Internal");
            Method getRuntimeMethod = internalClass.getMethod("getRuntime");
            Object runtime = getRuntimeMethod.invoke(null);
            if (runtime == null) {
                return;
            }

            Method getIngredientListOverlayMethod = runtime.getClass().getMethod("getIngredientListOverlay");
            Object ingredientListOverlay = getIngredientListOverlayMethod.invoke(runtime);
            if (ingredientListOverlay == null) {
                return;
            }

            Object guiScreenHelper = getFieldValue(ingredientListOverlay, "guiScreenHelper");
            Object guiScreenHandlersObject = guiScreenHelper == null ? null : getFieldValue(guiScreenHelper,
                "guiScreenHandlers");
            if (!(guiScreenHandlersObject instanceof Map)) {
                unavailable = true;
                return;
            }

            Class<?> guiScreenHandlerClass = Class.forName("mezz.jei.api.gui.IGuiScreenHandler");
            Class<?> guiPropertiesClass = Class.forName("mezz.jei.api.gui.IGuiProperties");
            Object handlerProxy = createGuiScreenHandlerProxy(guiScreenHandlerClass, guiPropertiesClass);

            @SuppressWarnings("unchecked")
            Map<Class<?>, Object> guiScreenHandlers = (Map<Class<?>, Object>) guiScreenHandlersObject;
            for (Class<?> screenType : PONDER_SCREEN_TYPES) {
                guiScreenHandlers.put(screenType, handlerProxy);
            }

            invokeNoArgIfPresent(ingredientListOverlay, "invalidateBuffer");
            invokeBooleanIfPresent(ingredientListOverlay, "updateLayout", true);

            installed = true;
            PonderBackportMod.LOGGER.info("Registered HEI compatibility handlers for Ponder screens");
        } catch (ClassNotFoundException ignored) {
            unavailable = true;
        } catch (Throwable throwable) {
            unavailable = true;
            PonderBackportMod.LOGGER.warn("Failed to install HEI compatibility handlers for Ponder screens",
                throwable);
        }
    }

    public static void syncOverlaySuppression(GuiScreen screen) {
        if (unavailable) {
            return;
        }

        try {
            boolean shouldSuppress = isPonderScreen(screen);
            Boolean overlayEnabled = isOverlayEnabled();
            if (overlayEnabled == null) {
                return;
            }

            if (shouldSuppress) {
                suppressButton("hideBottomRightCornerConfigButton", true);
                suppressButton("hideBottomLeftCornerBookmarkButton", false);
                if (overlayEnabled.booleanValue()) {
                    toggleOverlayEnabled();
                    overlaySuppressedByPonder = true;
                }
                return;
            }

            if (overlaySuppressedByPonder && !overlayEnabled.booleanValue()) {
                toggleOverlayEnabled();
            }
            overlaySuppressedByPonder = false;
            restoreButton("hideBottomRightCornerConfigButton", true);
            restoreButton("hideBottomLeftCornerBookmarkButton", false);
        } catch (ClassNotFoundException ignored) {
            unavailable = true;
        } catch (Throwable throwable) {
            unavailable = true;
            PonderBackportMod.LOGGER.warn("Failed to synchronize HEI overlay suppression for Ponder screens",
                throwable);
        }
    }

    private static Object createGuiScreenHandlerProxy(Class<?> guiScreenHandlerClass, Class<?> guiPropertiesClass) {
        InvocationHandler handler = new JeiGuiScreenHandlerInvocation(guiPropertiesClass);
        return Proxy.newProxyInstance(guiScreenHandlerClass.getClassLoader(), new Class<?>[] { guiScreenHandlerClass },
            handler);
    }

    static Object createGuiPropertiesProxy(Class<?> guiPropertiesClass, GuiScreen screen) {
        Class<? extends GuiScreen> guiClass = screen.getClass();
        int screenWidth = screen instanceof CompatGuiScreen ? Math.max(1, ((CompatGuiScreen) screen).width)
            : Math.max(1, resolveIntField(screen, "width"));
        int screenHeight = screen instanceof CompatGuiScreen ? Math.max(1, ((CompatGuiScreen) screen).height)
            : Math.max(1, resolveIntField(screen, "height"));
        InvocationHandler handler = new JeiGuiPropertiesInvocation(guiClass, screenWidth, screenHeight);

        return Proxy.newProxyInstance(guiPropertiesClass.getClassLoader(), new Class<?>[] { guiPropertiesClass },
            handler);
    }

    private static int resolveIntField(Object target, String fieldName) {
        Object value = getFieldValue(target, fieldName);
        return value instanceof Integer ? ((Integer) value).intValue() : 0;
    }

    private static boolean isPonderScreen(GuiScreen screen) {
        if (screen == null) {
            return false;
        }
        for (Class<?> screenType : PONDER_SCREEN_TYPES) {
            if (screenType.isInstance(screen)) {
                return true;
            }
        }
        return false;
    }

    private static Boolean isOverlayEnabled() throws Exception {
        Class<?> configClass = Class.forName("mezz.jei.config.Config");
        Method method = configClass.getMethod("isOverlayEnabled");
        Object value = method.invoke(null);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static void toggleOverlayEnabled() throws Exception {
        Class<?> configClass = Class.forName("mezz.jei.config.Config");
        Method method = configClass.getMethod("toggleOverlayEnabled");
        method.invoke(null);
    }

    private static void suppressButton(String fieldName, boolean configButton) throws Exception {
        Boolean currentValue = getConfigBoolean(fieldName);
        if (currentValue == null || currentValue.booleanValue()) {
            return;
        }

        setConfigBoolean(fieldName, true);
        if (configButton) {
            configButtonSuppressedByPonder = true;
        } else {
            bookmarkButtonSuppressedByPonder = true;
        }
    }

    private static void restoreButton(String fieldName, boolean configButton) throws Exception {
        boolean suppressedByPonder = configButton ? configButtonSuppressedByPonder : bookmarkButtonSuppressedByPonder;
        if (!suppressedByPonder) {
            return;
        }

        setConfigBoolean(fieldName, false);
        if (configButton) {
            configButtonSuppressedByPonder = false;
        } else {
            bookmarkButtonSuppressedByPonder = false;
        }
    }

    private static Boolean getConfigBoolean(String fieldName) throws Exception {
        Object values = getConfigValues();
        if (values == null) {
            return null;
        }
        Object value = getFieldValue(values, fieldName);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static void setConfigBoolean(String fieldName, boolean value) throws Exception {
        Object values = getConfigValues();
        if (values == null) {
            return;
        }
        setFieldValue(values, fieldName, Boolean.valueOf(value));
    }

    private static Object getConfigValues() throws Exception {
        Class<?> configClass = Class.forName("mezz.jei.config.Config");
        Field valuesField = configClass.getDeclaredField("values");
        valuesField.setAccessible(true);
        return valuesField.get(null);
    }

    private static Object getFieldValue(Object target, String fieldName) {
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

    private static void setFieldValue(Object target, String fieldName, Object value) {
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

    private static void invokeNoArgIfPresent(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.invoke(target);
        } catch (Throwable ignored) {
        }
    }

    private static void invokeBooleanIfPresent(Object target, String methodName, boolean value) {
        try {
            Method method = target.getClass().getMethod(methodName, boolean.class);
            method.invoke(target, Boolean.valueOf(value));
        } catch (Throwable ignored) {
        }
    }

}
