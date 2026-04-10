package net.createmod.ponder.foundation.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.minecraft.client.gui.GuiScreen;

final class JeiGuiPropertiesInvocation implements InvocationHandler {

    private final Class<? extends GuiScreen> guiClass;
    private final int screenWidth;
    private final int screenHeight;

    JeiGuiPropertiesInvocation(Class<? extends GuiScreen> guiClass, int screenWidth, int screenHeight) {
        this.guiClass = guiClass;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        if ("getGuiClass".equals(methodName)) {
            return guiClass;
        }
        if ("getGuiLeft".equals(methodName) || "getGuiTop".equals(methodName)) {
            return Integer.valueOf(0);
        }
        if ("getGuiXSize".equals(methodName) || "getScreenWidth".equals(methodName)) {
            return Integer.valueOf(screenWidth);
        }
        if ("getGuiYSize".equals(methodName) || "getScreenHeight".equals(methodName)) {
            return Integer.valueOf(screenHeight);
        }
        if ("toString".equals(methodName)) {
            return "PonderJeiGuiProperties[" + guiClass.getSimpleName() + "]";
        }
        if ("hashCode".equals(methodName)) {
            return Integer.valueOf(guiClass.hashCode() * 31 + screenWidth * 13 + screenHeight);
        }
        if ("equals".equals(methodName)) {
            return Boolean.valueOf(proxy == (args == null || args.length == 0 ? null : args[0]));
        }
        return null;
    }
}
