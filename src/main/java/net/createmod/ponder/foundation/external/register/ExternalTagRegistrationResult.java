package net.createmod.ponder.foundation.external.register;

public record ExternalTagRegistrationResult(
    RegistrationOutcome registrationOutcome,
    RegistrationDiagnosticReport diagnosticReport
) {
    public static final ExternalTagRegistrationResult EMPTY =
        new ExternalTagRegistrationResult(RegistrationOutcome.EMPTY, RegistrationDiagnosticReport.EMPTY);

    public ExternalTagRegistrationResult {
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
        diagnosticReport = diagnosticReport == null ? RegistrationDiagnosticReport.EMPTY : diagnosticReport;
    }
}
