package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.commands.*;
import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.FutureWarning;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.debugging.Warning;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DenizenDiscordBot extends JavaPlugin {

    public static Warning oldMessageContexts = new SlowWarning("dDiscordBot contexts relating to message data are now provided by DiscordMessageTag.");
    public static Warning oldMessageCommand = new SlowWarning("dDiscordBot's 'discord message' sub-command has been moved to a base 'discordmessage' command.");
    public static Warning oldConnectCommand = new FutureWarning("dDiscordBot's 'discord connect' sub-command has been moved to a base 'discordconnect' command.");

    public static DenizenDiscordBot instance;

    public HashMap<String, DiscordConnection> connections = new HashMap<>();

    @Override
    public void onEnable() {
        Debug.log("dDiscordBot loaded!");
        instance = this;
        try {
            DenizenCore.getCommandRegistry().registerCommand(DiscordCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordCommandCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordConnectCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordInteractionCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordMessageCommand.class);
            DenizenCore.getCommandRegistry().registerCommand(DiscordReactCommand.class);
            ScriptEvent.registerScriptEvent(DiscordButtonClickedScriptEvent.instance = new DiscordButtonClickedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageDeletedScriptEvent.instance = new DiscordMessageDeletedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageModifiedScriptEvent.instance = new DiscordMessageModifiedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReactionAddScriptEvent.instance = new DiscordMessageReactionAddScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReactionRemoveScriptEvent.instance = new DiscordMessageReactionRemoveScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.instance = new DiscordMessageReceivedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordSelectionUsedScriptEvent.instance = new DiscordSelectionUsedScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordSlashCommandScriptEvent.instance = new DiscordSlashCommandScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserJoinsScriptEvent.instance = new DiscordUserJoinsScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserLeavesScriptEvent.instance = new DiscordUserLeavesScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserNicknameChangeScriptEvent.instance = new DiscordUserNicknameChangeScriptEvent());
            ScriptEvent.registerScriptEvent(DiscordUserRoleChangeScriptEvent.instance = new DiscordUserRoleChangeScriptEvent());
            ObjectFetcher.registerWithObjectFetcher(DiscordBotTag.class, DiscordBotTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordButtonTag.class, DiscordButtonTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordChannelTag.class, DiscordChannelTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordCommandTag.class, DiscordCommandTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordEmbedTag.class, DiscordEmbedTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordGroupTag.class, DiscordGroupTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordInteractionTag.class, DiscordInteractionTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordMessageTag.class, DiscordMessageTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordReactionTag.class, DiscordReactionTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordRoleTag.class, DiscordRoleTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordSelectionTag.class, DiscordSelectionTag.tagProcessor);
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
            // @attribute <discord_button[(<button>)]>
            // @returns DiscordButtonTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordButtonTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord Button object constructed from the input value.
            // Refer to <@link objecttype DiscordButtonTag>.
            // -->
            TagManager.registerTagHandler("discord_button", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    return new DiscordButtonTag();
                }
                return DiscordButtonTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_channel[<channel>]>
            // @returns DiscordChannelTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Channel object constructed from the input value.
            // Refer to <@link objecttype DiscordChannelTag>.
            // -->
            TagManager.registerTagHandler("discord_channel", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord channel tag base must have input.");
                    return null;
                }
                return DiscordChannelTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_command[<command>]>
            // @returns DiscordCommandTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Command object constructed from the input value.
            // Refer to <@link objecttype DiscordCommandTag>.
            // -->
            TagManager.registerTagHandler("discord_command", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord command tag base must have input.");
                    return null;
                }
                return DiscordCommandTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_embed[(<embed>)]>
            // @returns DiscordEmbedTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordEmbedTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord Embed object constructed from the input value.
            // Refer to <@link objecttype DiscordEmbedTag>.
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
            // Refer to <@link objecttype DiscordGroupTag>.
            // -->
            TagManager.registerTagHandler("discord_group", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord group tag base must have input.");
                    return null;
                }
                return DiscordGroupTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_interaction[<interaction>]>
            // @returns DiscordInteractionTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Interaction object constructed from the input value.
            // Refer to <@link objecttype DiscordInteractionTag>.
            // -->
            TagManager.registerTagHandler("discord_interaction", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord interaction tag base must have input.");
                    return null;
                }
                return DiscordInteractionTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_message[<message>]>
            // @returns DiscordMessageTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Message object constructed from the input value.
            // Refer to <@link objecttype DiscordMessageTag>.
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
            // Refer to <@link objecttype DiscordReactionTag>.
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
            // Refer to <@link objecttype DiscordRoleTag>.
            // -->
            TagManager.registerTagHandler("discord_role", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    attribute.echoError("Discord role tag base must have input.");
                    return null;
                }
                return DiscordRoleTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_selection[(<menu>)]>
            // @returns DiscordSelectionTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordSelectionTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord Embed object constructed from the input value.
            // Refer to <@link objecttype DiscordSelectionTag>.
            // -->
            TagManager.registerTagHandler("discord_selection", (attribute) -> {
                if (!attribute.hasContext(1)) {
                    return new DiscordSelectionTag();
                }
                return DiscordSelectionTag.valueOf(attribute.getContext(1), attribute.context);
            });
            // <--[tag]
            // @attribute <discord_user[<user>]>
            // @returns DiscordUserTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord User object constructed from the input value.
            // Refer to <@link objecttype DiscordUserTag>.
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
                        connection.getValue().flags.saveToFile(DiscordConnectCommand.flagFilePathFor(connection.getKey()));
                    }
                    connection.getValue().client.shutdownNow();
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        connections.clear();
        DiscordInteractionTag.interactionCache.clear();
        Bukkit.getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }
}
