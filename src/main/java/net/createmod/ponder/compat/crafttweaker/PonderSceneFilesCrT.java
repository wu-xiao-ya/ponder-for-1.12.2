package net.createmod.ponder.compat.crafttweaker;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.external.scan.ExternalPonderSceneScanner;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ModOnly("crafttweaker")
@ZenClass("mods.ponder.SceneFiles")
public class PonderSceneFilesCrT {

    @ZenMethod
    public static void loadJson(String path) {
        ExternalPonderSceneScanner.rememberScriptJson(path);
    }

    @ZenMethod
    public static void clearQueuedJson() {
        ExternalPonderSceneScanner.clearRememberedScriptJson();
    }

    @ZenMethod
    public static void reloadPonder() {
        PonderIndex.reload();
    }

    @ZenMethod
    public static void loadJsonAndReload(String path) {
        loadJson(path);
        reloadPonder();
    }
}
