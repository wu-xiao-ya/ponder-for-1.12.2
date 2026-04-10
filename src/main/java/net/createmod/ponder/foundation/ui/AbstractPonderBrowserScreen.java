package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

abstract class AbstractPonderBrowserScreen extends CompatGuiScreen {

    protected static final String UI_LANG_PREFIX = "ponder.ui.";

    protected static final int OUTER_MARGIN = 16;
    protected static final int FOOTER_HEIGHT = 28;
    protected static final int SLOT_SIZE = 22;
    protected static final int SLOT_GAP = 6;
    protected static final int COLOR_TEXT = 0xF3EFE7;
    protected static final int COLOR_TEXT_MUTED = 0xAEB8C1;
    protected static final int COLOR_TEXT_DIM = 0x7E8A93;
    protected static final int COLOR_ACCENT = 0xD5CCB8;
    protected static final int COLOR_PANEL_TOP = 0xCC121820;
    protected static final int COLOR_PANEL_BOTTOM = 0xCC090D12;
    protected static final int COLOR_PANEL_GLOW = 0x221A2631;
    protected static final int COLOR_SLOT_FILL = 0xCC10161D;
    protected static final int COLOR_SLOT_HOVER = 0xDD19212B;
    protected static final int COLOR_SLOT_SELECTED = 0xEE222D38;
    protected static final int COLOR_SLOT_BORDER = 0x88626B74;

    @Nullable
    private final GuiScreen parentScreen;

    protected AbstractPonderBrowserScreen(@Nullable GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Nullable
    protected GuiScreen getParentScreen() {
        return parentScreen;
    }

    protected void closeToParent() {
        mc.displayGuiScreen(parentScreen);
    }

    protected void drawPonderBackground() {
        drawGradientRect(0, 0, width, height, 0xF306090D, 0xFF010205);
        drawGradientRect(0, 0, width, height / 3, 0x181E2B36, 0x00000000);
    }

    protected void drawPanel(int x, int y, int panelWidth, int panelHeight) {
        drawGradientRect(x, y, x + panelWidth, y + panelHeight, COLOR_PANEL_TOP, COLOR_PANEL_BOTTOM);
        drawRect(x, y, x + panelWidth, y + 1, 0x77E7E0D1);
        drawRect(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0x55313A42);
        drawRect(x, y, x + 1, y + panelHeight, 0x66414952);
        drawRect(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0x55222931);
        drawGradientRect(x - 8, y - 8, x + panelWidth + 8, y + panelHeight + 8, COLOR_PANEL_GLOW, 0x00000000);
    }

    protected void drawSectionLabel(String label, int x, int y) {
        drawString(fontRenderer, label, x, y, COLOR_ACCENT);
    }

    protected String tr(String key) {
        return I18n.format(UI_LANG_PREFIX + key);
    }

    protected String tr(String key, Object... args) {
        return I18n.format(UI_LANG_PREFIX + key, args);
    }

    protected void drawHintLine(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        drawCenteredString(fontRenderer, text, width / 2, height - 12, COLOR_TEXT_MUTED);
    }

    protected void drawSlotBackground(int x, int y, int size, boolean hovered, boolean selected) {
        int fill = selected ? COLOR_SLOT_SELECTED : (hovered ? COLOR_SLOT_HOVER : COLOR_SLOT_FILL);
        int border = selected ? 0xDDE7E0D1 : (hovered ? 0xCCB8C3CC : COLOR_SLOT_BORDER);
        drawRect(x, y, x + size, y + size, border);
        drawRect(x + 1, y + 1, x + size - 1, y + size - 1, fill);
    }

    protected void drawRowBackground(int x, int y, int width, int height, boolean hovered, boolean selected) {
        int fill = selected ? COLOR_SLOT_SELECTED : (hovered ? COLOR_SLOT_HOVER : COLOR_SLOT_FILL);
        int border = selected ? 0xDDE7E0D1 : (hovered ? 0xCCB8C3CC : COLOR_SLOT_BORDER);
        drawRect(x, y, x + width, y + height, border);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    protected void drawItemSlot(ItemStack stack, int x, int y, int size, boolean hovered, boolean selected) {
        drawSlotBackground(x, y, size, hovered, selected);
        renderCenteredItem(stack, x, y, size);
    }

    protected void renderCenteredItem(ItemStack stack, int x, int y, int size) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x + (size - 16) / 2, y + (size - 16) / 2);
        mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, stack, x + (size - 16) / 2, y + (size - 16) / 2,
            null);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
    }

    protected void drawTagSlot(PonderTag tag, int x, int y, int size, boolean hovered, boolean selected) {
        drawSlotBackground(x, y, size, hovered, selected);
        ItemStack iconStack = tag.getItemIcon();
        if (!iconStack.isEmpty()) {
            renderCenteredItem(iconStack, x, y, size);
            return;
        }

        ItemStack mainStack = tag.getMainItem();
        if (!mainStack.isEmpty()) {
            renderCenteredItem(mainStack, x, y, size);
            return;
        }

        ResourceLocation texture = tag.getTextureIconLocation();
        if (texture == null) {
            drawCenteredString(fontRenderer, "?", x + size / 2, y + size / 2 - 4, COLOR_TEXT);
            return;
        }

        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawScaledCustomSizeModalRect(x + (size - 16) / 2, y + (size - 16) / 2, 0, 0, 16, 16, 16, 16, 16, 16);
    }

    protected static boolean isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected static int clampScroll(int current, int delta, int max) {
        return MathHelper.clamp(current + delta, 0, Math.max(0, max));
    }

    protected static Map<ResourceLocation, Integer> countScenesByComponent() {
        Map<ResourceLocation, Integer> counts = new LinkedHashMap<ResourceLocation, Integer>();
        for (Map.Entry<ResourceLocation, StoryBoardEntry> entry : PonderIndex.getSceneAccess().getRegisteredEntries()) {
            Integer count = counts.get(entry.getKey());
            counts.put(entry.getKey(), Integer.valueOf(count == null ? 1 : count.intValue() + 1));
        }
        return counts;
    }

    protected static List<PonderComponentEntry> buildComponentEntries(Collection<ResourceLocation> componentIds) {
        Map<ResourceLocation, Integer> sceneCounts = countScenesByComponent();
        List<PonderComponentEntry> entries = new ArrayList<PonderComponentEntry>();
        for (ResourceLocation componentId : componentIds) {
            ItemStack displayStack = PonderIndex.getSceneAccess().getDisplayStack(componentId);
            String label = displayStack.isEmpty() ? componentId.toString() : displayStack.getDisplayName();
            Set<PonderTag> tags = PonderIndex.getTagAccess().getTags(componentId);
            int sceneCount = sceneCounts.containsKey(componentId) ? sceneCounts.get(componentId).intValue() : 0;
            entries.add(new PonderComponentEntry(componentId, displayStack, label, sceneCount, tags));
        }

        Collections.sort(entries);
        return entries;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            closeToParent();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
    protected GuiButton addFooterButton(int id, int x, int width, String label) {
        GuiButton button = new GuiButton(id, x, height - OUTER_MARGIN - 20, width, 20, label);
        return addCompatButton(button);
    }
}
