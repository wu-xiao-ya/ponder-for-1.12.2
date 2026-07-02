package net.createmod.ponder.foundation.ui;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import net.createmod.ponder.Ponder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class PonderGuiSnapshotRegistry {

    private static final int TE_BASE_WIDTH        = 198;
    private static final int TE_BASE_HEIGHT       = 166;
    private static final int TE_PANEL_WIDTH       = 100;
    private static final int TE_PANEL_HEIGHT      = 92;
    private static final int TE_REDSTONE_PANEL_WIDTH = 112;
    private static final int TE_COMPOSITE_WIDTH   = 276;
    private static final int TE_REDSTONE_COMPOSITE_WIDTH = 288;

    private static final Map<ResourceLocation, SnapshotSource> SNAPSHOTS = new LinkedHashMap<>();

    static {
        registerDefaults();
    }

    private PonderGuiSnapshotRegistry() {
    }

    public static void register(ResourceLocation id, Snapshot snapshot) {
        if (id == null || snapshot == null) {
            return;
        }
        SNAPSHOTS.put(id, SnapshotSource.constant(snapshot));
    }

    public static void registerProvider(ResourceLocation id, SnapshotProvider provider) {
        if (id == null || provider == null) {
            return;
        }
        SNAPSHOTS.put(id, provider.asSource());
    }

    public static Snapshot get(ResourceLocation id, float currentTick) {
        SnapshotSource source = id == null ? null : SNAPSHOTS.get(id);
        return source == null ? null : source.resolve(SnapshotContext.of(currentTick));
    }

    public static ResourceLocation registerBlockGuiSnapshot(ResourceLocation blockId, int meta, int width, int height) {
        return registerBlockGuiSnapshot(blockId, meta, null, width, height);
    }

    public static ResourceLocation registerBlockGuiSnapshot(ResourceLocation blockId, int meta, NBTTagCompound tileNbt,
        int width, int height) {
        var snapshotId = Ponder.asResource(
            "gui_snapshot/block/%s/%s/%d/%dx%d".formatted(
                sanitizePath(blockId.getNamespace()),
                sanitizePath(blockId.getPath()),
                Math.max(0, meta),
                Math.max(1, width),
                Math.max(1, height)));
        register(snapshotId, Snapshot.liveRenderer(Math.max(1, width), Math.max(1, height), true,
            SandboxTriggeredBlockGuiSnapshot.getOrCreate(blockId, Math.max(0, meta), tileNbt)));
        return snapshotId;
    }

    private static String sanitizePath(String value) {
        String lower = value == null ? "unknown" : value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "unknown" : builder.toString();
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
        register(Ponder.asResource("gui_snapshot/minecraft_furnace_sandbox_live"),
            Snapshot.liveRenderer(176, 166, true, SandboxTriggeredBlockGuiSnapshot.MINECRAFT_FURNACE));
    }

    private static void registerThermalPanelAliases() {
        var augmentPanel = thermalSnapshot("furnace_panel_augment.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        var configPanel = thermalSnapshot("furnace_panel_config.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        var redstonePanel = thermalSnapshot("furnace_panel_redstone.png", TE_REDSTONE_PANEL_WIDTH, TE_PANEL_HEIGHT);

        register(Ponder.asResource("gui_snapshot/te_panel_augment"), augmentPanel);
        register(Ponder.asResource("gui_snapshot/te_panel_config"), configPanel);
        register(Ponder.asResource("gui_snapshot/te_panel_redstone"), redstonePanel);

        register(Ponder.asResource("gui_snapshot/te_furnace_panel_augment"), augmentPanel);
        register(Ponder.asResource("gui_snapshot/te_furnace_panel_config"), configPanel);
        register(Ponder.asResource("gui_snapshot/te_furnace_panel_redstone"), redstonePanel);
    }

    private static void registerThermalMachine(String machineName) {
        var baseId = Ponder.asResource("gui_snapshot/te_" + machineName + "_base");
        var augmentId = Ponder.asResource("gui_snapshot/te_" + machineName + "_augment");
        var configId = Ponder.asResource("gui_snapshot/te_" + machineName + "_config");
        var redstoneId = Ponder.asResource("gui_snapshot/te_" + machineName + "_redstone");

        var base = thermalSnapshot(machineName + "_snapshot_base.png", TE_BASE_WIDTH, TE_BASE_HEIGHT);
        var augment = thermalSnapshot(machineName + "_snapshot_augment.png", TE_COMPOSITE_WIDTH, TE_BASE_HEIGHT);
        var config = thermalSnapshot(machineName + "_snapshot_config.png", TE_COMPOSITE_WIDTH, TE_BASE_HEIGHT);
        var redstone = thermalSnapshot(machineName + "_snapshot_redstone.png", TE_REDSTONE_COMPOSITE_WIDTH,
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
        var suffix = Character.toUpperCase(machineName.charAt(0)) + machineName.substring(1);
        var guiClass = "cofh.thermalexpansion.gui.client.machine.Gui" + suffix;
        var tileClass = "cofh.thermalexpansion.block.machine.Tile" + suffix;
        register(Ponder.asResource("gui_snapshot/te_" + machineName + "_live"),
            Snapshot.liveRenderer(TE_BASE_WIDTH, TE_BASE_HEIGHT, true,
                new EmbeddedReflectiveGuiSnapshot(guiClass, tileClass)));
        if ("furnace".equals(machineName)) {
            register(Ponder.asResource("gui_snapshot/te_furnace_sandbox_live"),
                Snapshot.liveRenderer(TE_BASE_WIDTH, TE_BASE_HEIGHT, true,
                    SandboxTriggeredBlockGuiSnapshot.THERMAL_FURNACE));
        }
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
}
