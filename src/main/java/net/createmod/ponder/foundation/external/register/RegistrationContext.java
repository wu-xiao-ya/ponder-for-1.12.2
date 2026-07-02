package net.createmod.ponder.foundation.external.register;

import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SourceInfo;

/**
 * Context that bundles the external definition set with convenience
 * accessors for namespace and source-path resolution.
 *
 * This is the first step toward a command/result/context pattern for
 * external registration: it centralises cross-cutting derivation logic
 * so individual registration commands do not repeat namespace or path
 * extraction.
 */
public record RegistrationContext(ExternalDefinitionSet definitions) {

    public static RegistrationContext of(ExternalDefinitionSet definitions) {
        return new RegistrationContext(definitions);
    }

    public SourceInfo source(SourceInfo source) {
        return source != null ? source : SourceInfo.EMPTY;
    }

    public String namespace(SourceInfo source) {
        return source(source).namespace();
    }

    public String sourcePath(SourceInfo source) {
        return source(source).sourcePath();
    }
}
