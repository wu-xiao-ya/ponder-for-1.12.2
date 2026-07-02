package net.createmod.ponder.foundation.registration;

abstract class AbstractPonderRegistry {

    private boolean allowRegistration = true;

    protected final void ensureRegistrationOpen() {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }
    }

    protected final void resetRegistrationPhase() {
        allowRegistration = true;
    }

    public final void finishRegistration() {
        allowRegistration = false;
    }

    public final boolean isRegistrationOpen() {
        return allowRegistration;
    }
}
