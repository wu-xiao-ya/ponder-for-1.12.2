package net.createmod.ponder.foundation.external.definition;

import java.io.File;

/**
 * Metadata about where a definition was loaded from.
 */
public record SourceInfo(
    String sourcePath,
    String namespace
) {
    public static final SourceInfo EMPTY = new SourceInfo("", "ponder");

    /**
     * Creates a SourceInfo from a file and namespace, storing the absolute path.
     */
    public static SourceInfo of(File file, String namespace) {
        return new SourceInfo(file.getAbsolutePath(), namespace);
    }
}
