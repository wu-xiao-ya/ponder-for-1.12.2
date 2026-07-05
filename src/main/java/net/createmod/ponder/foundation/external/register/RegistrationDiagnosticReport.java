package net.createmod.ponder.foundation.external.register;

public record RegistrationDiagnosticReport(int infos, int warnings, int errors) {

    public static final RegistrationDiagnosticReport EMPTY = new RegistrationDiagnosticReport(0, 0, 0);

    public RegistrationDiagnosticReport {
        infos = Math.max(0, infos);
        warnings = Math.max(0, warnings);
        errors = Math.max(0, errors);
    }

    public RegistrationDiagnosticReport merge(RegistrationDiagnosticReport other) {
        if (other == null) {
            return this;
        }
        return new RegistrationDiagnosticReport(infos + other.infos, warnings + other.warnings,
            errors + other.errors);
    }

    public boolean hasDiagnostics() {
        return !equals(EMPTY);
    }

    public String formatSummary() {
        return infos + " info, " + warnings + " warning(s), " + errors + " error(s)";
    }
}
