package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.RecordedOperation;
import net.createmod.ponder.foundation.PonderScene.WorldEvent;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

class DebugPanelRenderer {

    private static final int LINE_HEIGHT = 12;
    private static final int LEFT_PANEL_WIDTH = 170;
    private static final int OUTER_MARGIN = 12;
    private static final int HEADER_HEIGHT = 186;

   private final FontRenderer fontRenderer;
   private final PonderSceneSelectionState selectionState;
   private final Function<WorldEvent, String> formatWorldEventFn;
   private int operationScroll;
    private int componentScroll;

    @FunctionalInterface
    interface HoverTextRenderer {
        void render(List<String> textLines, int x, int y);
    }

    DebugPanelRenderer(FontRenderer fontRenderer, PonderSceneSelectionState selectionState,
        Function<WorldEvent, String> formatWorldEventFn) {
        this.fontRenderer = fontRenderer;
        this.selectionState = selectionState;
        this.formatWorldEventFn = formatWorldEventFn;
    }

    int getOperationScroll() {
        return operationScroll;
    }

    void setOperationScroll(int operationScroll) {
        this.operationScroll = Math.max(0, operationScroll);
    }

   void resetOperationScroll() {
       this.operationScroll = 0;
   }
    int getComponentScroll() {
        return componentScroll;
    }

    void setComponentScroll(int componentScroll) {
        this.componentScroll = Math.max(0, componentScroll);
    }

    void resetComponentScroll() {
        this.componentScroll = 0;
    }

    void scrollComponents(int delta, List<ResourceLocation> componentIds, int screenHeight) {
        this.componentScroll = MathHelper.clamp(componentScroll + delta, 0,
            getMaxComponentScroll(componentIds, screenHeight));
    }

    int getMaxComponentScroll(List<ResourceLocation> componentIds, int screenHeight) {
        int componentTop = 12 + 20;
        int componentBottom = screenHeight - 12 - 28;
        int visible = Math.max(1, (componentBottom - componentTop - 6) / 12);
        return Math.max(0, componentIds.size() - visible);
    }


    void scrollOperations(int delta, int screenHeight) {
        this.operationScroll = MathHelper.clamp(operationScroll + delta, 0, getMaxOperationScroll(screenHeight));
    }

    void drawComponentList(int mouseX, int mouseY, int x, int startY, int bottom,
        int lineHeight, int leftPanelWidth, DebugDrawContext ctx) {
        List<ResourceLocation> componentIds = selectionState.getComponentIds();
        int visibleLines = Math.max(1, (bottom - startY - 6) / lineHeight);
        int maxIndex = Math.min(componentIds.size(), componentScroll + visibleLines);

        for (int index = componentScroll; index < maxIndex; index++) {
            int y = startY + (index - componentScroll) * lineHeight;
            boolean selected = index == selectionState.getSelectedComponentIndex();
            boolean hovered = isWithin(mouseX, mouseY, x + 4, y - 1, x + leftPanelWidth - 4, y + lineHeight - 1);
            int background = selected ? 0xCC304060 : hovered ? 0x66303030 : 0x33202020;
            drawEntry(ctx, x, y, leftPanelWidth, lineHeight, background,
                componentIds.get(index).toString(), selected ? 0xFFFFFF : 0xD0D0D0);
        }
    }

