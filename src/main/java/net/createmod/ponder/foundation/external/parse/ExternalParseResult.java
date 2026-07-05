package net.createmod.ponder.foundation.external.parse;

import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.scan.ExternalScanResult;
import net.createmod.ponder.foundation.external.validate.ValidationReport;

public record ExternalParseResult(
    ExternalDefinitionSet definitions,
    ExternalScanResult scanResult,
    ValidationReport validationReport
) {
    public ExternalParseResult {
        definitions = definitions == null ? ExternalDefinitionSet.EMPTY : definitions;
        scanResult = scanResult == null ? ExternalScanResult.EMPTY : scanResult;
        validationReport = validationReport == null ? ValidationReport.EMPTY : validationReport;
    }
}
