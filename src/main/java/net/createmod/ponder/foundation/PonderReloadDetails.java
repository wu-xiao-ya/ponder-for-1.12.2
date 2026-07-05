package net.createmod.ponder.foundation;

import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.createmod.ponder.foundation.external.register.ExternalSceneCompileSummary;
import net.createmod.ponder.foundation.external.register.ExternalSceneCompileFailureDiagnostic;
import net.createmod.ponder.foundation.external.register.ExternalSceneCompileFailureReport;
import net.createmod.ponder.foundation.external.register.ExternalRegistrationFailureReport;
import net.createmod.ponder.foundation.external.register.RegistrationDiagnosticReport;
import net.createmod.ponder.foundation.external.validate.ValidationReport;

public record PonderReloadDetails(ValidationReport validationReport, ExternalSceneCompileSummary sceneCompileSummary,
    ExternalSceneCompileFailureReport sceneCompileFailures, RegistrationDiagnosticReport registrationDiagnostics,
    ExternalRegistrationFailureReport tagFailureReport, ExternalRegistrationFailureReport sharedTextFailureReport,
    RegistrationOutcome sceneOutcome, RegistrationOutcome tagOutcome, RegistrationOutcome sharedTextOutcome) {

    public static final PonderReloadDetails EMPTY =
        new PonderReloadDetails(ValidationReport.EMPTY, ExternalSceneCompileSummary.EMPTY,
            ExternalSceneCompileFailureReport.EMPTY, RegistrationDiagnosticReport.EMPTY,
            ExternalRegistrationFailureReport.EMPTY, ExternalRegistrationFailureReport.EMPTY,
            RegistrationOutcome.EMPTY, RegistrationOutcome.EMPTY, RegistrationOutcome.EMPTY);

    public PonderReloadDetails {
        validationReport = validationReport == null ? ValidationReport.EMPTY : validationReport;
        sceneCompileSummary =
            sceneCompileSummary == null ? ExternalSceneCompileSummary.EMPTY : sceneCompileSummary;
        sceneCompileFailures =
            sceneCompileFailures == null ? ExternalSceneCompileFailureReport.EMPTY : sceneCompileFailures;
        registrationDiagnostics =
            registrationDiagnostics == null ? RegistrationDiagnosticReport.EMPTY : registrationDiagnostics;
        tagFailureReport =
            tagFailureReport == null ? ExternalRegistrationFailureReport.EMPTY : tagFailureReport;
        sharedTextFailureReport =
            sharedTextFailureReport == null ? ExternalRegistrationFailureReport.EMPTY : sharedTextFailureReport;
        sceneOutcome = sceneOutcome == null ? RegistrationOutcome.EMPTY : sceneOutcome;
        tagOutcome = tagOutcome == null ? RegistrationOutcome.EMPTY : tagOutcome;
        sharedTextOutcome = sharedTextOutcome == null ? RegistrationOutcome.EMPTY : sharedTextOutcome;
    }

    public PonderReloadDetails merge(PonderReloadDetails other) {
        if (other == null) {
            return this;
        }
        return new PonderReloadDetails(validationReport.merge(other.validationReport),
            sceneCompileSummary.merge(other.sceneCompileSummary),
            sceneCompileFailures.merge(other.sceneCompileFailures),
            registrationDiagnostics.merge(other.registrationDiagnostics),
            tagFailureReport.merge(other.tagFailureReport),
            sharedTextFailureReport.merge(other.sharedTextFailureReport), sceneOutcome.merge(other.sceneOutcome),
            tagOutcome.merge(other.tagOutcome),
            sharedTextOutcome.merge(other.sharedTextOutcome));
    }

    public boolean hasDetails() {
        return hasCompileSummary() || hasCompileFailureDiagnostics() || hasExternalDetails();
    }

    public boolean hasCompileSummary() {
        return sceneCompileSummary.hasSummary();
    }

    public boolean hasCompileFailureDiagnostics() {
        return sceneCompileFailures.hasFailures();
    }

    public boolean hasExternalDetails() {
        return hasExternalSummary() || tagFailureReport.hasFailures() || sharedTextFailureReport.hasFailures();
    }

    public boolean hasExternalSummary() {
        return !validationReport.equals(ValidationReport.EMPTY) || registrationDiagnostics.hasDiagnostics()
            || !sceneOutcome.equals(RegistrationOutcome.EMPTY) || !tagOutcome.equals(RegistrationOutcome.EMPTY)
            || !sharedTextOutcome.equals(RegistrationOutcome.EMPTY);
    }

    public String formatCompileSummary() {
        return sceneCompileSummary.formatSummary();
    }

    public String formatCompileFailureSummary() {
        return "Compile failures (" + sceneCompileFailures.failureCount() + "):";
    }

    public Iterable<ExternalSceneCompileFailureDiagnostic> compileFailureDiagnostics() {
        return sceneCompileFailures.failures();
    }

    public boolean hasTagFailureDiagnostics() {
        return tagFailureReport.hasFailures();
    }

    public boolean hasSharedTextFailureDiagnostics() {
        return sharedTextFailureReport.hasFailures();
    }

    public String formatTagFailureSummary() {
        return "Tag registration failures (" + tagFailureReport.failureCount() + "):";
    }

    public String formatSharedTextFailureSummary() {
        return "Shared text registration failures (" + sharedTextFailureReport.failureCount() + "):";
    }

    public String formatSummary() {
        StringBuilder builder = new StringBuilder();
        appendValidationSummary(builder);
        appendRegistrationDiagnosticSummary(builder);
        appendOutcomeSummary(builder, "external scene registration", sceneOutcome);
        appendOutcomeSummary(builder, "external tag registration", tagOutcome);
        appendOutcomeSummary(builder, "external shared text registration", sharedTextOutcome);
        return builder.length() == 0 ? "no reload details" : builder.toString();
    }

    private void appendValidationSummary(StringBuilder builder) {
        if (validationReport.equals(ValidationReport.EMPTY)) {
            return;
        }
        appendPart(builder, "validation: " + validationReport.filesScanned() + " scanned, "
            + validationReport.filesLoaded() + " loaded, " + validationReport.filesFailed() + " failed, "
            + validationReport.warnings() + " warnings, " + validationReport.errors() + " errors");
    }

    private void appendRegistrationDiagnosticSummary(StringBuilder builder) {
        if (!registrationDiagnostics.hasDiagnostics()) {
            return;
        }
        appendPart(builder, "registration diagnostics: " + registrationDiagnostics.formatSummary());
    }

    private void appendOutcomeSummary(StringBuilder builder, String label, RegistrationOutcome outcome) {
        if (outcome.equals(RegistrationOutcome.EMPTY)) {
            return;
        }
        appendPart(builder, label + ": " + outcome.registered() + " registered, " + outcome.skipped()
            + " skipped, " + outcome.failed() + " failed");
    }

    private void appendPart(StringBuilder builder, String text) {
        if (builder.length() > 0) {
            builder.append("; ");
        }
        builder.append(text);
    }
}
