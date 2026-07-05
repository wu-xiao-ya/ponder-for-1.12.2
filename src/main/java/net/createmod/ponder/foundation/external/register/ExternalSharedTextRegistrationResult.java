package net.createmod.ponder.foundation.external.register;

public record ExternalSharedTextRegistrationResult(
    RegistrationOutcome registrationOutcome,
    RegistrationDiagnosticReport diagnosticReport
) {
    public static final ExternalSharedTextRegistrationResult EMPTY =
        new ExternalSharedTextRegistrationResult(RegistrationOutcome.EMPTY, RegistrationDiagnosticReport.EMPTY);

    public ExternalSharedTextRegistrationResult {
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
        diagnosticReport = diagnosticReport == null ? RegistrationDiagnosticReport.EMPTY : diagnosticReport;
    }
}
