package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.util.ResourceLocation;

final class ShowcaseGroupHelper {

    private ShowcaseGroupHelper() {
    }

    @Nullable
    static ShowcaseGroupModel computeFor(@Nullable ResourceLocation componentId) {
        if (componentId == null) {
            return null;
        }

        PonderTag bestTag = null;
        List<ResourceLocation> bestComponents = Collections.emptyList();
        for (PonderTag tag : PonderIndex.getTagAccess().getTags(componentId)) {
            if (!isShowcaseGroupCandidate(tag)) {
                continue;
            }

            Set<ResourceLocation> groupedComponents = PonderIndex.getTagAccess().getItems(tag);
            if (groupedComponents.size() <= 1) {
                continue;
            }

            List<ResourceLocation> sortedComponents = new ArrayList<ResourceLocation>(groupedComponents);
            Collections.sort(sortedComponents, new Comparator<ResourceLocation>() {
                @Override
                public int compare(ResourceLocation left, ResourceLocation right) {
                    boolean leftSelected = left.equals(componentId);
                    boolean rightSelected = right.equals(componentId);
                    if (leftSelected != rightSelected) {
                        return leftSelected ? -1 : 1;
                    }
                    return left.toString().compareTo(right.toString());
                }
            });

            if (bestTag == null || sortedComponents.size() > bestComponents.size()) {
                bestTag = tag;
                bestComponents = sortedComponents;
            }
        }

        return bestTag == null ? null : new ShowcaseGroupModel(bestTag, bestComponents);
    }

    static boolean hasChoices(@Nullable ResourceLocation componentId) {
        ShowcaseGroupModel model = computeFor(componentId);
        return hasChoices(model);
    }

    static boolean hasChoices(@Nullable ShowcaseGroupModel model) {
        return model != null && model.size() > 1;
    }

    static boolean isShowcaseGroupCandidate(PonderTag tag) {
        if (tag == null || tag.getId() == null) {
            return false;
        }
        ResourceLocation id = tag.getId();
        return !PonderTag.Highlight.ALL.equals(id)
            && !("ponder".equals(id.getNamespace()) && "not_registered".equals(id.getPath()));
    }
}
