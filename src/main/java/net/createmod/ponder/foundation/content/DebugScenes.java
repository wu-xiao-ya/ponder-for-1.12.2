package net.createmod.ponder.foundation.content;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DebugScenes {

    private DebugScenes() {
    }

    public static void registerAll(PonderSceneRegistrationHelper<ResourceLocation> helper,
        ResourceLocation componentId, ResourceLocation debugTag) {
        add(helper, componentId, "debug/scene_1", DebugScenes::coordinateScene, debugTag);
        add(helper, componentId, "debug/scene_2", DebugScenes::blocksScene, debugTag);
        add(helper, componentId, "debug/scene_3", DebugScenes::offScreenScene, debugTag);
        add(helper, componentId, "debug/scene_4", DebugScenes::particleScene, debugTag);
        add(helper, componentId, "debug/scene_5", DebugScenes::controlsScene, debugTag);
        add(helper, componentId, "debug/scene_6", DebugScenes::birbScene, debugTag);
        add(helper, componentId, "debug/scene_7", DebugScenes::sectionsScene, debugTag);
        add(helper, componentId, "debug/scene_8", DebugScenes::trackingScene, debugTag);
        add(helper, componentId, "debug/scene_9", DebugScenes::itemScene, debugTag);
        add(helper, componentId, "debug/runtime_test", DebugScenes::guiSnapshotScene, debugTag);
    }

    private static void add(PonderSceneRegistrationHelper<ResourceLocation> helper, ResourceLocation componentId,
        String schematicPath, PonderStoryBoard storyBoard, ResourceLocation debugTag) {
        helper.addStoryBoard(componentId, schematicPath, storyBoard, debugTag);
    }

    public static void coordinateScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_coords", "Coordinate Space");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);

        Selection xAxis = util.select().fromTo(2, 1, 1, 4, 1, 1);
        Selection yAxis = util.select().fromTo(1, 2, 1, 1, 4, 1);
        Selection zAxis = util.select().fromTo(1, 1, 2, 1, 1, 4);

        scene.idle(10);
        scene.overlay().showOutlineWithText(xAxis, 20)
            .colored(PonderPalette.RED)
            .text("Das X axis");
        scene.idle(20);
        scene.overlay().showOutlineWithText(yAxis, 20)
            .colored(PonderPalette.GREEN)
            .text("Das Y axis");
        scene.idle(20);
        scene.overlay().showOutlineWithText(zAxis, 20)
            .colored(PonderPalette.BLUE)
            .text("Das Z axis");
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void blocksScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_blocks", "Changing Blocks");
        scene.showBasePlate();
        scene.scaleSceneView(0.75F);
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);
        scene.overlay().showText(1000)
            .independent(10)
            .text("Blocks can be modified");
        scene.idle(20);
        scene.world().replaceBlocks(util.select().fromTo(1, 1, 3, 2, 2, 4), Blocks.QUARTZ_BLOCK.getDefaultState(),
            true);
        scene.idle(10);
        scene.addKeyframe();
        scene.world().replaceBlocks(util.select().position(3, 1, 1), Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.rotateCameraY(180);

        for (int i = 0; i < 20; i++) {
            scene.world().incrementBlockBreakingProgress(util.grid().at(3, 1, 1));
            scene.idle(10);
        }

        scene.markAsFinished();
    }

    public static void offScreenScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_baseplate", "Out of bounds / configureBasePlate");
        scene.configureBasePlate(1, 0, 6);
        scene.showBasePlate();

        Selection out1 = util.select().fromTo(7, 0, 0, 8, 0, 5);
        Selection out2 = util.select().fromTo(0, 0, 0, 0, 0, 5);
        Selection blocksExceptBasePlate = util.select().layersFrom(1)
            .add(out1)
            .add(out2);

        scene.addKeyframe();
        scene.idle(10);
        scene.world().showSection(blocksExceptBasePlate, EnumFacing.DOWN);
        scene.idle(10);
        scene.addKeyframe();
        scene.idle(20);
        scene.addKeyframe();
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showOutlineWithText(out1, 100)
            .colored(PonderPalette.BLACK)
            .text("Blocks outside of the base plate do not affect scaling");
        scene.overlay().showOutlineWithText(out2, 100)
            .colored(PonderPalette.BLACK)
            .text("configureBasePlate() makes sure of that.");
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void particleScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_particles", "Emitting particles");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);

        Vec3d emitterPos = util.vector().of(2.5D, 2.25D, 2.5D);
        ParticleEmitter flame = scene.effects().simpleParticleEmitter(EnumParticleTypes.FLAME, util.vector().of(0, 0.08D, 0));
        ParticleEmitter portal =
            scene.effects().simpleParticleEmitter(EnumParticleTypes.PORTAL, util.vector().of(0, 0.08D, 0));

        scene.overlay().showText(20)
            .text("Incoming...")
            .pointAt(emitterPos);
        scene.idle(30);
        scene.effects().emitParticles(emitterPos, flame, 1, 60);
        scene.effects().emitParticles(emitterPos, portal, 20, 1);
        scene.idle(30);
        scene.rotateCameraY(180);
        scene.markAsFinished();
    }

    public static void controlsScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_controls", "Basic player interaction");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), EnumFacing.DOWN);
        scene.idle(4);
        scene.world().showSection(util.select().layer(2), EnumFacing.DOWN);
        scene.idle(4);
        scene.world().showSection(util.select().layer(3), EnumFacing.DOWN);
        scene.idle(10);

        BlockPos shaftPos = util.grid().at(3, 1, 1);
        Selection shaftSelection = util.select().position(shaftPos);
        scene.overlay().showControls(util.vector().topOf(shaftPos), Pointing.DOWN, 40)
            .rightClick()
            .whileSneaking()
            .withItem(new ItemStack(Items.REDSTONE));
        scene.idle(20);
        scene.world().replaceBlocks(shaftSelection, Blocks.REDSTONE_BLOCK.getDefaultState(), true);

        scene.idle(20);
        scene.world().hideSection(shaftSelection, EnumFacing.UP);
        scene.idle(20);

        scene.overlay().showControls(util.vector().of(1, 4.5D, 3.5D), Pointing.LEFT, 20)
            .rightClick()
            .withItem(new ItemStack(Blocks.STONE));
        scene.world().showSection(util.select().layer(4), EnumFacing.DOWN);
        scene.idle(40);

        BlockPos chassis = util.grid().at(1, 1, 3);
        Vec3d chassisSurface = util.vector().blockSurface(chassis, EnumFacing.NORTH);

        Object chassisValueBoxHighlight = new Object();
        Object chassisEffectHighlight = new Object();

        AxisAlignedBB point = boxAround(chassisSurface, 0.02D, 0.02D, 0.02D);
        AxisAlignedBB expanded = boxAround(chassisSurface, 0.25D, 0.25D, 0.08D);

        Selection singleBlock = util.select().position(1, 2, 3);
        Selection twoBlocks = util.select().fromTo(1, 2, 3, 1, 3, 3);
        Selection threeBlocks = util.select().fromTo(1, 2, 3, 1, 4, 3);
        Selection singleRow = util.select().fromTo(1, 2, 3, 3, 2, 3);
        Selection twoRows = util.select().fromTo(1, 2, 3, 3, 3, 3);
        Selection threeRows = twoRows.copy().add(threeBlocks);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, point, 1);
        scene.idle(1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, chassisValueBoxHighlight, expanded, 120);
        scene.overlay().showControls(chassisSurface, Pointing.UP, 40)
            .scroll()
            .withItem(new ItemStack(Items.REDSTONE));

        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, singleBlock, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, twoBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, threeBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, twoBlocks, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, singleBlock, 10);
        scene.idle(10);

        scene.idle(30);
        scene.overlay().showControls(chassisSurface, Pointing.UP, 40)
            .whileCTRL()
            .scroll()
            .withItem(new ItemStack(Items.COMPASS));

        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, singleRow, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, twoRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, threeRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, twoRows, 10);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.WHITE, chassisEffectHighlight, singleRow, 10);
        scene.idle(10);
        scene.markAsFinished();
    }

    public static void birbScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_birbs", "Birbs");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);

        BlockPos pos = util.grid().at(1, 2, 3);
        scene.special().createBirb(util.vector().blockSurface(pos, EnumFacing.UP), ParrotPose.FaceCursorPose::new);
        scene.overlay().showText(100)
            .colored(PonderPalette.GREEN)
            .text("More birbs = More interesting")
            .pointAt(util.vector().topOf(pos));

        scene.idle(10);
        scene.special().createBirb(util.vector().topOf(0, 1, 2), ParrotPose.DancePose::new);
        scene.idle(10);

        scene.special().createBirb(offset(util.vector().centerOf(3, 1, 3), 0, 0.25D, 0),
            ParrotPose.FacePointOfInterestPose::new);
        scene.idle(20);

        BlockPos poi1 = util.grid().at(4, 1, 0);
        BlockPos poi2 = util.grid().at(0, 1, 4);

        scene.world().setBlock(poi1, Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.special().movePointOfInterest(poi1);
        scene.idle(20);

        scene.world().setBlock(poi2, Blocks.GOLD_BLOCK.getDefaultState(), true);
        scene.special().movePointOfInterest(poi2);
        scene.overlay().showText(20)
            .text("Point of Interest")
            .pointAt(util.vector().centerOf(poi2));
        scene.idle(20);

        scene.world().destroyBlock(poi1);
        scene.special().movePointOfInterest(poi1);
        scene.idle(20);

        scene.world().destroyBlock(poi2);
        scene.special().movePointOfInterest(poi2);
        scene.markAsFinished();
    }

    public static void sectionsScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_sections", "Sections");
        scene.showBasePlate();
        scene.idle(10);
        scene.rotateCameraY(95);

        BlockPos mergePos = util.grid().at(1, 1, 1);
        BlockPos independentPos = util.grid().at(3, 1, 1);
        Selection toMerge = util.select().position(mergePos);
        Selection independent = util.select().position(independentPos);
        Selection start = util.select().layersFrom(1).substract(toMerge).substract(independent);

        scene.world().showSection(start, EnumFacing.DOWN);
        scene.idle(20);

        scene.world().showSection(toMerge, EnumFacing.DOWN);
        ElementLink<WorldSectionElement> link = scene.world().showIndependentSection(independent, EnumFacing.DOWN);
        scene.idle(20);

        scene.overlay().showText(40)
            .colored(PonderPalette.GREEN)
            .text("This Section got merged to base.")
            .pointAt(util.vector().topOf(mergePos));
        scene.idle(10);
        scene.overlay().showText(40)
            .colored(PonderPalette.RED)
            .text("This Section renders independently.")
            .pointAt(util.vector().topOf(independentPos));

        scene.idle(40);
        scene.world().hideIndependentSection(link, EnumFacing.DOWN);
        scene.world().hideSection(util.select().fromTo(mergePos, util.grid().at(1, 1, 4)), EnumFacing.DOWN);
        scene.idle(20);

        Selection hiddenReplaceArea = util.select().fromTo(2, 1, 2, 4, 1, 4)
            .substract(util.select().position(4, 1, 3))
            .substract(util.select().position(2, 1, 3));

        scene.world().hideSection(hiddenReplaceArea, EnumFacing.UP);
        scene.idle(20);
        scene.world().setBlocks(hiddenReplaceArea, Blocks.OBSIDIAN.getDefaultState(), false);
        scene.world().showSection(hiddenReplaceArea, EnumFacing.DOWN);
        scene.idle(20);
        scene.overlay().showOutlineWithText(hiddenReplaceArea, 30)
            .colored(PonderPalette.BLUE)
            .text("Seamless substitution of blocks");

        scene.idle(40);
        ElementLink<WorldSectionElement> helicopter = scene.world().makeSectionIndependent(hiddenReplaceArea);
        scene.world().rotateSection(helicopter, 50, 5 * 360, 0, 60);
        scene.world().moveSection(helicopter, util.vector().of(0, 4, 5), 50);
        scene.overlay().showText(30)
            .colored(PonderPalette.BLUE)
            .text("Up, up and away.")
            .independent(30);

        scene.idle(40);
        scene.world().hideIndependentSection(helicopter, EnumFacing.UP);
        scene.markAsFinished();
    }

    public static void trackingScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_tracking", "Outlines and POI");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);

        Vec3d birbPos = util.vector().topOf(1, 0, 1);
        scene.special().createBirb(birbPos, ParrotPose.FacePointOfInterestPose::new);

        Object outlineSlot = new Object();
        Vec3d poi1 = util.vector().of(1.5D, 3.0D, 0.5D);
        Vec3d poi2 = util.vector().of(0.5D, 1.25D, 1.5D);
        AxisAlignedBB boundingBox1 = new AxisAlignedBB(1.35D, 0.0D, 0.35D, 1.65D, 2.5D, 0.65D);
        AxisAlignedBB boundingBox2 = new AxisAlignedBB(0.05D, 1.0D, 1.05D, 0.95D, 1.125D, 1.95D);

        for (int i = 0; i < 10; i++) {
            scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, outlineSlot,
                i % 2 == 0 ? boundingBox1 : boundingBox2, 15);
            scene.idle(3);
            scene.special().movePointOfInterest(i % 2 == 0 ? poi1 : poi2);
            scene.idle(12);
        }

        scene.idle(12);
        scene.special().movePointOfInterest(util.vector().of(-4, 5, 4));
        scene.overlay().showText(40)
            .colored(PonderPalette.RED)
            .text("wut?")
            .pointAt(offset(birbPos, -0.25D, 0.25D, 0.25D));
        scene.markAsFinished();
    }

    public static void itemScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_items", "Item entities");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);

        scene.idle(10);
        scene.world().createItemEntity(util.vector().centerOf(1, 3, 2), util.vector().of(0.04D, 0.05D, 0.0D),
            new ItemStack(Items.REDSTONE));
        scene.world().createItemEntity(util.vector().centerOf(2, 3, 2), util.vector().of(0.0D, 0.05D, 0.03D),
            new ItemStack(Items.COMPASS));
        scene.world().createItemEntity(util.vector().centerOf(3, 3, 2), util.vector().of(-0.04D, 0.05D, 0.0D),
            new ItemStack(Blocks.GOLD_BLOCK));

        scene.overlay().showText(50)
            .colored(PonderPalette.OUTPUT)
            .text("Item actors now render in the preview.")
            .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(60);
        scene.markAsFinished();
    }

    public static void guiSnapshotScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("debug_gui_snapshot", "Dynamic GUI snapshots");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layersFrom(1), EnumFacing.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .colored(PonderPalette.WHITE)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .text("Snapshot providers can swap GUI states at render time.");
        scene.overlay().showGuiSnapshot(snapshot("te_furnace_base"), 55)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .offset(0, -12);
        scene.idle(24);

        scene.overlay().showGuiSnapshot(snapshot("te_panel_config"), 55)
            .pointAt(util.vector().topOf(3, 2, 2))
            .placeNearTarget()
            .offset(80, -10);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(snapshot("te_insolator_live"), 70)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .offset(140, 10);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(snapshot("minecraft_furnace"), 55)
            .pointAt(util.vector().centerOf(1, 2, 2))
            .placeNearTarget()
            .offset(-110, -8);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(snapshot("minecraft_furnace_live"), 55)
            .pointAt(util.vector().centerOf(1, 2, 2))
            .placeNearTarget()
            .offset(-110, 90);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(snapshot("te_furnace_live"), 70)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .offset(140, 110);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(new ResourceLocation("minecraft", "textures/gui/container/furnace.png"),
            176, 166, 55)
            .pointAt(util.vector().centerOf(1, 2, 2))
            .placeNearTarget()
            .offset(-110, 188);
        scene.idle(20);

        scene.overlay().showGuiSnapshot(snapshot("te_furnace_cycle"), 90)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .offset(0, -12);
        scene.overlay().showText(90)
            .colored(PonderPalette.GREEN)
            .pointAt(util.vector().centerOf(2, 2, 2))
            .placeNearTarget()
            .text("This provider cycles base, config, augment and redstone every 20 ticks.");
        scene.idle(90);
        scene.markAsFinished();
    }

    private static ResourceLocation snapshot(String path) {
        return Ponder.asResource("gui_snapshot/" + path);
    }

    private static AxisAlignedBB boxAround(Vec3d center, double xRadius, double yRadius, double zRadius) {
        return new AxisAlignedBB(center.x - xRadius, center.y - yRadius, center.z - zRadius, center.x + xRadius,
            center.y + yRadius, center.z + zRadius);
    }

    private static Vec3d offset(Vec3d vec, double x, double y, double z) {
        return new Vec3d(vec.x + x, vec.y + y, vec.z + z);
    }
}
