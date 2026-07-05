package net.createmod.ponder.foundation.external;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.PonderReloadDetails;
import net.createmod.ponder.foundation.PonderReloadDetailsProvider;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.parse.ExternalPonderSceneParser;
import net.createmod.ponder.foundation.external.parse.ExternalParseResult;
import net.createmod.ponder.foundation.external.register.ExternalPonderRegistrationService;
import net.createmod.ponder.foundation.external.register.ExternalSceneRegistrationResult;
import net.createmod.ponder.foundation.external.register.ExternalSharedTextRegistrationResult;
import net.createmod.ponder.foundation.external.register.ExternalTagRegistrationResult;
import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.createmod.ponder.foundation.external.validate.ExternalValidationDiagnostics;
import net.createmod.ponder.foundation.external.validate.ValidationReport;
import net.minecraft.util.ResourceLocation;

public class ExternalPonderPlugin implements PonderPlugin, PonderReloadDetailsProvider {

    private ExternalParseResult cachedParseResult;
    private boolean validationReportLogged;
    private PonderReloadDetails reloadDetails = PonderReloadDetails.EMPTY;

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        resetReloadDetails();
        clearCachedParseResult();
        ExternalSceneRegistrationResult result =
            ExternalPonderRegistrationService.registerLoadedScenesResult(loadDefinitions(), helper);
        reloadDetails = reloadDetails.merge(new PonderReloadDetails(null, result.compileSummary(),
            result.compileFailureReport(), result.diagnosticReport(), result.registrationOutcome(), null, null));
        logOutcome("scenes", result.registrationOutcome());
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ExternalTagRegistrationResult result =
            ExternalPonderRegistrationService.registerLoadedTagsResult(loadDefinitions(), helper);
        reloadDetails = reloadDetails.merge(new PonderReloadDetails(null, null, null,
            result.diagnosticReport(), null, result.registrationOutcome(), null));
        logOutcome("tags", result.registrationOutcome());
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        try {
            ExternalSharedTextRegistrationResult result =
                ExternalPonderRegistrationService.registerLoadedSharedTextResult(loadDefinitions(), helper);
            reloadDetails = reloadDetails.merge(new PonderReloadDetails(null, null, null,
                result.diagnosticReport(), null, null, result.registrationOutcome()));
            logOutcome("shared text", result.registrationOutcome());
        } finally {
            clearCachedParseResult();
        }
    }

    @Override
    public PonderReloadDetails collectReloadDetails() {
        return reloadDetails;
    }

    private ExternalDefinitionSet loadDefinitions() {
        return loadResult().definitions();
    }

    private ExternalParseResult loadResult() {
        if (cachedParseResult == null) {
            ExternalValidationDiagnostics diagnostics = new ExternalValidationDiagnostics();
            cachedParseResult = ExternalPonderSceneParser.loadResult(diagnostics);
            recordValidationReport(cachedParseResult.validationReport());
            logValidationReport();
        }
        return cachedParseResult;
    }

    private void recordValidationReport(ValidationReport validationReport) {
        reloadDetails =
            reloadDetails.merge(new PonderReloadDetails(validationReport, null, null, null, null, null, null));
    }

    private void resetReloadDetails() {
        reloadDetails = PonderReloadDetails.EMPTY;
    }

    private void clearCachedParseResult() {
        cachedParseResult = null;
        validationReportLogged = false;
    }

    private void logValidationReport() {
        ExternalParseResult parseResult = cachedParseResult;
        if (validationReportLogged || parseResult == null) {
            return;
        }
        validationReportLogged = true;
        Ponder.LOGGER.info("External ponder validation: {} scanned, {} loaded, {} failed, {} warnings, {} errors",
            Integer.valueOf(parseResult.validationReport().filesScanned()),
            Integer.valueOf(parseResult.validationReport().filesLoaded()),
            Integer.valueOf(parseResult.validationReport().filesFailed()),
            Integer.valueOf(parseResult.validationReport().warnings()),
            Integer.valueOf(parseResult.validationReport().errors()));
    }

    private void logOutcome(String channel, RegistrationOutcome outcome) {
        if (outcome == null) {
            return;
        }
        Ponder.LOGGER.info("External ponder {} registration: {} registered, {} skipped, {} failed",
            channel, Integer.valueOf(outcome.registered()), Integer.valueOf(outcome.skipped()),
            Integer.valueOf(outcome.failed()));
    }
}