    void drawSceneSummary(int x, int y, int width, int playbackTick, boolean playing, int sceneEndTick, DebugDrawContext ctx) {
        PonderScene scene = selectionState.getSelectedScene();
        ResourceLocation componentId = selectionState.getSelectedComponentId();

        if (componentId == null) {
            ctx.drawString("No Ponder components registered.", x + 6, y, 0xFF8888);
            return;
        }

        List<String> lines = new ArrayList<String>();
        lines.add("Component: " + componentId);

        if (scene == null) {
            lines.add("Scene: none");
            lines.add("Tip: use /ponder reload if you just changed registrations.");
        } else {
            PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
            PreviewState previewState = PonderScenePreview.buildState(scene, playbackTick);
            RecordedOperation activeOperation = getActiveOperation(scene.getRecordedOperations(), playbackTick);
            WorldEvent latestWorldEvent = getLatestWorldEvent(scene, playbackTick);
            List<PonderScene> compiledScenes = selectionState.getCompiledScenes();
            int sceneCount = compiledScenes.size();
            int sceneIdx = selectionState.getSelectedSceneIndex();
            lines.add("Scene: " + (sceneIdx + 1) + "/" + sceneCount + "  id=" + scene.getSceneId());
            lines.add("Title: " + scene.getTitle());
            lines.add("Schematic: " + scene.getSchematicLocation());
            String tickStatus = playing ? "playing" : "paused";
            lines.add("Ticks: " + playbackTick + "/" + sceneEndTick + "  status=" + tickStatus);
            lines.add("Active op: " + (activeOperation == null ? "none yet" : activeOperation.getDescription()));
            lines.add("Latest world: " + (latestWorldEvent == null ? "none yet" : formatWorldEventFn.apply(latestWorldEvent)));
            lines.add("Ops: " + scene.getRecordedOperations().size() + "  worldEvents=" + scene.getWorldEvents().size());
            lines.add("Visible blocks: " + previewState.visibleBlocks + "  columns=" + previewState.columnsWithBlocks);
            lines.add("Bounds: x " + bounds.minX + ".." + bounds.maxX + "  z " + bounds.minZ + ".." + bounds.maxZ
                + "  y " + bounds.minY + ".." + bounds.maxY);
            lines.add("Camera Y: " + Math.round(PonderSceneRuntime.getCameraYaw(scene, playbackTick)) + " / "
                + Math.round(scene.getAccumulatedCameraYaw()) + "  scale=" + scene.getScaleFactor());
            lines.add("Keyframes: " + scene.getKeyframeCount() + "  lazy=" + scene.getLazyKeyframeCount()
                + "  finished=" + scene.isFinished());
            lines.add("Tags: " + formatTags(PonderIndex.getTagAccess().getTags(componentId)));
        }

        for (int i = 0; i < lines.size(); i++) {
            ctx.drawString(fontRenderer.trimStringToWidth(lines.get(i), width - 12), x + 6, y + i * 12, 0xE0E0E0);
        }
    }

    void drawOperationList(int mouseX, int mouseY, int x, int startY, int width, int bottom,
        int lineHeight, int playbackTick, DebugDrawContext ctx) {
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        ctx.drawString("Timeline", x + 6, startY - 14, 0xFFEEDD);

        if (operations.isEmpty()) {
            ctx.drawString("No recorded operations for the current scene.", x + 6, startY, 0xA0A0A0);
            return;
        }

        int visibleLines = Math.max(1, (bottom - startY - 18) / lineHeight);
        int maxIndex = Math.min(operations.size(), operationScroll + visibleLines);
        int activeIndex = getActiveOperationIndex(operations, playbackTick);

        for (int index = operationScroll; index < maxIndex; index++) {
            RecordedOperation operation = operations.get(index);
            int y = startY + (index - operationScroll) * lineHeight;
            boolean hovered = isWithin(mouseX, mouseY, x + 4, y - 1, x + width - 4, y + lineHeight - 1);
            boolean active = index == activeIndex;
            boolean reached = operation.getTick() <= playbackTick;
            int background = active ? 0xCC6F5A1E : reached ? 0x663A4A28 : hovered ? 0x55303030 : 0x33202020;
            drawEntry(ctx, x, y, width, lineHeight, background, "T+" + operation.getTick() + " "
                + operation.getDescription(), active ? 0xFFFFFF : reached ? 0xE5F0D0 : 0xD0D0D0);
        }
    }

    void drawTooltips(int mouseX, int mouseY, List<ResourceLocation> componentIds,
        int leftPanelX, int leftPanelY, int leftPanelWidth, int rightPanelX, int rightPanelY, int rightPanelWidth, int lineHeight,
        HoverTextRenderer hoverTextRenderer) {

        if (mouseX >= leftPanelX + 4 && mouseX <= leftPanelX + leftPanelWidth - 4) {
            int relativeY = mouseY - (leftPanelY + 20);
            int index = componentScroll + relativeY / lineHeight;
            if (index >= 0 && index < componentIds.size()) {
                hoverTextRenderer.render(Collections.singletonList(componentIds.get(index).toString()), mouseX, mouseY);
                return;
            }
        }

        if (mouseX >= rightPanelX + 4 && mouseX <= rightPanelX + rightPanelWidth - 4) {
            int relativeY = mouseY - (rightPanelY + HEADER_HEIGHT);
            int index = operationScroll + relativeY / lineHeight;
            List<RecordedOperation> operations = getSelectedRecordedOperations();
            if (index >= 0 && index < operations.size()) {
                RecordedOperation operation = operations.get(index);
                hoverTextRenderer.render(Collections.singletonList("T+" + operation.getTick() + " " + operation.getDescription()),
                    mouseX, mouseY);
            }
        }
    }

