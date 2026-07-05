package net.createmod.ponder.foundation.external.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ExternalScanResult(
    List<File> files,
    List<File> scannedRoots,
    List<File> skippedRoots
) {
    public static final ExternalScanResult EMPTY = new ExternalScanResult(
        Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
    );

    public ExternalScanResult {
        files = files == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<File>(files));
        scannedRoots = scannedRoots == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<File>(scannedRoots));
        skippedRoots = skippedRoots == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<File>(skippedRoots));
    }
}
