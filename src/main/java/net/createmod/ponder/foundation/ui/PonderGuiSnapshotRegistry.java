package net.createmod.ponder.foundation.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import net.createmod.ponder.Ponder;
import net.minecraft.util.ResourceLocation;

public final class PonderGuiSnapshotRegistry {

    private static final int TE_BASE_WIDTH = 198;
    private static final int TE_BASE_HEIGHT = 166;
    private static final int TE_PANEL_WIDTH = 100;
    private static final int TE_PANEL_HEIGHT = 92;
    private static final int TE_REDSTONE_PANEL_WIDTH = 112;
    private static final int TE_COMPOSITE_WIDTH = 276;
    private static final int TE_REDSTONE_COMPOSITE_WIDTH = 288;

    private static final Map<ResourceLocation, SnapshotProvider> SNAPSHOTS =
        new LinkedHashMap<ResourceLocation, SnapshotProvider>();

    static {
        registerDefaults();
    }

    private PonderGuiSnapshotRegistry() {
    }

    public static void register(ResourceLocation id, Snapshot snapshot) {
        if (id == null || snapshot == null) {
            return;
        }
        registerProvider(id, currentTick -> snapshot);
    }

    public static void registerProvider(ResourceLocation id, SnapshotProvider provider) {
        if (id == null || provider == null) {
            return;
        }
        SNAPSHOTS.put(id, provider);
    }

    public static Snapshot get(ResourceLocation id, float currentTick) {
        SnapshotProvider provider = id == null ? null : SNAPSHOTS.get(id);
        return provider == null ? null : provider.provide(currentTick);
    }

    private static void registerDefaults() {
        registerMinecraftSnapshots();
        registerThermalPanelAliases();
        registerThermalMachine("furnace");
        registerThermalMachine("charger");
        registerThermalMachine("insolator");
        registerThermalMachine("pulverizer");
        registerThermalMachine("sawmill");
        registerThermalMachine("smelter");
        registerThermalLiveOnly("brewer");
        registerThermalLiveOnly("centrifuge");
        registerThermalLiveOnly("compactor");
        registerThermalLiveOnly("crafter");
        registerThermalLiveOnly("crucible");
        registerThermalLiveOnly("enchanter");
        registerThermalLiveOnly("extruder");
        registerThermalLiveOnly("precipitator");
        registerThermalLiveOnly("refinery");
        registerThermalLiveOnly("transposer");
    }

    private static void registerMinecraftSnapshots() {
        register(Ponder.asResource("gui_snapshot/minecraft_furnace"),
            Snapshot.fullTexture(new ResourceLocation("minecraft", "textures/gui/container/furnace.png"), 176, 166));
        register(Ponder.asResource("gui_snapshot/minecraft_furnace_live"),
            Snapshot.liveRenderer(176, 166, true, EmbeddedGuiFurnaceSnapshot.INSTANCE));
    }

