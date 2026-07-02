package net.createmod.ponder.foundation.ui.render;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

public final class ScissorStack {

    private final Minecraft minecraft;
    private final Deque<Rect> stack = new ArrayDeque<>();

    public ScissorStack(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public AutoCloseable push(int x, int y, int width, int height) {
        Rect rect = stack.isEmpty() ? new Rect(x, y, width, height) : stack.peek().intersect(x, y, width, height);
        stack.push(rect);
        apply(rect);
        return this::pop;
    }

    private void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }
        apply(stack.peek());
    }

    private void apply(Rect rect) {
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(rect.x * scaleFactor,
            minecraft.displayHeight - (rect.y + rect.height) * scaleFactor,
            rect.width * scaleFactor,
            rect.height * scaleFactor);
    }

    private record Rect(int x, int y, int width, int height) {
        Rect intersect(int otherX, int otherY, int otherWidth, int otherHeight) {
            int minX = Math.max(x, otherX);
            int minY = Math.max(y, otherY);
            int maxX = Math.min(x + width, otherX + otherWidth);
            int maxY = Math.min(y + height, otherY + otherHeight);
            return new Rect(minX, minY, Math.max(0, maxX - minX), Math.max(0, maxY - minY));
        }
    }
}