    // ----- Absorbed helpers -----

    int getActiveOperationIndex(List<RecordedOperation> operations, int currentTick) {
        int activeIndex = -1;
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i).getTick() <= currentTick) {
                activeIndex = i;
            } else {
                break;
            }
        }
        return activeIndex;
    }

    @Nullable
    RecordedOperation getActiveOperation(List<RecordedOperation> operations, int currentTick) {
        int activeIndex = getActiveOperationIndex(operations, currentTick);
        return activeIndex >= 0 && activeIndex < operations.size() ? operations.get(activeIndex) : null;
    }

    @Nullable
    WorldEvent getLatestWorldEvent(PonderScene scene, int currentTick) {
        WorldEvent latest = null;
        for (WorldEvent event : scene.getWorldEvents()) {
            if (event.getTick() > currentTick) {
                break;
            }
            latest = event;
        }
        return latest;
    }

    int getVisibleOperationLines(int screenHeight) {
        int operationsTop = OUTER_MARGIN + HEADER_HEIGHT;
        int operationsBottom = screenHeight - OUTER_MARGIN - 46;
        return Math.max(1, (operationsBottom - operationsTop) / LINE_HEIGHT);
    }

    int getMaxOperationScroll(int screenHeight) {
        return Math.max(0, getSelectedRecordedOperations().size() - getVisibleOperationLines(screenHeight));
    }

    int computeCenteredOperationScroll(int playbackTick, int screenHeight) {
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        if (operations.isEmpty()) {
            return 0;
        }
        int activeIndex = Math.max(0, getActiveOperationIndex(operations, playbackTick));
        int visibleLines = getVisibleOperationLines(screenHeight);
        return MathHelper.clamp(activeIndex - visibleLines / 2, 0, getMaxOperationScroll(screenHeight));
    }

    int getComponentIndexAt(int mouseX, int mouseY, int screenHeight) {
        if (!isMouseOverComponentList(mouseX, mouseY, screenHeight)) {
            return -1;
        }
        int relativeY = mouseY - (OUTER_MARGIN + 20);
        int index = componentScroll + relativeY / LINE_HEIGHT;
        List<ResourceLocation> componentIds = selectionState.getComponentIds();
        return index >= 0 && index < componentIds.size() ? index : -1;
    }

    int getOperationIndexAt(int mouseX, int mouseY, int screenHeight) {
        if (!isMouseOverOperations(mouseX, mouseY, screenHeight)) {
            return -1;
        }
        int relativeY = mouseY - (OUTER_MARGIN + HEADER_HEIGHT);
        int index = operationScroll + relativeY / LINE_HEIGHT;
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        return index >= 0 && index < operations.size() ? index : -1;
    }

    boolean isMouseOverComponentList(int mouseX, int mouseY, int screenHeight) {
        return isWithin(mouseX, mouseY, OUTER_MARGIN + 4, OUTER_MARGIN + 20, OUTER_MARGIN + LEFT_PANEL_WIDTH - 4,
            screenHeight - OUTER_MARGIN - 28);
    }

    boolean isMouseOverOperations(int mouseX, int mouseY, int screenHeight) {
        int x = OUTER_MARGIN + LEFT_PANEL_WIDTH + OUTER_MARGIN;
        return isWithin(mouseX, mouseY, x + 4, OUTER_MARGIN + HEADER_HEIGHT, Integer.MAX_VALUE,
            screenHeight - OUTER_MARGIN - 28);
    }

    // ----- Private helpers -----

    private static String formatTags(Collection<PonderTag> tags) {
        if (tags.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (PonderTag tag : tags) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(tag.getId());
        }
        builder.append(']');
        return builder.toString();
    }

    private List<RecordedOperation> getSelectedRecordedOperations() {
        PonderScene scene = selectionState.getSelectedScene();
        return scene == null ? Collections.<RecordedOperation>emptyList() : scene.getRecordedOperations();
    }

    private static boolean isWithin(int mouseX, int mouseY, int minX, int minY, int maxX, int maxY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private void drawEntry(DebugDrawContext ctx, int x, int y, int width, int lineHeight, int background,
        String label, int color) {
        ctx.fillRect(x + 4, y - 1, x + width - 4, y + lineHeight - 1, background);
        ctx.drawString(fontRenderer.trimStringToWidth(label, width - 12), x + 8, y + 1, color);
    }
}
