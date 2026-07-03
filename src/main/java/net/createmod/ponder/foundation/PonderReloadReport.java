package net.createmod.ponder.foundation;

public record PonderReloadReport(int sceneEntryCount, int listedTagCount, int pluginCount, int sharedTextCount) {

    public String formatCounts() {
        return pluginCount + " plugin(s), " + sceneEntryCount + " scene entry(ies), " + listedTagCount
            + " listed tag(s), " + sharedTextCount + " shared text entry(ies)";
    }
}
