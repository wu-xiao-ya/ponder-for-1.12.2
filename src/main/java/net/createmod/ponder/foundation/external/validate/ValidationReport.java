package net.createmod.ponder.foundation.external.validate;

public record ValidationReport(
    int filesScanned,
    int filesLoaded,
    int filesFailed,
    int warnings,
    int errors
) {
    public static final ValidationReport EMPTY = new ValidationReport(0, 0, 0, 0, 0);

    public ValidationReport {
        filesScanned = Math.max(0, filesScanned);
        filesLoaded = Math.max(0, filesLoaded);
        filesFailed = Math.max(0, filesFailed);
        warnings = Math.max(0, warnings);
        errors = Math.max(0, errors);
    }

    public ValidationReport merge(ValidationReport other) {
        if (other == null) {
            return this;
        }
        return new ValidationReport(
            filesScanned + other.filesScanned,
            filesLoaded + other.filesLoaded,
            filesFailed + other.filesFailed,
            warnings + other.warnings,
            errors + other.errors
        );
    }
}
