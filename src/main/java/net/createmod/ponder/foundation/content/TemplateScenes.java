package net.createmod.ponder.foundation.content;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.util.EnumFacing;

public class TemplateScenes {

    public static void templateMethod(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("crafting_table", "Introducing the Cleanroom backport");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), EnumFacing.UP);
        scene.idle(5);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);
        scene.markAsFinished();
    }
}
