package net.createmod.ponder.foundation.external.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ExternalRegistrationFailureReport(List<ExternalRegistrationDiagnostic> failures) {

    public static final ExternalRegistrationFailureReport EMPTY =
        new ExternalRegistrationFailureReport(Collections.emptyList());

    public ExternalRegistrationFailureReport {
        if (failures == null || failures.isEmpty()) {
            failures = Collections.emptyList();
        } else {
            failures = Collections.unmodifiableList(new ArrayList<ExternalRegistrationDiagnostic>(failures));
        }
    }

    public ExternalRegistrationFailureReport merge(ExternalRegistrationFailureReport other) {
        if (other == null || other.failures.isEmpty()) {
            return this;
        }
        if (failures.isEmpty()) {
            return other;
        }

        List<ExternalRegistrationDiagnostic> merged =
            new ArrayList<ExternalRegistrationDiagnostic>(failures);
        merged.addAll(other.failures);
        return new ExternalRegistrationFailureReport(merged);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    public int failureCount() {
        return failures.size();
    }
}
