package net.createmod.ponder.foundation.registration;

import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

abstract class PonderRegistrationHelperSupport<T> {

    protected final String namespace;
    protected final Function<T, ResourceLocation> keyGen;

    protected PonderRegistrationHelperSupport(String namespace, Function<T, ResourceLocation> keyGen) {
        this.namespace = namespace;
        this.keyGen = keyGen;
    }

    public ResourceLocation asLocation(String path) {
        return new ResourceLocation(namespace, path);
    }

    protected ResourceLocation resolveKey(T component) {
        return keyGen.apply(component);
    }
}
