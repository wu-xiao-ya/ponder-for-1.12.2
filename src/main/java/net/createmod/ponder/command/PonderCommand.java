package net.createmod.ponder.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.RecordedOperation;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class PonderCommand extends CommandBase {

    private static final int MAX_DUMP_LINES = 40;

    @Override
    public String getName() {
        return "ponder";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ponderdebug");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ponder <help|list|compile|dump|reload>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || "help".equals(args[0])) {
            sendUsage(sender);
            return;
        }

        String subCommand = args[0];
        if ("list".equals(subCommand)) {
            listRegistryState(sender);
            return;
        }

        if ("reload".equals(subCommand)) {
            PonderIndex.reload();
            sendSummary(sender, "Reloaded");
            return;
        }

        if ("compile".equals(subCommand)) {
            if (args.length < 2) {
                sendUsage(sender);
                return;
            }
            compileScenes(sender, parseComponentId(args[1]));
            return;
        }

        if ("dump".equals(subCommand)) {
            if (args.length < 2) {
                sendUsage(sender);
                return;
            }

            ResourceLocation componentId = parseComponentId(args[1]);
            int sceneIndex = args.length >= 3 ? parseInt(args[2], 0) : 0;
            dumpScene(sender, componentId, sceneIndex);
            return;
        }

        sendUsage(sender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "list", "compile", "dump", "reload");
        }

        if (args.length == 2 && ("compile".equals(args[0]) || "dump".equals(args[0]))) {
            return getListOfStringsMatchingLastWord(args, getRegisteredComponentStrings());
        }

        if (args.length == 3 && "dump".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, getSceneIndexes(parseComponentId(args[1])));
        }

        return Collections.emptyList();
    }

    private void sendUsage(ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Ponder debug commands:"));
        sender.sendMessage(new TextComponentString("/ponder list"));
        sender.sendMessage(new TextComponentString("/ponder reload"));
        sender.sendMessage(new TextComponentString("/ponder compile <component_id>"));
        sender.sendMessage(new TextComponentString("/ponder dump <component_id> [scene_index]"));
    }

    private void listRegistryState(ICommandSender sender) {
        sendSummary(sender, "Registry");

        Map<ResourceLocation, Integer> counts = countScenesByComponent();
        if (counts.isEmpty()) {
            sender.sendMessage(new TextComponentString("No Ponder scenes are registered."));
            return;
        }

        for (Map.Entry<ResourceLocation, Integer> entry : counts.entrySet()) {
            int tagCount = PonderIndex.getTagAccess().getTags(entry.getKey()).size();
            sender.sendMessage(new TextComponentString(entry.getKey() + " -> " + entry.getValue()
                + " storyboard(s), " + tagCount + " tag(s)"));
        }
    }

    private void compileScenes(ICommandSender sender, ResourceLocation componentId) {
        List<PonderScene> scenes = PonderIndex.getSceneAccess().compile(componentId);
        if (scenes.isEmpty()) {
            sender.sendMessage(new TextComponentString("No storyboard entries found for " + componentId));
            return;
        }

        sender.sendMessage(new TextComponentString(
            "Compiled " + scenes.size() + " scene(s) for " + componentId + ":"));
        for (int i = 0; i < scenes.size(); i++) {
            sender.sendMessage(new TextComponentString(describeScene(i, scenes.get(i))));
        }
    }

    private void dumpScene(ICommandSender sender, ResourceLocation componentId, int sceneIndex) throws CommandException {
        List<PonderScene> scenes = PonderIndex.getSceneAccess().compile(componentId);
        if (scenes.isEmpty()) {
            sender.sendMessage(new TextComponentString("No storyboard entries found for " + componentId));
            return;
        }

        if (sceneIndex < 0 || sceneIndex >= scenes.size()) {
            throw new CommandException("Scene index out of bounds for " + componentId + ": " + sceneIndex);
        }

        PonderScene scene = scenes.get(sceneIndex);
        sender.sendMessage(new TextComponentString(describeScene(sceneIndex, scene)));

        List<RecordedOperation> operations = scene.getRecordedOperations();
        if (operations.isEmpty()) {
            sender.sendMessage(new TextComponentString("This scene did not record any operations."));
            return;
        }

        int lines = Math.min(MAX_DUMP_LINES, operations.size());
        for (int i = 0; i < lines; i++) {
            RecordedOperation operation = operations.get(i);
            sender.sendMessage(
                new TextComponentString("[" + i + " @ " + operation.getTick() + "t] " + operation.getDescription()));
        }

        if (operations.size() > lines) {
            sender.sendMessage(new TextComponentString(
                "... truncated " + (operations.size() - lines) + " additional operation(s)"));
        }
    }

    private void sendSummary(ICommandSender sender, String prefix) {
        int sceneEntries = PonderIndex.getSceneAccess().getRegisteredEntries().size();
        int componentCount = countScenesByComponent().size();
        int listedTags = PonderIndex.getTagAccess().getListedTags().size();
        sender.sendMessage(new TextComponentString(prefix + " Ponder state: " + PonderIndex.getPluginCount()
            + " plugin(s), " + sceneEntries + " storyboard entry(ies), " + componentCount + " component(s), "
            + listedTags + " listed tag(s)"));
    }

    private Map<ResourceLocation, Integer> countScenesByComponent() {
        Map<ResourceLocation, Integer> counts = new LinkedHashMap<ResourceLocation, Integer>();
        for (Map.Entry<ResourceLocation, net.createmod.ponder.api.registration.StoryBoardEntry> entry : PonderIndex
            .getSceneAccess()
            .getRegisteredEntries()) {
            Integer existing = counts.get(entry.getKey());
            counts.put(entry.getKey(), Integer.valueOf(existing == null ? 1 : existing.intValue() + 1));
        }
        return counts;
    }

    private Collection<String> getRegisteredComponentStrings() {
        List<String> ids = new ArrayList<String>();
        for (ResourceLocation componentId : countScenesByComponent().keySet()) {
            ids.add(componentId.toString());
        }
        return ids;
    }

    private Collection<String> getSceneIndexes(ResourceLocation componentId) {
        List<PonderScene> scenes = PonderIndex.getSceneAccess().compile(componentId);
        List<String> indexes = new ArrayList<String>();
        for (int i = 0; i < scenes.size(); i++) {
            indexes.add(Integer.toString(i));
        }
        return indexes;
    }

    private String describeScene(int sceneIndex, PonderScene scene) {
        StringBuilder builder = new StringBuilder();
        builder.append('#')
            .append(sceneIndex)
            .append(" sceneId=")
            .append(scene.getSceneId())
            .append(", title=\"")
            .append(scene.getTitle())
            .append("\", schematic=")
            .append(scene.getSchematicLocation())
            .append(", tags=")
            .append(formatTags(scene.getTags()))
            .append(", ops=")
            .append(scene.getOperationLog().size())
            .append(", idleTicks=")
            .append(scene.getTotalIdleTicks())
            .append(", finished=")
            .append(scene.isFinished());
        return builder.toString();
    }

    private String formatTags(List<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(tags.get(i));
        }
        builder.append(']');
        return builder.toString();
    }

    private ResourceLocation parseComponentId(String rawId) {
        return new ResourceLocation(rawId);
    }
}
