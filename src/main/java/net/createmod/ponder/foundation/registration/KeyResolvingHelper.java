package net.createmod.ponder.foundation.registration;

import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

abstract class KeyResolvingHelper<T, D> extends PonderRegistrationHelperSupport<T> {

    protected final D helperDelegate;

    protected KeyResolvingHelper(D helperDelegate, Function<T, ResourceLocation> keyGen) {
        super("", keyGen);
        this.helperDelegate = helperDelegate;
    }
}
