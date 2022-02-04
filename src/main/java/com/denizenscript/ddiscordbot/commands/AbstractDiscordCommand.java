package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;

public abstract class AbstractDiscordCommand extends AbstractCommand {

    public void handleError(ScriptEntry entry, String message) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenDiscordBot.instance, () -> {
            Debug.echoError(entry, "Error in " + getName() + " command: " + message);
        });
    }
    public void handleError(ScriptEntry entry, Throwable ex) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenDiscordBot.instance, () -> {
            Debug.echoError(entry, "Exception in " + getName() + " command:");
            Debug.echoError(ex);
        });
    }
}
