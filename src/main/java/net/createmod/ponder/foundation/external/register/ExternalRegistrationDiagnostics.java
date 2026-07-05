package net.createmod.ponder.foundation.external.register;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.createmod.ponder.Ponder;

final class ExternalRegistrationDiagnostics implements Consumer<ExternalRegistrationDiagnostic> {

    private final Consumer<ExternalRegistrationDiagnostic> delegate;
    private final List<ExternalRegistrationDiagnostic> errors = new ArrayList<ExternalRegistrationDiagnostic>();
    private int infos;
    private int warnings;
    private int errorCount;

    ExternalRegistrationDiagnostics() {
        this(ExternalRegistrationDiagnostics.logger());
    }

    ExternalRegistrationDiagnostics(Consumer<ExternalRegistrationDiagnostic> delegate) {
        this.delegate = delegate == null ? ExternalRegistrationDiagnostics.logger() : delegate;
    }

    @Override
    public void accept(ExternalRegistrationDiagnostic diagnostic) {
        if (diagnostic == null) {
            return;
        }

        switch (diagnostic.severity()) {
            case INFO:
                infos++;
                break;
            case WARN:
                warnings++;
                break;
            case ERROR:
                errorCount++;
                errors.add(diagnostic);
                break;
            default:
                throw new IllegalStateException("Unhandled severity " + diagnostic.severity());
        }

        delegate.accept(diagnostic);
    }

    RegistrationDiagnosticReport report() {
        return new RegistrationDiagnosticReport(infos, warnings, errorCount);
    }

    ExternalRegistrationFailureReport failureReport() {
        if (errors.isEmpty()) {
            return ExternalRegistrationFailureReport.EMPTY;
        }
        return new ExternalRegistrationFailureReport(errors);
    }

    static Consumer<ExternalRegistrationDiagnostic> logger() {
        return new Consumer<ExternalRegistrationDiagnostic>() {
            @Override
            public void accept(ExternalRegistrationDiagnostic diagnostic) {
                logDiagnostic(diagnostic);
            }
        };
    }

    private static void logDiagnostic(ExternalRegistrationDiagnostic diagnostic) {
        if (diagnostic == null) {
            return;
        }

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
