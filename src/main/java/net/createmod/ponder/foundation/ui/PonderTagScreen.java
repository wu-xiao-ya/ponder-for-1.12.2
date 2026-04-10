package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class PonderTagScreen extends AbstractPonderBrowserScreen {

    private static final int BUTTON_RELOAD = 1;
    private static final int BUTTON_DONE = 2;
    private static final int SUMMARY_HEIGHT = 122;
    private static final int GRID_SLOT_SIZE = 24;
    private static final int GRID_SLOT_GAP = 8;

    private final ResourceLocation tagId;
    private final List<PonderComponentEntry> components = new ArrayList<PonderComponentEntry>();
    private final List<PonderClickRegion<PonderComponentEntry>> visibleComponentRegions =
        new ArrayList<PonderClickRegion<PonderComponentEntry>>();

    private PonderTag tag;
    private int componentScroll;
    private String hoverHint = "";

    public PonderTagScreen(ResourceLocation tagId) {
        this(tagId, null);
    }

    public PonderTagScreen(ResourceLocation tagId, @Nullable GuiScreen parentScreen) {
        super(parentScreen);
        this.tagId = tagId;
    }

    public PonderTagScreen(PonderTag tag, @Nullable GuiScreen parentScreen) {
        this(tag.getId(), parentScreen);
    }

    @Override
    public void initGui() {
        super.initGui();
        reloadTagView();

        clearCompatButtons();
        addFooterButton(BUTTON_RELOAD, OUTER_MARGIN, 60, tr("button.reload"));
        addFooterButton(BUTTON_DONE, width - OUTER_MARGIN - 70, 70, tr("button.done"));
    }

    private void reloadTagView() {
        tag = PonderIndex.getTagAccess().getRegisteredTag(tagId);
        components.clear();
        components.addAll(buildComponentEntries(PonderIndex.getTagAccess().getItems(tag)));
        componentScroll = MathHelper.clamp(componentScroll, 0, getMaxComponentScroll());
        hoverHint = "";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_RELOAD) {
            PonderIndex.reload();
            reloadTagView();
        } else if (button.id == BUTTON_DONE) {
            closeToParent();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }

        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (!isMouseWithinGrid(mouseX, mouseY)) {
            return;
        }
        componentScroll = clampScroll(componentScroll, wheel > 0 ? -1 : 1, getMaxComponentScroll());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }

        PonderComponentEntry entry = getClickedComponent(mouseX, mouseY);
        if (entry != null) {
            mc.displayGuiScreen(PonderUI.showcase(entry.componentId, 0, this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawPonderBackground();
        hoverHint = "";
        visibleComponentRegions.clear();

        int panelX = OUTER_MARGIN;
        int panelY = OUTER_MARGIN;
        int panelWidth = width - OUTER_MARGIN * 2;
        int contentBottom = height - OUTER_MARGIN - FOOTER_HEIGHT;
        int panelHeight = contentBottom - panelY;

        drawPanel(panelX, panelY, panelWidth, panelHeight);
        drawSummary(panelX, panelY, panelWidth);
        drawComponents(mouseX, mouseY, panelX, panelY + SUMMARY_HEIGHT, panelWidth, panelHeight - SUMMARY_HEIGHT);

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawHintLine(resolveHint());
    }

    private void drawSummary(int x, int y, int panelWidth) {
        drawCenteredString(fontRenderer, tr("tag.title"), width / 2, y + 8, COLOR_TEXT);
        drawSectionLabel(tag.getTitle(), x + 64, y + 28);
        drawTagSlot(tag, x + 16, y + 24, 32, false, true);

        int totalScenes = 0;
        for (PonderComponentEntry entry : components) {
            totalScenes += entry.sceneCount;
        }

        drawString(fontRenderer, tr("tag.component_count", Integer.valueOf(components.size())), x + 64, y + 44,
            COLOR_TEXT_MUTED);
        drawString(fontRenderer, tr("tag.scene_count", Integer.valueOf(totalScenes)), x + 164, y + 44,
            COLOR_TEXT_MUTED);
        fontRenderer.drawSplitString(tag.getDescription(), x + 16, y + 68, panelWidth - 32, COLOR_TEXT_DIM);
        drawSectionLabel(tr("tag.associated_components"), x + 16, y + SUMMARY_HEIGHT - 18);
    }

    private void drawComponents(int mouseX, int mouseY, int x, int y, int panelWidth, int panelHeight) {
        int innerX = x + 16;
        int innerY = y + 10;
        int innerWidth = panelWidth - 32;
        int columns = Math.max(1, (innerWidth + GRID_SLOT_GAP) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int totalRows = (int) Math.ceil(components.size() / (double) columns);
        int rowsVisible = Math.max(1, (panelHeight - 26) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int startRow = Math.min(componentScroll, Math.max(0, totalRows - rowsVisible));
        int startIndex = startRow * columns;
        int endIndex = Math.min(components.size(), startIndex + rowsVisible * columns);

        if (components.isEmpty()) {
            drawString(fontRenderer, tr("tag.no_components"), innerX, innerY, COLOR_TEXT_DIM);
            return;
        }

        for (int index = startIndex; index < endIndex; index++) {
            PonderComponentEntry entry = components.get(index);
            int localIndex = index - startIndex;
            int column = localIndex % columns;
            int row = localIndex / columns;
            int slotX = innerX + column * (GRID_SLOT_SIZE + GRID_SLOT_GAP);
            int slotY = innerY + row * (GRID_SLOT_SIZE + GRID_SLOT_GAP);
            boolean hovered = isWithin(mouseX, mouseY, slotX, slotY, GRID_SLOT_SIZE, GRID_SLOT_SIZE);

            drawItemSlot(entry.displayStack, slotX, slotY, GRID_SLOT_SIZE, hovered, false);
            visibleComponentRegions.add(new PonderClickRegion<PonderComponentEntry>(entry, slotX, slotY,
                GRID_SLOT_SIZE, GRID_SLOT_SIZE));

            if (hovered) {
                hoverHint = tr("tag.hover.component", entry.label, Integer.valueOf(entry.sceneCount));
            }
        }

        drawString(fontRenderer, tr("tag.click_to_open"), x + 16, y + panelHeight - 16, COLOR_TEXT_MUTED);
    }

    private int getMaxComponentScroll() {
        int innerWidth = width - OUTER_MARGIN * 2 - 32;
        int columns = Math.max(1, (innerWidth + GRID_SLOT_GAP) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int panelHeight = height - OUTER_MARGIN - FOOTER_HEIGHT - OUTER_MARGIN - SUMMARY_HEIGHT;
        int rowsVisible = Math.max(1, (panelHeight - 26) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int totalRows = (int) Math.ceil(components.size() / (double) columns);
        return Math.max(0, totalRows - rowsVisible);
    }

    private boolean isMouseWithinGrid(int mouseX, int mouseY) {
        return isWithin(mouseX, mouseY, OUTER_MARGIN, OUTER_MARGIN + SUMMARY_HEIGHT, width - OUTER_MARGIN * 2,
            height - OUTER_MARGIN - FOOTER_HEIGHT - (OUTER_MARGIN + SUMMARY_HEIGHT));
    }

    @Nullable
    private PonderComponentEntry getClickedComponent(int mouseX, int mouseY) {
        for (PonderClickRegion<PonderComponentEntry> region : visibleComponentRegions) {
            if (region.contains(mouseX, mouseY)) {
                return region.value;
            }
        }
        return null;
    }

    private String resolveHint() {
        if (!hoverHint.isEmpty()) {
            return hoverHint;
        }
        return tag.getTitle() + "  |  " + tag.getDescription();
    }
}
