package net.createmod.ponder.foundation.external.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ExternalSceneCompileFailureReport(List<ExternalSceneCompileFailureDiagnostic> failures) {

    public static final ExternalSceneCompileFailureReport EMPTY =
        new ExternalSceneCompileFailureReport(Collections.emptyList());

    public ExternalSceneCompileFailureReport {
        if (failures == null || failures.isEmpty()) {
            failures = Collections.emptyList();
        } else {
            failures = Collections.unmodifiableList(new ArrayList<ExternalSceneCompileFailureDiagnostic>(failures));
        }
    }

    public ExternalSceneCompileFailureReport merge(ExternalSceneCompileFailureReport other) {
        if (other == null || other.failures.isEmpty()) {
            return this;
        }
        if (failures.isEmpty()) {
            return other;
        }

        List<ExternalSceneCompileFailureDiagnostic> merged =
            new ArrayList<ExternalSceneCompileFailureDiagnostic>(failures);
        merged.addAll(other.failures);
        return new ExternalSceneCompileFailureReport(merged);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    public int failureCount() {
        return failures.size();
    }
}
