package net.createmod.ponder.foundation.external.register;

public record ExternalSceneCompileSummary(
    int sceneDefinitionCount,
    int compiledBundleCount,
    int componentBindingCount,
    int compileFailureCount
) {
    public static final ExternalSceneCompileSummary EMPTY = new ExternalSceneCompileSummary(0, 0, 0, 0);

    public ExternalSceneCompileSummary {
        sceneDefinitionCount = Math.max(0, sceneDefinitionCount);
        compiledBundleCount = Math.max(0, compiledBundleCount);
        componentBindingCount = Math.max(0, componentBindingCount);
        compileFailureCount = Math.max(0, compileFailureCount);
    }

    public ExternalSceneCompileSummary merge(ExternalSceneCompileSummary other) {
        if (other == null) {
            return this;
        }
        return new ExternalSceneCompileSummary(
            sceneDefinitionCount + other.sceneDefinitionCount,
            compiledBundleCount + other.compiledBundleCount,
            componentBindingCount + other.componentBindingCount,
            compileFailureCount + other.compileFailureCount
        );
    }

    public boolean hasSummary() {
        return !equals(EMPTY);
    }

    public String formatSummary() {
        return sceneDefinitionCount + " scene definition(s), " + compiledBundleCount + " compiled bundle(s), "
            + componentBindingCount + " component binding(s), " + compileFailureCount + " compile failure(s)";
    }
}
