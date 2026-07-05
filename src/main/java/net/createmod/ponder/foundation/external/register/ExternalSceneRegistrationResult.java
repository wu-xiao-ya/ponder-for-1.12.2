package net.createmod.ponder.foundation.external.register;

public record ExternalSceneRegistrationResult(
    ExternalSceneCompileSummary compileSummary,
    RegistrationOutcome registrationOutcome
) {
    public static final ExternalSceneRegistrationResult EMPTY =
        new ExternalSceneRegistrationResult(ExternalSceneCompileSummary.EMPTY, RegistrationOutcome.EMPTY);

    public ExternalSceneRegistrationResult {
        compileSummary = compileSummary == null ? ExternalSceneCompileSummary.EMPTY : compileSummary;
        registrationOutcome = registrationOutcome == null ? RegistrationOutcome.EMPTY : registrationOutcome;
    }
}
