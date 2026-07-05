package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

final class ControlsOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final RenderContext draw;

    ControlsOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, RenderContext draw) {
        this.overlayLayoutHelper = overlayLayoutHelper;
        this.draw = draw;
    }

    void drawControlsOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        SpeechRenderer.Point target =
            overlayLayoutHelper.projectScenePoint(scene, bounds, layout, currentTick, overlayEvent.getPointAt());
        if (target == null) {
            return;
        }

        List<String> actionTokens = new ArrayList<String>();
        if (overlayEvent.isControlLeftClick()) {
            actionTokens.add("LMB");
        }
        if (overlayEvent.isControlRightClick()) {
            actionTokens.add("RMB");
        }
        if (overlayEvent.isControlScroll()) {
            actionTokens.add("SCROLL");
        }

        List<String> modifierTokens = new ArrayList<String>();
        if (overlayEvent.isControlSneaking()) {
            modifierTokens.add("Sneak");
        }
        if (overlayEvent.isControlCtrl()) {
            modifierTokens.add("CTRL");
        }

        Pointing direction = overlayEvent.getControlDirection();
        String directionToken = direction == null ? "INPUT" : getPointingLabel(direction);

        ItemStack stack = overlayEvent.getControlItem();
        int itemWidth = stack.isEmpty() ? 0 : 20;
        int actionsWidth = measureTokenRow(actionTokens);
        int modifiersWidth = measureTokenRow(modifierTokens);
        int directionWidth = measureTokenRow(Collections.singletonList(directionToken));
        int contentWidth = Math.max(Math.max(actionsWidth, modifiersWidth), directionWidth) + itemWidth;
        int boxWidth = Math.max(66, contentWidth + 16);
        int boxHeight = 30 + (modifierTokens.isEmpty() ? 0 : 14);
        int boxX = MathHelper.clamp(target.x - boxWidth / 2, layout.originX + 4,
            layout.originX + layout.width - boxWidth - 4);
        int boxY = target.y - boxHeight - 18;
        SpeechRenderer.SpeechPointing pointing = SpeechRenderer.SpeechPointing.DOWN;
        if (boxY < layout.originY + 4) {
            boxY = target.y + 14;
            pointing = SpeechRenderer.SpeechPointing.UP;
        }

        SpeechRenderer.drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, overlayEvent.getColor(), fade);
        SpeechRenderer.Point tip = SpeechRenderer.getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
        if (tip != null) {
            SpeechRenderer.drawCaptionConnector(tip.x, tip.y, target.x, target.y, overlayEvent.getColor(), fade * 0.9F);
        }

        int rowX = boxX + 7;
        if (!stack.isEmpty()) {
            draw.renderItem(stack, boxX + 6, boxY + 4);
            rowX += 20;
        }

        int topY = boxY + 5;
        drawTokenRow(actionTokens.isEmpty() ? Collections.singletonList("ACT") : actionTokens, rowX, topY,
            overlayEvent.getColor(), fade, true);
        drawTokenRow(Collections.singletonList(directionToken), rowX, topY + 12, overlayEvent.getColor(), fade, false);
        if (!modifierTokens.isEmpty()) {
            drawTokenRow(modifierTokens, rowX, topY + 24, overlayEvent.getColor(), fade, false);
        }
    }

    private int measureTokenRow(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }
        int width = 0;
        for (String token : tokens) {
            width += Math.max(16, getStringWidth(token) + 8);
        }
        width += Math.max(0, tokens.size() - 1) * 4;
        return width;
    }

    private void drawTokenRow(List<String> tokens, int x, int y, int accentColor, float fade, boolean emphasized) {
        int cursor = x;
        for (String token : tokens) {
            int chipWidth = Math.max(16, getStringWidth(token) + 8);
            int fill = withAlpha(emphasized ? blendColors(0x18212B, accentColor, 0.66F) : 0x18212B,
                fade * (emphasized ? 126.0F : 96.0F));
            int edge = withAlpha(blendColors(0xEDE4D4, accentColor, emphasized ? 0.85F : 0.45F),
                fade * (emphasized ? 220.0F : 164.0F));
            draw.drawBorderedRect(cursor, y, cursor + chipWidth, y + 10, fill, edge);
            draw.renderCenteredText(token, cursor + chipWidth / 2, y + 1, 0xF2F5F8);
            cursor += chipWidth + 4;
        }
    }

    private int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    private static int blendColors(int baseColor, int accentColor, float accentWeight) {
        float clampedWeight = MathHelper.clamp(accentWeight, 0.0F, 1.0F);
        float baseWeight = 1.0F - clampedWeight;
        int red = Math.round(((baseColor >> 16) & 0xFF) * baseWeight + ((accentColor >> 16) & 0xFF) * clampedWeight);
        int green = Math.round(((baseColor >> 8) & 0xFF) * baseWeight + ((accentColor >> 8) & 0xFF) * clampedWeight);
        int blue = Math.round((baseColor & 0xFF) * baseWeight + (accentColor & 0xFF) * clampedWeight);
        return red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.min(255, Math.max(0, (int) alpha));
        return (clampedAlpha << 24) | (color & 0x00FFFFFF);
    }

    private String getPointingLabel(Pointing pointing) {
        if (pointing == null) {
            return "INPUT";
        }
        switch (pointing) {
            case UP:
                return "UP";
            case DOWN:
                return "DOWN";
            case LEFT:
                return "LEFT";
            case RIGHT:
                return "RIGHT";
            default:
                return pointing.name();
        }
    }
}
