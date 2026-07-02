package net.createmod.ponder.foundation.external.register;

import java.util.function.Consumer;

import net.createmod.ponder.Ponder;

final class ExternalRegistrationDiagnostics {

    private ExternalRegistrationDiagnostics() {
    }

    static Consumer<ExternalRegistrationDiagnostic> logger() {
        return new Consumer<ExternalRegistrationDiagnostic>() {
            @Override
            public void accept(ExternalRegistrationDiagnostic diagnostic) {
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
        };
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
