package net.createmod.ponder.foundation.external.register;

import java.util.LinkedHashSet;
import java.util.Set;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.minecraft.util.ResourceLocation;

final class ExternalComponentTagRegistrar {

    private ExternalComponentTagRegistrar() {
    }

    static int register(ExternalDefinitionSet definitions, PonderTagRegistrationHelper<ResourceLocation> helper) {
        Set<String> assignments = new LinkedHashSet<String>();
        int assignmentCount = 0;
        for (SceneDefinition definition : definitions.scenes()) {
            for (ComponentDefinition component : definition.components()) {
                for (ResourceLocation tag : definition.componentTags()) {
                    String assignmentKey = component.componentId() + "|" + tag;
                    if (assignments.add(assignmentKey)) {
                        helper.addTagToComponent(component.componentId(), tag);
                        assignmentCount++;
                    }
                }
            }
        }
        return assignmentCount;
    }
}
