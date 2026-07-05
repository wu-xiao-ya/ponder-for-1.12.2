package net.createmod.ponder.foundation.external.register;

public record ExternalTagRegistrationResult(
    RegistrationOutcome registrationOutcome,
    RegistrationDiagnosticReport diagnosticReport,
    ExternalRegistrationFailureReport failureReport
) {
    public static final ExternalTagRegistrationResult EMPTY =
        new ExternalTagRegistrationResult(RegistrationOutcome.EMPTY, RegistrationDiagnosticReport.EMPTY,
            ExternalRegistrationFailureReport.EMPTY);

    public ExternalTagRegistrationResult(RegistrationOutcome registrationOutcome,
        RegistrationDiagnosticReport diagnosticReport) {
        this(registrationOutcome, diagnosticReport, ExternalRegistrationFailureReport.EMPTY);
    }

    public ExternalTagRegistrationResult {
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
        diagnosticReport = diagnosticReport == null ? RegistrationDiagnosticReport.EMPTY : diagnosticReport;
        failureReport = failureReport == null ? ExternalRegistrationFailureReport.EMPTY : failureReport;
    }
}
