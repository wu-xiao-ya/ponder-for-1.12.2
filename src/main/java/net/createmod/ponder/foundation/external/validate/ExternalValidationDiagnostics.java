package net.createmod.ponder.foundation.external.validate;

import java.util.function.Consumer;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.external.definition.SourceInfo;

public final class ExternalValidationDiagnostics implements Consumer<ExternalValidationDiagnostic> {

    private final Consumer<ExternalValidationDiagnostic> delegate;
    private int filesScanned;
    private int filesLoaded;
    private int filesFailed;
    private int warnings;
    private int errors;

    public ExternalValidationDiagnostics() {
        this(ExternalValidationDiagnostics::logDiagnostic);
    }

    public ExternalValidationDiagnostics(Consumer<ExternalValidationDiagnostic> delegate) {
        this.delegate = delegate == null ? ExternalValidationDiagnostics::logDiagnostic : delegate;
    }

    @Override
    public void accept(ExternalValidationDiagnostic diagnostic) {
        if (diagnostic == null) {
            return;
        }

        switch (diagnostic.severity()) {
            case WARN:
                warnings++;
                break;
            case ERROR:
                errors++;
                break;
            default:
                break;
        }

        delegate.accept(diagnostic);
    }

    public void fileScanned() {
        filesScanned++;
    }

    public void fileLoaded() {
        filesLoaded++;
    }

    public void fileFailed() {
        filesFailed++;
    }

    public void info(String channel, String subject, SourceInfo source, String detail) {
        accept(ExternalValidationDiagnostic.info(channel, subject, source, detail));
    }

    public void warn(String channel, String subject, SourceInfo source, String detail) {
        accept(ExternalValidationDiagnostic.warn(channel, subject, source, detail));
    }

    public void error(String channel, String subject, SourceInfo source, String detail, Throwable cause) {
        accept(ExternalValidationDiagnostic.error(channel, subject, source, detail, cause));
    }

    public ValidationReport report() {
        return new ValidationReport(filesScanned, filesLoaded, filesFailed, warnings, errors);
    }

    private static void logDiagnostic(ExternalValidationDiagnostic diagnostic) {
        String message = diagnostic.render();
        Throwable cause = diagnostic.cause();
        switch (diagnostic.severity()) {
            case INFO:
                logInfo(message, cause);
                break;
            case WARN:
                logWarn(message, cause);
                break;
            case ERROR:
                logError(message, cause);
                break;
            default:
                throw new IllegalStateException("Unhandled severity " + diagnostic.severity());
        }
    }

    private static void logInfo(String message, Throwable cause) {
        if (cause == null) {
            Ponder.LOGGER.info(message);
            return;
        }
        Ponder.LOGGER.info(message, cause);
    }

    private static void logWarn(String message, Throwable cause) {
        if (cause == null) {
            Ponder.LOGGER.warn(message);
            return;
        }
        Ponder.LOGGER.warn(message, cause);
    }

    private static void logError(String message, Throwable cause) {
        if (cause == null) {
            Ponder.LOGGER.error(message);
            return;
        }
        Ponder.LOGGER.error(message, cause);
    }
}
