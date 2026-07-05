package net.createmod.ponder.foundation.external;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.parse.ExternalPonderSceneParser;
import net.createmod.ponder.foundation.external.register.ExternalPonderRegistrationService;
import net.createmod.ponder.foundation.external.register.ExternalSharedTextRegistrationService;
import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.createmod.ponder.foundation.external.validate.ExternalValidationDiagnostics;
import net.createmod.ponder.foundation.external.validate.ValidationReport;
import net.minecraft.util.ResourceLocation;

public class ExternalPonderPlugin implements PonderPlugin {

    private ExternalDefinitionSet cachedDefinitions;
    private ValidationReport cachedValidationReport = ValidationReport.EMPTY;
    private boolean validationReportLogged;

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        clearCachedDefinitions();
        logOutcome("scenes", ExternalPonderRegistrationService.registerLoadedScenes(loadDefinitions(), helper));
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        logOutcome("tags", ExternalPonderRegistrationService.registerLoadedTags(loadDefinitions(), helper));
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        try {
            logOutcome("shared text",
                ExternalSharedTextRegistrationService.registerLoadedSharedText(loadDefinitions(), helper));
        } finally {
            clearCachedDefinitions();
        }
    }

    private ExternalDefinitionSet loadDefinitions() {
        if (cachedDefinitions == null) {
            ExternalValidationDiagnostics diagnostics = new ExternalValidationDiagnostics();
            cachedDefinitions = ExternalPonderSceneParser.loadDefinitions(diagnostics);
            cachedValidationReport = diagnostics.report();
            logValidationReport();
        }
        return cachedDefinitions;
    }

    private void clearCachedDefinitions() {
        cachedDefinitions = null;
        cachedValidationReport = ValidationReport.EMPTY;
        validationReportLogged = false;
    }

    private void logValidationReport() {
        if (validationReportLogged || cachedValidationReport == null) {
            return;
        }
        validationReportLogged = true;
        Ponder.LOGGER.info("External ponder validation: {} scanned, {} loaded, {} failed, {} warnings, {} errors",
            Integer.valueOf(cachedValidationReport.filesScanned()), Integer.valueOf(cachedValidationReport.filesLoaded()),
            Integer.valueOf(cachedValidationReport.filesFailed()), Integer.valueOf(cachedValidationReport.warnings()),
            Integer.valueOf(cachedValidationReport.errors()));
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
