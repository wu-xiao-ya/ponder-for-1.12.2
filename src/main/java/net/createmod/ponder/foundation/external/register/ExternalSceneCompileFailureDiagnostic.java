package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.foundation.external.definition.SourceInfo;

public record ExternalSceneCompileFailureDiagnostic(
    String sceneId,
    SourceInfo source,
    String detail,
    String exceptionType,
    String exceptionMessage
) {
    public ExternalSceneCompileFailureDiagnostic {
        if (sceneId == null) {
            sceneId = "";
        }
        if (source == null) {
            source = SourceInfo.EMPTY;
        }
        if (detail == null) {
            detail = "";
        }
        if (exceptionType == null) {
            exceptionType = "";
        }
        if (exceptionMessage == null) {
            exceptionMessage = "";
        } else {
            exceptionMessage = exceptionMessage.replace('\n', ' ').replace('\r', ' ');
        }
    }

    public static ExternalSceneCompileFailureDiagnostic from(String sceneId, SourceInfo source,
        String detail, Throwable cause) {
        return new ExternalSceneCompileFailureDiagnostic(sceneId, source, detail,
            cause == null ? "" : cause.getClass().getSimpleName(), cause == null ? "" : cause.getMessage());
    }

    public String formatSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("scene ");
        builder.append(sceneId.isEmpty() ? "<unknown>" : sceneId);
        builder.append(" @ ");
        builder.append(sourceLabel());
        if (!detail.isEmpty()) {
            builder.append(" - ").append(detail);
        }
        if (!exceptionType.isEmpty()) {
            builder.append(" (").append(exceptionType);
            if (!exceptionMessage.isEmpty()) {
                builder.append(": ").append(exceptionMessage);
            }
            builder.append(")");
        }
        return builder.toString();
    }

    private String sourceLabel() {
        String sourcePath = source.sourcePath() == null ? "" : source.sourcePath();
        String namespace = source.namespace() == null ? "" : source.namespace();
        if (sourcePath.isEmpty()) {
            return namespace;
        }
        return namespace + " @ " + sourcePath;
    }
}