    private static void registerThermalPanelAliases() {
        Snapshot augmentPanel = thermalSnapshot("furnace_panel_augment.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        Snapshot configPanel = thermalSnapshot("furnace_panel_config.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        Snapshot redstonePanel = thermalSnapshot("furnace_panel_redstone.png", TE_REDSTONE_PANEL_WIDTH, TE_PANEL_HEIGHT);

        register(Ponder.asResource("gui_snapshot/te_panel_augment"), augmentPanel);
        register(Ponder.asResource("gui_snapshot/te_panel_config"), configPanel);
        register(Ponder.asResource("gui_snapshot/te_panel_redstone"), redstonePanel);

        register(Ponder.asResource("gui_snapshot/te_furnace_panel_augment"), augmentPanel);
        register(Ponder.asResource("gui_snapshot/te_furnace_panel_config"), configPanel);
        register(Ponder.asResource("gui_snapshot/te_furnace_panel_redstone"), redstonePanel);
    }

    private static void registerThermalMachine(String machineName) {
        ResourceLocation baseId = Ponder.asResource("gui_snapshot/te_" + machineName + "_base");
        ResourceLocation augmentId = Ponder.asResource("gui_snapshot/te_" + machineName + "_augment");
        ResourceLocation configId = Ponder.asResource("gui_snapshot/te_" + machineName + "_config");
        ResourceLocation redstoneId = Ponder.asResource("gui_snapshot/te_" + machineName + "_redstone");

        Snapshot base = thermalSnapshot(machineName + "_snapshot_base.png", TE_BASE_WIDTH, TE_BASE_HEIGHT);
        Snapshot augment = thermalSnapshot(machineName + "_snapshot_augment.png", TE_COMPOSITE_WIDTH, TE_BASE_HEIGHT);
        Snapshot config = thermalSnapshot(machineName + "_snapshot_config.png", TE_COMPOSITE_WIDTH, TE_BASE_HEIGHT);
        Snapshot redstone = thermalSnapshot(machineName + "_snapshot_redstone.png", TE_REDSTONE_COMPOSITE_WIDTH,
            TE_BASE_HEIGHT);

        register(baseId, base);
        register(augmentId, augment);
        register(configId, config);
        register(redstoneId, redstone);
        registerThermalLive(machineName);

        registerProvider(Ponder.asResource("gui_snapshot/te_" + machineName + "_cycle"), currentTick -> {
            int frame = ((int) currentTick / 20) % 4;
            if (frame == 1) {
                return config;
            }
            if (frame == 2) {
                return augment;
            }
            if (frame == 3) {
                return redstone;
            }
            return base;
        });
    }

    private static Snapshot thermalSnapshot(String fileName, int width, int height) {
        return Snapshot.fullTexture(Ponder.asResource("textures/gui/thermalexpansion/" + fileName), width, height);
    }

    private static void registerThermalLive(String machineName) {
        String suffix = Character.toUpperCase(machineName.charAt(0)) + machineName.substring(1);
        String guiClass = "cofh.thermalexpansion.gui.client.machine.Gui" + suffix;
        String tileClass = "cofh.thermalexpansion.block.machine.Tile" + suffix;
        register(Ponder.asResource("gui_snapshot/te_" + machineName + "_live"),
            Snapshot.liveRenderer(TE_BASE_WIDTH, TE_BASE_HEIGHT, true,
                new EmbeddedReflectiveGuiSnapshot(guiClass, tileClass)));
        registerThermalPanelLive(machineName, guiClass, tileClass, "augmentTab", "augment", TE_PANEL_WIDTH,
            TE_PANEL_HEIGHT);
        registerThermalPanelLive(machineName, guiClass, tileClass, "configTab", "config", TE_PANEL_WIDTH,
            TE_PANEL_HEIGHT);
        registerThermalPanelLive(machineName, guiClass, tileClass, "redstoneTab", "redstone",
            TE_REDSTONE_PANEL_WIDTH, TE_PANEL_HEIGHT);
    }

    private static void registerThermalLiveOnly(String machineName) {
        register(Ponder.asResource("gui_snapshot/te_" + machineName + "_base"),
            Snapshot.fullTexture(new ResourceLocation("thermalexpansion", "textures/gui/machine/" + machineName + ".png"),
                TE_BASE_WIDTH, TE_BASE_HEIGHT));
        registerThermalLive(machineName);
    }

    private static void registerThermalPanelLive(String machineName, String guiClass, String tileClass,
        String tabFieldName, String panelName, int width, int height) {
        register(Ponder.asResource("gui_snapshot/te_" + machineName + "_panel_" + panelName + "_live"),
            Snapshot.liveRenderer(width, height, true,
                new EmbeddedReflectiveTabGuiSnapshot(guiClass, tileClass, tabFieldName, width, height)));
    }

    public interface SnapshotProvider {
        Snapshot provide(float currentTick);
    }

    public interface SnapshotRenderer {
        void render(int x, int y, int width, int height, float currentTick, float fade);
    }

    public static final class Snapshot {
        public final ResourceLocation texture;
        public final int u;
        public final int v;
        public final int regionWidth;
        public final int regionHeight;
        public final int textureWidth;
        public final int textureHeight;
        public final int displayWidth;
        public final int displayHeight;
        public final boolean framed;
        public final SnapshotRenderer renderer;

        public Snapshot(ResourceLocation texture, int u, int v, int regionWidth, int regionHeight, int textureWidth,
            int textureHeight, int displayWidth, int displayHeight, boolean framed, SnapshotRenderer renderer) {
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.regionWidth = regionWidth;
            this.regionHeight = regionHeight;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.framed = framed;
            this.renderer = renderer;
        }

        public static Snapshot fullTexture(ResourceLocation texture, int width, int height) {
            return new Snapshot(texture, 0, 0, width, height, width, height, width, height, true, null);
        }

        public static Snapshot liveRenderer(int width, int height, boolean framed, SnapshotRenderer renderer) {
            return new Snapshot(null, 0, 0, width, height, width, height, width, height, framed, renderer);
        }
    }
}
