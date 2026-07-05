package net.createmod.ponder.foundation.external.register;

public record ExternalSceneRegistrationResult(
    ExternalSceneCompileSummary compileSummary,
    RegistrationOutcome registrationOutcome,
    RegistrationDiagnosticReport diagnosticReport,
    ExternalSceneCompileFailureReport compileFailureReport
) {
    public static final ExternalSceneRegistrationResult EMPTY =
        new ExternalSceneRegistrationResult(ExternalSceneCompileSummary.EMPTY, RegistrationOutcome.EMPTY,
            RegistrationDiagnosticReport.EMPTY, ExternalSceneCompileFailureReport.EMPTY);

    public ExternalSceneRegistrationResult(ExternalSceneCompileSummary compileSummary,
        RegistrationOutcome registrationOutcome) {
        this(compileSummary, registrationOutcome, RegistrationDiagnosticReport.EMPTY,
            ExternalSceneCompileFailureReport.EMPTY);
    }

    public ExternalSceneRegistrationResult {
        compileSummary = compileSummary == null ? ExternalSceneCompileSummary.EMPTY : compileSummary;
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
        diagnosticReport = diagnosticReport == null ? RegistrationDiagnosticReport.EMPTY : diagnosticReport;
        compileFailureReport =
            compileFailureReport == null ? ExternalSceneCompileFailureReport.EMPTY : compileFailureReport;
    }
}
