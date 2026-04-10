package net.createmod.ponder.foundation.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.minecraft.client.gui.GuiScreen;

final class JeiGuiScreenHandlerInvocation implements InvocationHandler {

    private final Class<?> guiPropertiesClass;

    JeiGuiScreenHandlerInvocation(Class<?> guiPropertiesClass) {
        this.guiPropertiesClass = guiPropertiesClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("apply".equals(methodName) && args != null && args.length == 1 && args[0] instanceof GuiScreen) {
            return JeiScreenCompat.createGuiPropertiesProxy(guiPropertiesClass, (GuiScreen) args[0]);
        }
        if ("toString".equals(methodName)) {
            return "PonderJeiScreenHandler";
        }
        if ("hashCode".equals(methodName)) {
            return Integer.valueOf(System.identityHashCode(proxy));
        }
        if ("equals".equals(methodName)) {
            return Boolean.valueOf(args != null && args.length == 1 && proxy == args[0]);
        }
        return null;
    }
}
