package net.createmod.ponder.foundation;

import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.createmod.ponder.foundation.external.register.ExternalSceneCompileSummary;
import net.createmod.ponder.foundation.external.validate.ValidationReport;

public record PonderReloadDetails(ValidationReport validationReport, ExternalSceneCompileSummary sceneCompileSummary,
    RegistrationOutcome sceneOutcome, RegistrationOutcome tagOutcome, RegistrationOutcome sharedTextOutcome) {

    public static final PonderReloadDetails EMPTY =
        new PonderReloadDetails(ValidationReport.EMPTY, ExternalSceneCompileSummary.EMPTY, RegistrationOutcome.EMPTY,
            RegistrationOutcome.EMPTY, RegistrationOutcome.EMPTY);

    public PonderReloadDetails {
        validationReport = validationReport == null ? ValidationReport.EMPTY : validationReport;
        sceneCompileSummary =
            sceneCompileSummary == null ? ExternalSceneCompileSummary.EMPTY : sceneCompileSummary;
        sceneOutcome = sceneOutcome == null ? RegistrationOutcome.EMPTY : sceneOutcome;
        tagOutcome = tagOutcome == null ? RegistrationOutcome.EMPTY : tagOutcome;
        sharedTextOutcome = sharedTextOutcome == null ? RegistrationOutcome.EMPTY : sharedTextOutcome;
    }

    public PonderReloadDetails merge(PonderReloadDetails other) {
        if (other == null) {
            return this;
        }
        return new PonderReloadDetails(validationReport.merge(other.validationReport),
            sceneCompileSummary.merge(other.sceneCompileSummary), sceneOutcome.merge(other.sceneOutcome),
            tagOutcome.merge(other.tagOutcome),
            sharedTextOutcome.merge(other.sharedTextOutcome));
    }

    public boolean hasDetails() {
        return hasCompileSummary() || hasExternalDetails();
    }

    public boolean hasCompileSummary() {
        return sceneCompileSummary.hasSummary();
    }

    public boolean hasExternalDetails() {
        return !validationReport.equals(ValidationReport.EMPTY) || !sceneOutcome.equals(RegistrationOutcome.EMPTY)
            || !tagOutcome.equals(RegistrationOutcome.EMPTY) || !sharedTextOutcome.equals(RegistrationOutcome.EMPTY);
    }

    public String formatCompileSummary() {
        return sceneCompileSummary.formatSummary();
    }

    public String formatSummary() {
        StringBuilder builder = new StringBuilder();
        appendValidationSummary(builder);
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
