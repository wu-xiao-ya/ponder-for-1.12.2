package net.createmod.ponder.client;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderDebugScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class PonderDebugClientCommand extends CommandBase {

    @Override
    public String getName() {
        return "ponderuidebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ponderuidebug [component_id] [scene_index]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ResourceLocation componentId = args.length >= 1 ? new ResourceLocation(args[0]) : null;
        int sceneIndex = args.length >= 2 ? parseInt(args[1], 0) : 0;
        ClientProxy.queueOpenDebugScreen(componentId, sceneIndex);
        if (Minecraft.getMinecraft().player != null) {
            sender.sendMessage(new TextComponentString("[Ponder] Opening debug viewer..."));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
        @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, PonderDebugScreen.getRegisteredComponentsAsStrings());
        }

        if (args.length == 2) {
            ResourceLocation componentId = new ResourceLocation(args[0]);
            java.util.ArrayList<String> indexes = new java.util.ArrayList<String>();
            int size = PonderIndex.getSceneAccess().compile(componentId).size();
            for (int i = 0; i < size; i++) {
                indexes.add(Integer.toString(i));
            }
            return getListOfStringsMatchingLastWord(args, indexes);
        }

        return java.util.Collections.emptyList();
    }
}
