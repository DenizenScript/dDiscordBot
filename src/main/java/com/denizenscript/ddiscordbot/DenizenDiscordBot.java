package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.commands.DiscordCommand;
import com.denizenscript.ddiscordbot.commands.DiscordMessageCommand;
import com.denizenscript.ddiscordbot.commands.DiscordReactCommand;
import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.FutureWarning;
import com.denizenscript.denizencore.utilities.debugging.Warning;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DenizenDiscordBot extends JavaPlugin {

    public static Warning oldMessageContexts = new FutureWarning("dDiscordBot contexts relating to message data are now provided by DiscordMessageTag.");
    public static Warning oldMessageCommand = new FutureWarning("dDiscordMessage's 'discord message' sub-command has been moved to a base 'discordmessage' command.");

    public static DenizenDiscordBot instance;

    public HashMap<String, DiscordConnection> connections = new HashMap<>();

    @Override
    public void onEnable() {
        Debug.log("dDiscordBot loaded!");
        instance = this;
        try {
            DenizenCore.getCommandRegistry().registerCommand(DiscordCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordMessageCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordReactCommand.class);
            ScriptEvent.registerScriptEvent(DiscordMessageDeletedScriptEvent.instance = new DiscordMessageDeletedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageModifiedScriptEvent.instance = new DiscordMessageModifiedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReactionAddScriptEvent.instance = new DiscordMessageReactionAddScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReactionRemoveScriptEvent.instance = new DiscordMessageReactionRemoveScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.instance = new DiscordMessageReceivedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserJoinsScriptEvent.instance = new DiscordUserJoinsScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserLeavesScriptEvent.instance = new DiscordUserLeavesScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserRoleChangeScriptEvent.instance = new DiscordUserRoleChangeScriptEvent());
            ObjectFetcher.registerWithObjectFetcher(DiscordBotTag.class, DiscordBotTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordChannelTag.class, DiscordChannelTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordEmbedTag.class, DiscordEmbedTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordGroupTag.class, DiscordGroupTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordMessageTag.class, DiscordMessageTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordReactionTag.class, DiscordReactionTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordRoleTag.class, DiscordRoleTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordUserTag.class, DiscordUserTag.tagProcessor);
            // <--[tag]
            // @attribute <discord[<bot-id>]>
            // @returns DiscordBotTag
            // @plugin dDiscordBot
            // @description
            // Returns the Discord bot for the given bot ID.
            // -->
            TagManager.registerTagHandler("discord", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord tag base must have input.");
                    return null;
                }
                return DiscordBotTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_channel[<channel>]>
            // @returns DiscordChannelTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Channel object constructed from the input value.
            // Refer to <@link language DiscordChannelTag objects>.
            // -->
            TagManager.registerTagHandler("discord_channel", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord channel tag base must have input.");
                    return null;
                }
                return DiscordChannelTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_embed[(<embed>)>
            // @returns DiscordEmbedTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordEmbedTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord Embed object constructed from the input value.
            // Refer to <@link language DiscordEmbedTag objects>.
            // -->
            TagManager.registerTagHandler("discord_embed", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    return new DiscordEmbedTag();
                }
                return DiscordEmbedTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_group[<group>]>
            // @returns DiscordGroupTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Group object constructed from the input value.
            // Refer to <@link language DiscordGroupTag objects>.
            // -->
            TagManager.registerTagHandler("discord_group", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord group tag base must have input.");
                    return null;
                }
                return DiscordGroupTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_message[<message>]>
            // @returns DiscordMessageTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Message object constructed from the input value.
            // Refer to <@link language DiscordMessageTag objects>.
            // -->
            TagManager.registerTagHandler("discord_message", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord message tag base must have input.");
                    return null;
                }
                return DiscordMessageTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_reaction[<reaction>]>
            // @returns DiscordReactionTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Reaction object constructed from the input value.
            // Refer to <@link language DiscordReactionTag objects>.
            // -->
            TagManager.registerTagHandler("discord_reaction", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord reaction tag base must have input.");
                    return null;
                }
                return DiscordReactionTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_role[<role>]>
            // @returns DiscordRoleTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Role object constructed from the input value.
            // Refer to <@link language DiscordRoleTag objects>.
            // -->
            TagManager.registerTagHandler("discord_role", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord role tag base must have input.");
                    return null;
                }
                return DiscordRoleTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_user[<user>]>
            // @returns DiscordUserTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord User object constructed from the input value.
            // Refer to <@link language DiscordUserTag objects>.
            // -->
            TagManager.registerTagHandler("discord_user", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord user tag base must have input.");
                    return null;
                }
                return DiscordUserTag.valueOf(attribute.getContext(1), attribute.context);
            });
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void onDisable() {
        for (Map.Entry<String, DiscordConnection> connection : connections.entrySet()) {
            try {
                if (connection.getValue().client != null) {
                    if (connection.getValue().flags.modified) {
                        connection.getValue().flags.saveToFile(DiscordCommand.flagFilePathFor(connection.getKey()));
                    }
                    connection.getValue().client.shutdownNow();
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        connections.clear();
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }
}
