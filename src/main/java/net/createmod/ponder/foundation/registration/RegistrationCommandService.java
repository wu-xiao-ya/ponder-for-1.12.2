package net.createmod.ponder.foundation.registration;

import java.util.Objects;

final class RegistrationCommandService {

    private RegistrationCommandService() {
    }

    static void execute(RegistrationCommands.RegistrationCommand command) {
        Objects.requireNonNull(command, "command").execute();
    }
}
