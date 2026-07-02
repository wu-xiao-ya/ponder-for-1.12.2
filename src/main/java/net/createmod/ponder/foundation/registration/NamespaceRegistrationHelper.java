package net.createmod.ponder.foundation.registration;

import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

abstract class NamespaceRegistrationHelper extends PonderRegistrationHelperSupport<ResourceLocation> {

    protected NamespaceRegistrationHelper(String namespace, Function<ResourceLocation, ResourceLocation> keyGen) {
        super(namespace, keyGen);
    }
}
