package net.createmod.ponder.foundation.content;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public final class TestScenes {

    private TestScenes() {
    }

    public static void registerAll(PonderSceneRegistrationHelper<ResourceLocation> helper,
        ResourceLocation componentId, ResourceLocation debugTag) {
        helper.addStoryBoard(componentId, "debug/runtime_test", TestScenes::runtimeTestScene, debugTag);
        helper.addStoryBoard(componentId, "debug/scene_8", TestScenes::visualShowcaseScene, debugTag);
    }

    public static void visualShowcaseScene(SceneBuilder scene, SceneBuildingUtil util) {
        Selection floor = util.select().fromTo(0, 0, 0, 6, 0, 6);
        Selection floorInset = util.select().fromTo(1, 0, 1, 5, 0, 5);
        scene.title("debug_visual_showcase", "视觉演示测试");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.68F);
        scene.world().setBlocks(floor, Blocks.STONEBRICK.getDefaultState(), true);
        scene.world().setBlocks(floorInset, Blocks.STONE.getDefaultState(), true);
        scene.showBasePlate();
        scene.idle(8);

        scene.overlay().showText(50)
            .colored(PonderPalette.WHITE)
            .independent(18)
            .text("这个场景会用清晰、均匀的步骤搭建，方便观察展示效果。");
        scene.idle(12);

        for (int z = 0; z <= 6; z++) {
            Selection row = util.select().fromTo(0, 1, z, 6, 1, z);
            scene.world().setBlocks(row, z % 2 == 0 ? Blocks.STONEBRICK.getDefaultState() : Blocks.STONE.getDefaultState(),
                true);
            scene.idle(4);
        }

        Selection[] shellSides = new Selection[] {
            util.select().fromTo(0, 2, 0, 6, 4, 0),
            util.select().fromTo(6, 2, 1, 6, 4, 6),
            util.select().fromTo(0, 2, 6, 5, 4, 6),
            util.select().fromTo(0, 2, 1, 0, 4, 5)
        };
        net.minecraft.block.state.IBlockState[] shellStates = new net.minecraft.block.state.IBlockState[] {
            Blocks.REDSTONE_BLOCK.getDefaultState(),
            Blocks.GOLD_BLOCK.getDefaultState(),
            Blocks.LAPIS_BLOCK.getDefaultState(),
            Blocks.EMERALD_BLOCK.getDefaultState()
        };

        scene.addKeyframe();
        for (int i = 0; i < shellSides.length; i++) {
            scene.world().setBlocks(shellSides[i], shellStates[i], true);
            scene.idle(8);
        }

        Selection core = util.select().fromTo(2, 2, 2, 4, 4, 4);
        Selection[] coreLayers = new Selection[] {
            util.select().fromTo(2, 2, 2, 4, 2, 4),
            util.select().fromTo(2, 3, 2, 4, 3, 4),
            util.select().fromTo(2, 4, 2, 4, 4, 4)
        };
        net.minecraft.block.state.IBlockState[] coreStates = new net.minecraft.block.state.IBlockState[] {
            Blocks.IRON_BLOCK.getDefaultState(),
            Blocks.DIAMOND_BLOCK.getDefaultState(),
            Blocks.QUARTZ_BLOCK.getDefaultState()
        };

        scene.overlay().showOutlineWithText(core, 50)
            .colored(PonderPalette.BLUE)
            .placeNearTarget()
            .text("核心结构会一层一层升起。");
        for (int i = 0; i < coreLayers.length; i++) {
            scene.world().setBlocks(coreLayers[i], coreStates[i], true);
            scene.idle(8);
        }

        Selection roofRing = util.select().fromTo(1, 5, 1, 5, 5, 5)
            .substract(util.select().fromTo(2, 5, 2, 4, 5, 4));
        scene.world().setBlocks(roofRing, Blocks.GLOWSTONE.getDefaultState(), true);
        scene.idle(10);

        for (int y = 2; y <= 6; y++) {
            scene.world().setBlock(util.grid().at(3, y, 3),
                y == 6 ? Blocks.SEA_LANTERN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState(), true);
            scene.idle(6);
        }

        scene.overlay().showText(40)
            .colored(PonderPalette.RED)
            .pointAt(util.vector().topOf(util.grid().at(3, 6, 3)))
            .placeNearTarget()
            .text("现在中心光源会被逐步破坏。");
        scene.idle(6);
        for (int i = 0; i < 8; i++) {
            scene.world().incrementBlockBreakingProgress(util.grid().at(3, 6, 3));
            scene.idle(4);
        }

        scene.world().destroyBlock(util.grid().at(3, 6, 3));
        scene.idle(10);

        Selection[] crossSlices = new Selection[] {
            util.select().fromTo(1, 3, 3, 2, 3, 3),
            util.select().fromTo(3, 3, 1, 3, 3, 2),
            util.select().fromTo(4, 3, 3, 5, 3, 3),
            util.select().fromTo(3, 3, 4, 3, 3, 5)
        };

        for (Selection slice : crossSlices) {
            scene.world().setBlocks(slice, Blocks.QUARTZ_BLOCK.getDefaultState(), true);
            scene.idle(6);
        }

        scene.world().setBlock(util.grid().at(3, 6, 3), Blocks.GLOWSTONE.getDefaultState(), true);
        scene.idle(12);
        scene.markAsFinished();
    }

    public static void runtimeTestScene(SceneBuilder scene, SceneBuildingUtil util) {
        Selection floor = util.select().fromTo(0, 0, 0, 4, 0, 4);
        Selection floorInset = util.select().fromTo(1, 0, 1, 3, 0, 3);
        Selection pillar = util.select().fromTo(1, 1, 1, 1, 3, 1);
        Selection platform = util.select().fromTo(2, 1, 1, 3, 1, 2);
        Selection restoreArea = util.select().fromTo(1, 1, 1, 3, 1, 2);

        scene.title("debug_runtime_test", "动态效果测试");
        scene.configureBasePlate(0, 0, 5);
        scene.world().setBlocks(floor, Blocks.STONEBRICK.getDefaultState(), true);
        scene.world().setBlocks(floorInset, Blocks.PLANKS.getDefaultState(), true);
        scene.showBasePlate();
        scene.scaleSceneView(0.8F);
        scene.idle(10);

        scene.world().showSection(pillar, EnumFacing.DOWN);
        scene.idle(16);
        scene.world().showSection(platform, EnumFacing.WEST);
        scene.idle(18);

        scene.overlay().showText(60)
            .colored(PonderPalette.WHITE)
            .pointAt(util.vector().topOf(util.grid().at(2, 1, 1)))
            .placeNearTarget()
            .text("现在的分段显隐和镜头旋转，应该已经更接近高版本 Ponder。");
        scene.rotateCameraY(75);
        scene.idle(24);

        scene.overlay().showOutlineWithText(platform, 50)
            .colored(PonderPalette.BLUE)
            .placeNearTarget()
            .text("大范围选区应当像一个完整分段那样平滑滑入。");
        scene.idle(14);

        scene.addKeyframe();
        scene.world().replaceBlocks(platform, Blocks.QUARTZ_BLOCK.getDefaultState(), true);
        scene.idle(10);

        scene.world().setBlock(util.grid().at(4, 1, 4), Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.idle(10);

        scene.world().setBlocks(util.select().fromTo(0, 1, 4, 1, 1, 4), Blocks.REDSTONE_BLOCK.getDefaultState(), true);
        scene.idle(10);

        for (int i = 0; i < 5; i++) {
            scene.world().incrementBlockBreakingProgress(util.grid().at(4, 1, 4));
            scene.idle(6);
        }

        scene.world().destroyBlock(util.grid().at(4, 1, 4));
        scene.idle(12);

        scene.rotateCameraY(-110);
        scene.idle(26);

        scene.world().hideSection(pillar, EnumFacing.UP);
        scene.idle(18);
        scene.world().showSection(pillar, EnumFacing.DOWN);
        scene.idle(18);

        scene.world().hideSection(platform, EnumFacing.SOUTH);
        scene.idle(18);
        scene.world().restoreBlocks(restoreArea);
        scene.overlay().showOutlineWithText(restoreArea, 60)
            .colored(PonderPalette.GREEN)
            .placeNearTarget()
            .text("restoreBlocks 应当在动画结束后重新填充预览。");
        scene.rotateCameraY(35);
        scene.idle(24);
        scene.markAsFinished();
    }
}
