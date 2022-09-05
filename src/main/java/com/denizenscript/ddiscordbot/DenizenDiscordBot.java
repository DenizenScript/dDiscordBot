package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.commands.*;
import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.ddiscordbot.properties.DiscordTimeTagProperties;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.debugging.*;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DenizenDiscordBot extends JavaPlugin {

    public static Warning oldMessageContexts = new SlowWarning("oldMessageContexts", "dDiscordBot contexts relating to message data are now provided by DiscordMessageTag.");
    public static Warning oldMessageCommand = new SlowWarning("oldMessageCommand", "dDiscordBot's 'discord message' sub-command has been moved to a base 'discordmessage' command.");
    public static Warning oldConnectCommand = new FutureWarning("oldConnectCommand", "dDiscordBot's 'discord connect' sub-command has been moved to a base 'discordconnect' command.");
    public static Warning oldStopTyping = new FutureWarning("oldStopTyping", "dDiscordBot's 'discord stop_typing' sub-command is deprecated as it does nothing.");
    public static Warning oldDeleteMessage = new FutureWarning("oldDeleteMessage", "dDiscordBot's 'discord delete_message' sub-command is deprecated in favor of 'adjust <[message]> delete'.");
    public static Warning oldEditMessage = new SlowWarning("oldEditMessage", "dDiscordBot's 'discord edit_message' sub-command has been moved to the 'discordmessage' command.");
    public static Warning oldTokenFile = new FutureWarning("oldTokenFile", "dDiscordBot used to recommend 'tokenfile' for 'discordconnect', however it is now recommended that you use a SecretTag and the 'secrets.secret' file for the token.");
    public static Warning oldCommandPermissions = new StrongWarning("oldCommandPermissions", "dDiscordBot's 'discordcommand' command's 'enabled', 'enable_for', 'disable_for' arguments and its 'perms' instruction no longer function due to API changes; use the 'Integrations' panel in your server settings instead.");

    public static DenizenDiscordBot instance;

    public HashMap<String, DiscordConnection> connections = new HashMap<>();

    public static boolean allowMessageRetrieval = true;

    public static int messageCacheSize = 128;

    @Override
    public void onEnable() {
        Debug.log("dDiscordBot loaded!");
        instance = this;
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        if (config != null) {
            allowMessageRetrieval = config.getBoolean("Allow message lookup", true);
            messageCacheSize = config.getInt("Message cache size", 128);
        }
        try {
            // Commands
            DenizenCore.commandRegistry.registerCommand(DiscordCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordCommandCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordConnectCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordCreateChannelCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordCreateThreadCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordInteractionCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordMessageCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordModalCommand.class);
            DenizenCore.commandRegistry.registerCommand(DiscordReactCommand.class);
            // Events
            ScriptEvent.registerScriptEvent(DiscordButtonClickedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordChannelCreateScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordChannelDeleteScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordMessageDeletedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordMessageModifiedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordMessageReactionAddScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordMessageReactionRemoveScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordMessageReceivedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordModalSubmittedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordSelectionUsedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordApplicationCommandScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordThreadArchivedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordThreadRevealedScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordUserJoinsScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordUserLeavesScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordUserNicknameChangeScriptEvent.class);
            ScriptEvent.registerScriptEvent(DiscordUserRoleChangeScriptEvent.class);
            // Objects
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
            ObjectFetcher.registerWithObjectFetcher(DiscordTextInputTag.class, DiscordTextInputTag.tagProcessor);
            ObjectFetcher.registerWithObjectFetcher(DiscordUserTag.class, DiscordUserTag.tagProcessor);
            // Extension properties
            PropertyParser.registerProperty(DiscordTimeTagProperties.class, TimeTag.class);

            // <--[tag]
            // @attribute <discord_bots>
            // @returns ListTag(DiscordBotTag)
            // @plugin dDiscordBot
            // @description
            // Returns a list of all Discord bots currently loaded in dDiscordBot.
            // -->
            TagManager.registerTagHandler(ListTag.class, "discord_bots", (attribute) -> {
                ListTag bots = new ListTag();
                for (String bot : connections.keySet()) {
                    bots.addObject(new DiscordBotTag(bot));
                }
                return bots;
            });

            // <--[tag]
            // @attribute <discord[<bot-id>]>
            // @returns DiscordBotTag
            // @plugin dDiscordBot
            // @description
            // Returns the Discord bot for the given bot ID.
            // -->
            TagManager.registerTagHandler(DiscordBotTag.class, "discord", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord tag base must have input.");
                    return null;
                }
                return DiscordBotTag.valueOf(attribute.getParam(), attribute.context);
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
            TagManager.registerTagHandler(DiscordButtonTag.class, "discord_button", (attribute) -> {
                if (!attribute.hasParam()) {
                    return new DiscordButtonTag();
                }
                return DiscordButtonTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_channel[<channel>]>
            // @returns DiscordChannelTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Channel object constructed from the input value.
            // Refer to <@link objecttype DiscordChannelTag>.
            // -->
            TagManager.registerTagHandler(DiscordChannelTag.class, "discord_channel", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord channel tag base must have input.");
                    return null;
                }
                return DiscordChannelTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_command[<command>]>
            // @returns DiscordCommandTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Command object constructed from the input value.
            // Refer to <@link objecttype DiscordCommandTag>.
            // -->
            TagManager.registerTagHandler(DiscordCommandTag.class, "discord_command", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord command tag base must have input.");
                    return null;
                }
                return DiscordCommandTag.valueOf(attribute.getParam(), attribute.context);
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
            TagManager.registerTagHandler(DiscordEmbedTag.class, "discord_embed", (attribute) -> {
                if (!attribute.hasParam()) {
                    return new DiscordEmbedTag();
                }
                return DiscordEmbedTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_group[<group>]>
            // @returns DiscordGroupTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Group object constructed from the input value.
            // Refer to <@link objecttype DiscordGroupTag>.
            // -->
            TagManager.registerTagHandler(DiscordGroupTag.class, "discord_group", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord group tag base must have input.");
                    return null;
                }
                return DiscordGroupTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_interaction[<interaction>]>
            // @returns DiscordInteractionTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Interaction object constructed from the input value.
            // Refer to <@link objecttype DiscordInteractionTag>.
            // -->
            TagManager.registerTagHandler(DiscordInteractionTag.class, "discord_interaction", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord interaction tag base must have input.");
                    return null;
                }
                return DiscordInteractionTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_message[<message>]>
            // @returns DiscordMessageTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Message object constructed from the input value.
            // Refer to <@link objecttype DiscordMessageTag>.
            // -->
            TagManager.registerTagHandler(DiscordMessageTag.class, "discord_message", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord message tag base must have input.");
                    return null;
                }
                return DiscordMessageTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_reaction[<reaction>]>
            // @returns DiscordReactionTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Reaction object constructed from the input value.
            // Refer to <@link objecttype DiscordReactionTag>.
            // -->
            TagManager.registerTagHandler(DiscordReactionTag.class, "discord_reaction", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord reaction tag base must have input.");
                    return null;
                }
                return DiscordReactionTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_role[<role>]>
            // @returns DiscordRoleTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord Role object constructed from the input value.
            // Refer to <@link objecttype DiscordRoleTag>.
            // -->
            TagManager.registerTagHandler(DiscordRoleTag.class, "discord_role", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord role tag base must have input.");
                    return null;
                }
                return DiscordRoleTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_selection[(<menu>)]>
            // @returns DiscordSelectionTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordSelectionTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord Selection object constructed from the input value.
            // Refer to <@link objecttype DiscordSelectionTag>.
            // -->
            TagManager.registerTagHandler(DiscordSelectionTag.class, "discord_selection", (attribute) -> {
                if (!attribute.hasParam()) {
                    return new DiscordSelectionTag();
                }
                return DiscordSelectionTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_text_input[(<button>)]>
            // @returns DiscordTextInputTag
            // @plugin dDiscordBot
            // @description
            // Returns a blank DiscordTextInputTag instance, to be filled with data via the with.as tag.
            // Or, if given an input, returns a Discord TextInput object constructed from the input value.
            // Refer to <@link objecttype DiscordTextInputTag>.
            // -->
            TagManager.registerTagHandler(DiscordTextInputTag.class, "discord_text_input", (attribute) -> {
                if (!attribute.hasParam()) {
                    return new DiscordTextInputTag();
                }
                return DiscordTextInputTag.valueOf(attribute.getParam(), attribute.context);
            });

            // <--[tag]
            // @attribute <discord_user[<user>]>
            // @returns DiscordUserTag
            // @plugin dDiscordBot
            // @description
            // Returns a Discord User object constructed from the input value.
            // Refer to <@link objecttype DiscordUserTag>.
            // -->
            TagManager.registerTagHandler(DiscordUserTag.class, "discord_user", (attribute) -> {
                if (!attribute.hasParam()) {
                    attribute.echoError("Discord user tag base must have input.");
                    return null;
                }
                return DiscordUserTag.valueOf(attribute.getParam(), attribute.context);
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
