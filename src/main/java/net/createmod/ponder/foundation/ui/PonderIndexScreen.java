package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PonderIndexScreen extends AbstractPonderBrowserScreen {

    private static final int BUTTON_RELOAD = 1;
    private static final int BUTTON_CLEAR_FILTER = 2;
    private static final int BUTTON_DONE = 3;
    private static final int TAG_PANEL_WIDTH = 220;
    private static final int HEADER_HEIGHT = 70;
    private static final int TAG_ROW_HEIGHT = 28;
    private static final int GRID_SLOT_SIZE = 24;
    private static final int GRID_SLOT_GAP = 8;

    private final List<PonderTag> listedTags = new ArrayList<PonderTag>();
    private final List<PonderComponentEntry> allComponents = new ArrayList<PonderComponentEntry>();
    private final List<PonderComponentEntry> filteredComponents = new ArrayList<PonderComponentEntry>();
    private final List<PonderClickRegion<PonderTag>> visibleTagRegions = new ArrayList<PonderClickRegion<PonderTag>>();
    private final List<PonderClickRegion<PonderComponentEntry>> visibleComponentRegions =
        new ArrayList<PonderClickRegion<PonderComponentEntry>>();

    @Nullable
    private ResourceLocation selectedTagId;
    private int tagScroll;
    private int componentScroll;
    private GuiButton clearFilterButton;
    private String hoverHint = "";
    private String searchQuery = "";

    public PonderIndexScreen() {
        this(null);
    }

    public PonderIndexScreen(@Nullable GuiScreen parentScreen) {
        super(parentScreen);
    }

    @Override
    public void initGui() {
        super.initGui();
        reloadRegistryView();

        clearCompatButtons();
        addFooterButton(BUTTON_RELOAD, OUTER_MARGIN, 60, tr("button.reload"));
        clearFilterButton = addFooterButton(BUTTON_CLEAR_FILTER, OUTER_MARGIN + 64, 92, tr("button.clear_filter"));
        addFooterButton(BUTTON_DONE, width - OUTER_MARGIN - 70, 70, tr("button.done"));
        updateButtonState();
    }

    private void reloadRegistryView() {
        listedTags.clear();
        listedTags.addAll(PonderIndex.getTagAccess().getListedTags());

        allComponents.clear();
        allComponents.addAll(buildComponentEntries(PonderDebugScreen.getRegisteredComponents()));

        if (selectedTagId != null && PonderIndex.getTagAccess().getRegisteredTag(selectedTagId) == null) {
            selectedTagId = null;
        }

        rebuildFilteredComponents();
        tagScroll = MathHelper.clamp(tagScroll, 0, getMaxTagScroll());
        componentScroll = MathHelper.clamp(componentScroll, 0, getMaxComponentScroll());
        hoverHint = "";
    }

    private void rebuildFilteredComponents() {
        filteredComponents.clear();
        for (PonderComponentEntry entry : allComponents) {
            if ((selectedTagId == null || containsTag(entry.tags, selectedTagId)) && matchesSearch(entry)) {
                filteredComponents.add(entry);
            }
        }
    }

    private boolean matchesSearch(PonderComponentEntry entry) {
        String query = normalizeSearch(searchQuery);
        if (query.isEmpty()) {
            return true;
        }
        if (normalizeSearch(entry.label).contains(query)) {
            return true;
        }
        if (normalizeSearch(entry.componentId.toString()).contains(query)) {
            return true;
        }
        for (PonderTag tag : entry.tags) {
            if (normalizeSearch(tag.getTitle()).contains(query) || normalizeSearch(tag.getId().toString()).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeSearch(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private static boolean isSearchCharacter(char typedChar) {
        return typedChar >= 32 && typedChar != 127;
    }

    private boolean containsTag(Set<PonderTag> tags, ResourceLocation tagId) {
        for (PonderTag tag : tags) {
            if (tagId.equals(tag.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_RELOAD) {
            PonderIndex.reload();
            reloadRegistryView();
        } else if (button.id == BUTTON_CLEAR_FILTER) {
            selectedTagId = null;
            searchQuery = "";
            rebuildFilteredComponents();
            componentScroll = 0;
            updateButtonState();
        } else if (button.id == BUTTON_DONE) {
            closeToParent();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_BACK && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            rebuildFilteredComponents();
            componentScroll = 0;
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_DELETE && !searchQuery.isEmpty()) {
            searchQuery = "";
            rebuildFilteredComponents();
            componentScroll = 0;
            updateButtonState();
            return;
        }

        if (isSearchCharacter(typedChar) && searchQuery.length() < 64) {
            searchQuery += typedChar;
            rebuildFilteredComponents();
            componentScroll = 0;
            updateButtonState();
            return;
        }

        super.keyTyped(typedChar, keyCode);
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
        int delta = wheel > 0 ? -1 : 1;
        if (isMouseWithinTags(mouseX, mouseY)) {
            tagScroll = clampScroll(tagScroll, delta, getMaxTagScroll());
        } else if (isMouseWithinGrid(mouseX, mouseY)) {
            componentScroll = clampScroll(componentScroll, delta, getMaxComponentScroll());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        PonderTag clickedTag = getClickedTag(mouseX, mouseY);
        if (clickedTag != null) {
            if (mouseButton == 1) {
                mc.displayGuiScreen(new PonderTagScreen(clickedTag, this));
                return;
            }
            if (mouseButton == 0) {
                selectedTagId = clickedTag.getId().equals(selectedTagId) ? null : clickedTag.getId();
                rebuildFilteredComponents();
                componentScroll = 0;
                updateButtonState();
            }
            return;
        }

        if (mouseButton != 0) {
            return;
        }

        PonderComponentEntry component = getClickedComponent(mouseX, mouseY);
        if (component != null) {
            mc.displayGuiScreen(PonderUI.showcase(component.componentId, 0, this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawPonderBackground();
        hoverHint = "";
        visibleTagRegions.clear();
        visibleComponentRegions.clear();

        int leftX = OUTER_MARGIN;
        int topY = OUTER_MARGIN;
        int contentBottom = height - OUTER_MARGIN - FOOTER_HEIGHT;
        int leftHeight = contentBottom - topY;
        int rightX = leftX + TAG_PANEL_WIDTH + OUTER_MARGIN;
        int rightWidth = width - rightX - OUTER_MARGIN;

        drawPanel(leftX, topY, TAG_PANEL_WIDTH, leftHeight);
        drawPanel(rightX, topY, rightWidth, leftHeight);

        drawHeader(leftX, topY, TAG_PANEL_WIDTH, rightX, rightWidth);
        drawTagsPanel(mouseX, mouseY, leftX, topY + HEADER_HEIGHT, TAG_PANEL_WIDTH, leftHeight - HEADER_HEIGHT);
        drawComponentsPanel(mouseX, mouseY, rightX, topY + HEADER_HEIGHT, rightWidth, leftHeight - HEADER_HEIGHT);

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawHintLine(resolveHint());
    }

    private void drawHeader(int leftX, int topY, int leftWidth, int rightX, int rightWidth) {
        drawCenteredString(fontRenderer, tr("browser.title"), width / 2, topY + 8, COLOR_TEXT);
        drawString(fontRenderer, tr("browser.help"), leftX + 10, topY + 26, COLOR_TEXT_MUTED);
        drawString(fontRenderer, selectedTagId == null ? tr("browser.all_tags") : getSelectedTag().getTitle(), leftX + 10,
            topY + 44, selectedTagId == null ? COLOR_TEXT_DIM : COLOR_ACCENT);

        String registryStats = tr("browser.stats", Integer.valueOf(listedTags.size()),
            Integer.valueOf(filteredComponents.size()));
        drawSearchBox(rightX + 10, topY + 22, rightWidth - 20);
        drawString(fontRenderer, registryStats, rightX + 10, topY + 44, COLOR_TEXT_MUTED);
        drawSectionLabel(tr("browser.tags"), leftX + 10, topY + 56);
        drawSectionLabel(tr("browser.components"), rightX + 10, topY + 56);
    }

    private void drawSearchBox(int x, int y, int width) {
        drawRect(x, y, x + width, y + 16, COLOR_SLOT_BORDER);
        drawRect(x + 1, y + 1, x + width - 1, y + 15, COLOR_SLOT_FILL);
        String label = searchQuery.isEmpty() ? tr("browser.search.placeholder") : searchQuery;
        int color = searchQuery.isEmpty() ? COLOR_TEXT_DIM : COLOR_TEXT;
        drawString(fontRenderer, tr("browser.search"), x + 5, y + 4, COLOR_TEXT_MUTED);
        int textX = x + 56;
        int textWidth = width - 64;
        drawString(fontRenderer, fontRenderer.trimStringToWidth(label, textWidth), textX, y + 4, color);
        if (!searchQuery.isEmpty() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursorX = textX + fontRenderer.getStringWidth(fontRenderer.trimStringToWidth(searchQuery, textWidth));
            drawRect(cursorX, y + 3, cursorX + 1, y + 13, COLOR_ACCENT);
        }
    }

    private void drawTagsPanel(int mouseX, int mouseY, int x, int y, int panelWidth, int panelHeight) {
        int availableHeight = panelHeight - 10;
        int maxRows = Math.max(1, availableHeight / TAG_ROW_HEIGHT);
        int start = Math.min(tagScroll, Math.max(0, listedTags.size() - maxRows));
        int end = Math.min(listedTags.size(), start + maxRows);

        if (listedTags.isEmpty()) {
            drawString(fontRenderer, tr("browser.no_tags"), x + 10, y + 10, COLOR_TEXT_DIM);
            return;
        }

        for (int index = start; index < end; index++) {
            PonderTag tag = listedTags.get(index);
            int rowY = y + 6 + (index - start) * TAG_ROW_HEIGHT;
            boolean hovered = isWithin(mouseX, mouseY, x + 8, rowY, panelWidth - 16, TAG_ROW_HEIGHT - 4);
            boolean selected = tag.getId().equals(selectedTagId);
            drawRowBackground(x + 8, rowY, panelWidth - 16, TAG_ROW_HEIGHT - 4, hovered, selected);
            drawTagSlot(tag, x + 12, rowY + 3, 18, hovered, selected);

            int itemCount = PonderIndex.getTagAccess().getItems(tag).size();
            String countText = Integer.toString(itemCount);
            int countX = x + panelWidth - 16 - fontRenderer.getStringWidth(countText);
            drawString(fontRenderer, countText, countX, rowY + 9, COLOR_TEXT_MUTED);
            drawString(fontRenderer, fontRenderer.trimStringToWidth(tag.getTitle(), panelWidth - 64), x + 36, rowY + 5,
                COLOR_TEXT);
            drawString(fontRenderer, fontRenderer.trimStringToWidth(tag.getDescription(), panelWidth - 74), x + 36,
                rowY + 15, COLOR_TEXT_DIM);

            visibleTagRegions.add(new PonderClickRegion<PonderTag>(tag, x + 8, rowY, panelWidth - 16,
                TAG_ROW_HEIGHT - 4));
            if (hovered) {
                hoverHint = tr("browser.hover.tag", tag.getTitle());
            }
        }
    }

    private void drawComponentsPanel(int mouseX, int mouseY, int x, int y, int panelWidth, int panelHeight) {
        int innerX = x + 12;
        int innerY = y + 10;
        int innerWidth = panelWidth - 24;
        int columns = Math.max(1, (innerWidth + GRID_SLOT_GAP) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int totalRows = (int) Math.ceil(filteredComponents.size() / (double) columns);
        int maxRows = Math.max(1, (panelHeight - 18) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int startRow = Math.min(componentScroll, Math.max(0, totalRows - maxRows));
        int startIndex = startRow * columns;
        int endIndex = Math.min(filteredComponents.size(), startIndex + maxRows * columns);

        if (filteredComponents.isEmpty()) {
            String label = searchQuery.isEmpty()
                ? (selectedTagId == null ? tr("browser.no_components") : tr("browser.no_components_for_tag"))
                : tr("browser.no_components_for_search", searchQuery);
            drawString(fontRenderer, label, innerX, innerY, COLOR_TEXT_DIM);
            return;
        }

        for (int index = startIndex; index < endIndex; index++) {
            PonderComponentEntry entry = filteredComponents.get(index);
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
                hoverHint = tr("browser.hover.component", entry.label, Integer.valueOf(entry.sceneCount));
            }
        }

        int textY = y + panelHeight - 16;
        String filterLabel = selectedTagId == null ? tr("browser.filter.all")
            : tr("browser.filter.tag", getSelectedTag().getTitle());
        if (!searchQuery.isEmpty()) {
            filterLabel += "  |  " + tr("browser.filter.search", searchQuery);
        }
        drawString(fontRenderer, fontRenderer.trimStringToWidth(filterLabel, panelWidth - 24), x + 12, textY,
            COLOR_TEXT_MUTED);
    }

    private String resolveHint() {
        if (!hoverHint.isEmpty()) {
            return hoverHint;
        }
        if (selectedTagId != null) {
            return tr("browser.hint.active_filter", getSelectedTag().getTitle());
        }
        return searchQuery.isEmpty() ? tr("browser.hint.debug") : tr("browser.hint.search");
    }

    private int getMaxTagScroll() {
        int availableHeight = height - OUTER_MARGIN - FOOTER_HEIGHT - (OUTER_MARGIN + HEADER_HEIGHT) - 10;
        int rowsVisible = Math.max(1, availableHeight / TAG_ROW_HEIGHT);
        return Math.max(0, listedTags.size() - rowsVisible);
    }

    private int getMaxComponentScroll() {
        int panelWidth = width - (OUTER_MARGIN + TAG_PANEL_WIDTH + OUTER_MARGIN) - OUTER_MARGIN;
        int innerWidth = panelWidth - 24;
        int columns = Math.max(1, (innerWidth + GRID_SLOT_GAP) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int contentHeight = height - OUTER_MARGIN - FOOTER_HEIGHT - (OUTER_MARGIN + HEADER_HEIGHT);
        int rowsVisible = Math.max(1, (contentHeight - 18) / (GRID_SLOT_SIZE + GRID_SLOT_GAP));
        int totalRows = (int) Math.ceil(filteredComponents.size() / (double) columns);
        return Math.max(0, totalRows - rowsVisible);
    }

    private boolean isMouseWithinTags(int mouseX, int mouseY) {
        int panelHeight = height - OUTER_MARGIN - FOOTER_HEIGHT - OUTER_MARGIN;
        return isWithin(mouseX, mouseY, OUTER_MARGIN, OUTER_MARGIN + HEADER_HEIGHT, TAG_PANEL_WIDTH,
            panelHeight - HEADER_HEIGHT);
    }

    private boolean isMouseWithinGrid(int mouseX, int mouseY) {
        int rightX = OUTER_MARGIN + TAG_PANEL_WIDTH + OUTER_MARGIN;
        int rightWidth = width - rightX - OUTER_MARGIN;
        int panelHeight = height - OUTER_MARGIN - FOOTER_HEIGHT - OUTER_MARGIN;
        return isWithin(mouseX, mouseY, rightX, OUTER_MARGIN + HEADER_HEIGHT, rightWidth, panelHeight - HEADER_HEIGHT);
    }

    @Nullable
    private PonderTag getClickedTag(int mouseX, int mouseY) {
        for (PonderClickRegion<PonderTag> region : visibleTagRegions) {
            if (region.contains(mouseX, mouseY)) {
                return region.value;
            }
        }
        return null;
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

    private void updateButtonState() {
        if (clearFilterButton != null) {
            clearFilterButton.enabled = selectedTagId != null || !searchQuery.isEmpty();
        }
    }

    private PonderTag getSelectedTag() {
        return selectedTagId == null ? null : PonderIndex.getTagAccess().getRegisteredTag(selectedTagId);
    }
}
