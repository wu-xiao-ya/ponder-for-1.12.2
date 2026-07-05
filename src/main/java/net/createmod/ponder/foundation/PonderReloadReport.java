package net.createmod.ponder.foundation;

public record PonderReloadReport(int sceneEntryCount, int componentCount, int listedTagCount, int pluginCount,
    int sharedTextCount, PonderReloadDetails details) {

    public PonderReloadReport {
        details = details == null ? PonderReloadDetails.EMPTY : details;
    }

    public String formatCounts() {
        return pluginCount + " plugin(s), " + sceneEntryCount + " scene entry(ies), " + componentCount
            + " component(s), " + listedTagCount + " listed tag(s), " + sharedTextCount + " shared text entry(ies)";
    }
}
