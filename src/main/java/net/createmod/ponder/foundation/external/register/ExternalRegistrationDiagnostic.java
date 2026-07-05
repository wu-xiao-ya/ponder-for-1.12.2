package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.foundation.external.definition.SourceInfo;

public record ExternalRegistrationDiagnostic(
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

    public ExternalRegistrationDiagnostic {
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

    public static ExternalRegistrationDiagnostic info(String channel, String subject, SourceInfo source, String detail) {
        return new ExternalRegistrationDiagnostic(Severity.INFO, channel, subject, source, detail, null);
    }

    public static ExternalRegistrationDiagnostic warn(String channel, String subject, SourceInfo source, String detail) {
        return new ExternalRegistrationDiagnostic(Severity.WARN, channel, subject, source, detail, null);
    }

    public static ExternalRegistrationDiagnostic error(String channel, String subject, SourceInfo source, String detail,
        Throwable cause) {
        return new ExternalRegistrationDiagnostic(Severity.ERROR, channel, subject, source, detail, cause);
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        switch (severity) {
            case INFO:
                builder.append("External ponder ");
                break;
            case WARN:
                builder.append("Skipping external ponder ");
                break;
            case ERROR:
                builder.append("Failed to register external ponder ");
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
