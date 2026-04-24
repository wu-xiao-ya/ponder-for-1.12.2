package net.createmod.ponder.client;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.CommonProxy;
import net.createmod.ponder.PonderBackportMod;
import net.createmod.ponder.command.PonderCommand;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.JeiScreenCompat;
import net.createmod.ponder.foundation.ui.PonderDebugScreen;
import net.createmod.ponder.foundation.ui.PonderIndexScreen;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    private static final String KEY_CATEGORY = "key.categories.ponder";
    private static KeyBinding openPonderUiKey;
    private static ResourceLocation pendingComponentId;
    private static int pendingSceneIndex;
    private static boolean pendingDebugScreen;
    private static boolean pendingIndexScreen;
    private static boolean openViewerQueued;
    private static boolean tooltipHandlerFailed;
    private static ItemStack hoveredStack = ItemStack.EMPTY;
    private static ItemStack trackedStack = ItemStack.EMPTY;
    private static ResourceLocation trackedComponentId;
    private static float holdProgress;
    private static long lastFrameTimeMs = -1L;

    @Override
    public void init() {
        JeiScreenCompat.tryInstall();
        registerClientCommand(new PonderCommand());
        registerClientCommand(new PonderClientCommand());
        registerClientCommand(new PonderDebugClientCommand());
        if (openPonderUiKey == null) {
            openPonderUiKey = new KeyBinding("key.ponder.open_ui",
                KeyConflictContext.IN_GAME, Keyboard.KEY_P, KEY_CATEGORY);
            ClientRegistry.registerKeyBinding(openPonderUiKey);
        }
        MinecraftForge.EVENT_BUS.register(this);
        PonderBackportMod.LOGGER.info("Registered Ponder client commands and Ponder UI key binding");
    }

    private static void registerClientCommand(ICommand command) {
        if (command == null) {
            return;
        }

        ClientCommandHandler handler = ClientCommandHandler.instance;
        if (handler == null) {
            throw new IllegalStateException("ClientCommandHandler.instance is null while registering " + command.getName());
        }

        if (invokeCommandRegistration(handler, "registerCommand", command)) {
            return;
        }
        if (invokeCommandRegistration(handler, "func_71560_a", command)) {
            return;
        }

        throw new IllegalStateException("Unable to register client command " + command.getName());
    }

    private static boolean invokeCommandRegistration(ClientCommandHandler handler, String methodName, ICommand command) {
        Class<?> current = handler.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, ICommand.class);
                method.setAccessible(true);
                method.invoke(handler, command);
                return true;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            } catch (Throwable throwable) {
                PonderBackportMod.LOGGER.debug("Failed to invoke client command registration method {}", methodName,
                    throwable);
                return false;
            }
        }
        return false;
    }

    public static void queueOpenDebugScreen(@Nullable ResourceLocation componentId, int sceneIndex) {
        queueOpenScreen(componentId, sceneIndex, true);
    }

    public static void queueOpenShowcaseScreen(@Nullable ResourceLocation componentId, int sceneIndex) {
        queueOpenScreen(componentId, sceneIndex, false);
    }

    public static void queueOpenIndexScreen() {
        pendingComponentId = null;
        pendingSceneIndex = 0;
        pendingDebugScreen = false;
        pendingIndexScreen = true;
        openViewerQueued = true;
    }

    private static void queueOpenScreen(@Nullable ResourceLocation componentId, int sceneIndex, boolean debugScreen) {
        pendingComponentId = componentId;
        pendingSceneIndex = Math.max(0, sceneIndex);
        pendingDebugScreen = debugScreen;
        pendingIndexScreen = false;
        openViewerQueued = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        JeiScreenCompat.tryInstall();
        Minecraft minecraft = Minecraft.getMinecraft();
        JeiScreenCompat.syncOverlaySuppression(minecraft == null ? null : minecraft.currentScreen);
        net.createmod.ponder.foundation.ui.SandboxTriggeredBlockGuiSnapshot.onClientTickAll();

        if (minecraft.player == null) {
            handleTooltipClientTick(null);
            return;
        }

        handleTooltipClientTick(minecraft.currentScreen);

        if (openPonderUiKey != null && minecraft.currentScreen == null) {
            while (openPonderUiKey.isPressed()) {
                queueOpenIndexScreen();
            }
        }

        if (!openViewerQueued) {
            return;
        }

        openViewerQueued = false;
        if (pendingIndexScreen) {
            minecraft.displayGuiScreen(new PonderIndexScreen());
            return;
        }

        minecraft.displayGuiScreen(pendingDebugScreen
            ? new PonderDebugScreen(pendingComponentId, pendingSceneIndex)
            : PonderUI.showcase(pendingComponentId, pendingSceneIndex));
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        net.createmod.ponder.foundation.ui.SandboxTriggeredBlockGuiSnapshot.onGuiOpenAll(event);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntityPlayer() == null) {
            return;
        }
        handleTooltipAddToTooltip(event);
    }

    @SubscribeEvent
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        handleTooltipDrawPost(event);
    }

    private static void handleTooltipClientTick(@Nullable GuiScreen screen) {
        if (tooltipHandlerFailed) {
            return;
        }

        try {
            if (screen == null) {
                resetTooltipState();
            }
        } catch (Throwable throwable) {
            disableTooltipHandler("client tick", throwable);
        }
    }

    private static void handleTooltipAddToTooltip(ItemTooltipEvent event) {
        if (tooltipHandlerFailed) {
            return;
        }

        try {
            appendPonderTooltip(event.getItemStack(), event.getToolTip());
        } catch (Throwable throwable) {
            disableTooltipHandler("item tooltip", throwable);
        }
    }

    private static void handleTooltipDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (tooltipHandlerFailed) {
            return;
        }

        try {
            updateTooltipHold(event.getGui(), event.getMouseX(), event.getMouseY());
        } catch (Throwable throwable) {
            disableTooltipHandler("gui draw", throwable);
        }
    }

    private static void disableTooltipHandler(String phase, Throwable throwable) {
        if (tooltipHandlerFailed) {
            return;
        }

        tooltipHandlerFailed = true;
        PonderBackportMod.LOGGER.error("Disabled Ponder tooltip handler after failure during {}", phase, throwable);
    }

    private static void appendPonderTooltip(ItemStack stack, List<String> tooltip) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.currentScreen == null || stack.isEmpty()) {
            return;
        }

        ResourceLocation componentId = getPonderComponentId(stack);
        if (componentId == null) {
            return;
        }

        hoveredStack = stack.copy();
        if (!areEquivalentForPonder(trackedStack, stack) || !componentId.equals(trackedComponentId)) {
            trackedStack = stack.copy();
            trackedComponentId = componentId;
            holdProgress = 0.0F;
        }

        int insertIndex = Math.min(1, tooltip.size());
        tooltip.add(insertIndex, buildTooltipLine(minecraft));
    }

    private static void updateTooltipHold(@Nullable GuiScreen screen, int mouseX, int mouseY) {
        long now = Minecraft.getSystemTime();
        if (screen == null) {
            resetTooltipState();
            lastFrameTimeMs = now;
            return;
        }

        float deltaMs = lastFrameTimeMs < 0L ? 50.0F : Math.min(100.0F, now - lastFrameTimeMs);
        lastFrameTimeMs = now;

        Minecraft minecraft = Minecraft.getMinecraft();
        boolean sameHoveredItem = !hoveredStack.isEmpty() && !trackedStack.isEmpty()
            && areEquivalentForPonder(hoveredStack, trackedStack);
        boolean forwardHeld = GameSettings.isKeyDown(minecraft.gameSettings.keyBindForward);

        if (sameHoveredItem && forwardHeld) {
            holdProgress = Math.min(1.0F, holdProgress + deltaMs / 550.0F);
        } else {
            holdProgress = Math.max(0.0F, holdProgress - deltaMs / 180.0F);
        }

        if (holdProgress >= 1.0F && trackedComponentId != null) {
            queueOpenShowcaseScreen(trackedComponentId, 0);
            resetTooltipState();
            lastFrameTimeMs = now;
            return;
        }

        hoveredStack = ItemStack.EMPTY;
    }

    @Nullable
    private static ResourceLocation getPonderComponentId(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : PonderIndex.getSceneAccess().resolveComponentId(stack);
    }

    private static boolean areEquivalentForPonder(ItemStack left, ItemStack right) {
        return !left.isEmpty()
            && !right.isEmpty()
            && ItemStack.areItemsEqual(left, right)
            && ItemStack.areItemStackTagsEqual(left, right);
    }

    private static String buildTooltipLine(Minecraft minecraft) {
        String keyName = GameSettings.getKeyDisplayString(minecraft.gameSettings.keyBindForward.getKeyCode());
        float progress = Math.min(1.0F, holdProgress * 8.0F / 7.0F);
        if (progress > 0.0F) {
            return buildTooltipProgressBar(keyName, progress);
        }
        return TextFormatting.DARK_GRAY + "\u6309\u4F4F " + TextFormatting.GRAY + keyName + TextFormatting.DARK_GRAY
            + " \u5F00\u59CB\u601D\u7D22";
    }

    private static String buildTooltipProgressBar(String keyName, float progress) {
        int totalBars = 10;
        int filledBars = Math.min(totalBars, Math.max(0, Math.round(progress * totalBars)));
        StringBuilder builder = new StringBuilder();
        builder.append(TextFormatting.DARK_GRAY)
            .append("\u6309\u4F4F ")
            .append(TextFormatting.GRAY)
            .append(keyName)
            .append(TextFormatting.DARK_GRAY)
            .append(" \u5F00\u59CB\u601D\u7D22 ")
            .append(TextFormatting.GRAY)
            .append('[');
        for (int i = 0; i < totalBars; i++) {
            builder.append(i < filledBars ? TextFormatting.GRAY : TextFormatting.DARK_GRAY)
                .append('|');
        }
        builder.append(TextFormatting.GRAY).append(']');
        return builder.toString();
    }

    private static void resetTooltipState() {
        hoveredStack = ItemStack.EMPTY;
        trackedStack = ItemStack.EMPTY;
        trackedComponentId = null;
        holdProgress = 0.0F;
        lastFrameTimeMs = -1L;
    }
}
