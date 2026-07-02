package net.createmod.ponder.foundation.external.register;

/**
 * Captures the result of a batch registration operation.
 *
 * Tracks how many items were registered, skipped (e.g. duplicates),
 * and failed.  Instances are immutable and can be merged to produce
 * an aggregate result across multiple operations.
 */
public record RegistrationOutcome(int registered, int skipped, int failed) {

    public static final RegistrationOutcome EMPTY = new RegistrationOutcome(0, 0, 0);

    public RegistrationOutcome merge(RegistrationOutcome other) {
        return new RegistrationOutcome(
            this.registered + other.registered,
            this.skipped + other.skipped,
            this.failed + other.failed
        );
    }
}
