package net.createmod.ponder.foundation.external;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.parse.ExternalPonderSceneParser;
import net.createmod.ponder.foundation.external.parse.ExternalParseResult;
import net.createmod.ponder.foundation.external.register.ExternalPonderRegistrationService;
import net.createmod.ponder.foundation.external.register.ExternalSharedTextRegistrationService;
import net.createmod.ponder.foundation.external.register.RegistrationOutcome;
import net.createmod.ponder.foundation.external.validate.ExternalValidationDiagnostics;
import net.minecraft.util.ResourceLocation;

public class ExternalPonderPlugin implements PonderPlugin {

    private ExternalParseResult cachedParseResult;
    private boolean validationReportLogged;

    @Override
    public String getModId() {
        return Ponder.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        clearCachedParseResult();
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
            clearCachedParseResult();
        }
    }

    private ExternalDefinitionSet loadDefinitions() {
        return loadResult().definitions();
    }

    private ExternalParseResult loadResult() {
        if (cachedParseResult == null) {
            ExternalValidationDiagnostics diagnostics = new ExternalValidationDiagnostics();
            cachedParseResult = ExternalPonderSceneParser.loadResult(diagnostics);
            logValidationReport();
        }
        return cachedParseResult;
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
