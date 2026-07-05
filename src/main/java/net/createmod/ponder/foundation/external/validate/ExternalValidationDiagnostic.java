package net.createmod.ponder.foundation.external.validate;

import net.createmod.ponder.foundation.external.definition.SourceInfo;

public record ExternalValidationDiagnostic(
    Severity severity,
    String channel,
    String subject,
    SourceInfo source,
    String detail,
    Throwable cause
) {
    public enum Severity {
        INFO,
        WARN,
        ERROR
    }

    public ExternalValidationDiagnostic {
        if (severity == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (source == null) {
            source = SourceInfo.EMPTY;
        }
        if (detail == null) {
            detail = "";
        }
    }

    public static ExternalValidationDiagnostic info(String channel, String subject, SourceInfo source, String detail) {
        return new ExternalValidationDiagnostic(Severity.INFO, channel, subject, source, detail, null);
    }

    public static ExternalValidationDiagnostic warn(String channel, String subject, SourceInfo source, String detail) {
        return new ExternalValidationDiagnostic(Severity.WARN, channel, subject, source, detail, null);
    }

    public static ExternalValidationDiagnostic error(String channel, String subject, SourceInfo source, String detail,
        Throwable cause) {
        return new ExternalValidationDiagnostic(Severity.ERROR, channel, subject, source, detail, cause);
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        switch (severity) {
            case INFO:
                builder.append("External validation ");
                break;
            case WARN:
                builder.append("Skipping external validation ");
                break;
            case ERROR:
                builder.append("Failed to validate external ");
                break;
            default:
                throw new IllegalStateException("Unhandled severity " + severity);
        }
        builder.append(channel)
            .append(" '")
            .append(subject)
            .append("'");
        builder.append(" from ").append(sourceLabel());
        if (!detail.isEmpty()) {
            builder.append(" - ").append(detail);
        }
        return builder.toString();
    }

    private String sourceLabel() {
        if (source.sourcePath().isEmpty()) {
            return source.namespace();
        }
        return source.namespace() + " @ " + source.sourcePath();
    }
}
