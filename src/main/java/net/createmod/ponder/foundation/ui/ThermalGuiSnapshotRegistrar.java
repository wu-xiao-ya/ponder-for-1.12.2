package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.Ponder;
import net.minecraft.util.ResourceLocation;

final class ThermalGuiSnapshotRegistrar {

    private static final int TE_BASE_WIDTH = 198;
    private static final int TE_BASE_HEIGHT = 166;
    private static final int TE_PANEL_WIDTH = 100;
    private static final int TE_PANEL_HEIGHT = 92;
    private static final int TE_REDSTONE_PANEL_WIDTH = 112;
    private static final int TE_COMPOSITE_WIDTH = 276;
    private static final int TE_REDSTONE_COMPOSITE_WIDTH = 288;

    private ThermalGuiSnapshotRegistrar() {
    }

    static void registerDefaults() {
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

    private static void registerThermalPanelAliases() {
        Snapshot augmentPanel = thermalSnapshot("furnace_panel_augment.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        Snapshot configPanel = thermalSnapshot("furnace_panel_config.png", TE_PANEL_WIDTH, TE_PANEL_HEIGHT);
        Snapshot redstonePanel = thermalSnapshot("furnace_panel_redstone.png", TE_REDSTONE_PANEL_WIDTH,
            TE_PANEL_HEIGHT);

        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_panel_augment"), augmentPanel);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_panel_config"), configPanel);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_panel_redstone"), redstonePanel);

        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_furnace_panel_augment"), augmentPanel);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_furnace_panel_config"), configPanel);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_furnace_panel_redstone"), redstonePanel);
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

        PonderGuiSnapshotRegistry.register(baseId, base);
        PonderGuiSnapshotRegistry.register(augmentId, augment);
        PonderGuiSnapshotRegistry.register(configId, config);
        PonderGuiSnapshotRegistry.register(redstoneId, redstone);
        registerThermalLive(machineName);

        PonderGuiSnapshotRegistry.registerProvider(Ponder.asResource("gui_snapshot/te_" + machineName + "_cycle"),
            currentTick -> {
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
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_" + machineName + "_live"),
            Snapshot.liveRenderer(TE_BASE_WIDTH, TE_BASE_HEIGHT, true,
                new EmbeddedReflectiveGuiSnapshot(guiClass, tileClass)));
        if ("furnace".equals(machineName)) {
            PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_furnace_sandbox_live"),
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
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_" + machineName + "_base"),
            Snapshot.fullTexture(new ResourceLocation("thermalexpansion", "textures/gui/machine/" + machineName + ".png"),
                TE_BASE_WIDTH, TE_BASE_HEIGHT));
        registerThermalLive(machineName);
    }

    private static void registerThermalPanelLive(String machineName, String guiClass, String tileClass,
        String tabFieldName, String panelName, int width, int height) {
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/te_" + machineName + "_panel_" + panelName
            + "_live"), Snapshot.liveRenderer(width, height, true,
                new EmbeddedReflectiveTabGuiSnapshot(guiClass, tileClass, tabFieldName, width, height)));
    }
}
