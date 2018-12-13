package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.DiscordMessageReceivedScriptEvent;
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
        ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.instance = new DiscordMessageReceivedScriptEvent());
        instance = this;
    }
}
