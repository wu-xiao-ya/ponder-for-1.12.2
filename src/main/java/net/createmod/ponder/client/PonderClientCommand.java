package net.createmod.ponder.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderDebugScreen;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class PonderClientCommand extends CommandBase {

    @Override
    public String getName() {
        return "ponderui";
    }

    @Override
    public List<String> getAliases() {
        return java.util.Arrays.asList("ponderviewer", "pondershowcase");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ponderui [component_id] [scene_index]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            ClientProxy.queueOpenIndexScreen();
            if (Minecraft.getMinecraft().player != null) {
                sender.sendMessage(new TextComponentString("[Ponder] Opening browser..."));
            }
            return;
        }

        ResourceLocation componentId = args.length >= 1 ? new ResourceLocation(args[0]) : null;
        int sceneIndex = args.length >= 2 ? parseInt(args[1], 0) : 0;
        ClientProxy.queueOpenShowcaseScreen(componentId, sceneIndex);
        if (Minecraft.getMinecraft().player != null) {
            sender.sendMessage(new TextComponentString("[Ponder] Opening Ponder UI..."));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, getRegisteredComponentStrings());
        }

        if (args.length == 2) {
            ResourceLocation componentId = new ResourceLocation(args[0]);
            List<String> indexes = new ArrayList<String>();
            int size = PonderIndex.getSceneAccess().compile(componentId).size();
            for (int i = 0; i < size; i++) {
                indexes.add(Integer.toString(i));
            }
            return getListOfStringsMatchingLastWord(args, indexes);
        }

        return Collections.emptyList();
    }

    private Collection<String> getRegisteredComponentStrings() {
        Set<String> ids = new LinkedHashSet<String>();
        for (ResourceLocation componentId : PonderUI.getRegisteredComponents()) {
            ids.add(componentId.toString());
        }
        return ids;
    }
}
