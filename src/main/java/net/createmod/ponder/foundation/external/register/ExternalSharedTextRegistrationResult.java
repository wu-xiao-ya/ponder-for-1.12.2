package net.createmod.ponder.foundation.external.register;

public record ExternalSharedTextRegistrationResult(
    RegistrationOutcome registrationOutcome,
    RegistrationDiagnosticReport diagnosticReport,
    ExternalRegistrationFailureReport failureReport
) {
    public static final ExternalSharedTextRegistrationResult EMPTY =
        new ExternalSharedTextRegistrationResult(RegistrationOutcome.EMPTY, RegistrationDiagnosticReport.EMPTY,
            ExternalRegistrationFailureReport.EMPTY);

    public ExternalSharedTextRegistrationResult(RegistrationOutcome registrationOutcome,
        RegistrationDiagnosticReport diagnosticReport) {
        this(registrationOutcome, diagnosticReport, ExternalRegistrationFailureReport.EMPTY);
    }

    public ExternalSharedTextRegistrationResult {
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
        diagnosticReport = diagnosticReport == null ? RegistrationDiagnosticReport.EMPTY : diagnosticReport;
        failureReport = failureReport == null ? ExternalRegistrationFailureReport.EMPTY : failureReport;
    }
}
