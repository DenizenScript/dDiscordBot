package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.events.ScriptEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class dDiscordBot extends JavaPlugin {

    public static dDiscordBot instance;

    public HashMap<String, DiscordConnection> connections = new HashMap<>();

    @Override
    public void onEnable() {
        dB.log("dDiscordBot loaded!");
        DenizenCore.getCommandRegistry().registerCoreMember(DiscordCommand.class, "DISCORD", "DISCORD [read(the<meta>)]", 2);
        ScriptEvent.registerScriptEvent(DiscordMessageModifiedScriptEvent.instance = new DiscordMessageModifiedScriptEvent());
        ScriptEvent.registerScriptEvent(DiscordMessageDeletedScriptEvent.instance = new DiscordMessageDeletedScriptEvent());
        ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.instance = new DiscordMessageReceivedScriptEvent());
        ScriptEvent.registerScriptEvent(DiscordUserJoinsScriptEvent.instance = new DiscordUserJoinsScriptEvent());
        ScriptEvent.registerScriptEvent(DiscordUserLeavesScriptEvent.instance = new DiscordUserLeavesScriptEvent());
        ScriptEvent.registerScriptEvent(DiscordUserRoleChangeScriptEvent.instance = new DiscordUserRoleChangeScriptEvent());
        instance = this;
    }
}
