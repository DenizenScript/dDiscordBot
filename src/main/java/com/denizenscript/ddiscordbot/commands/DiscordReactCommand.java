package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;

import java.util.List;

public class DiscordReactCommand extends AbstractCommand implements Holdable {

    public DiscordReactCommand() {
        setName("discordreact");
        setSyntax("discordreact [id:<id>] [message:<message_id>] [add/remove/clear] [reaction:<reaction>/all] (user:<user>)");
        setRequiredArguments(4, 6);
    }
    // <--[command]
    // @Name discordreact
    // @Syntax discordreact [id:<id>] (channel:<channel>) [message:<message>] [add/remove/clear] [reaction:<reaction>/all] (user:<user>)
    // @Required 4
    // @Maximum 6
    // @Short Manages message reactions on Discord.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Manages message reactions on Discord.
    //
    // The message can be a <@link objecttype DiscordMessageTag>, or just the message ID, with a channel ID also given.
    //
    // You can add or remove reactions from the bot, or clear all reactions of a specific ID, or clear all reactions from a message entirely.
    //
    // Reactions can be unicode symbols, or custom emoji IDs.
    //
    // Optionally specify a user for 'remove' to remove only a specific user's reaction.
    //
    // 'Add' requires basic add-reaction permissions.
    // 'Clear' requires 'manage messages' permission.
    //
    // @Tags
    // <DiscordMessageTag.reactions>
    //
    // @Usage
    // Use to react to a previously sent message.
    // - ~discordreact id:mybot message:<[some_message]> add reaction:<[my_emoji_id]>
    //
    // @Usage
    // Use to remove a reaction from a previously sent message.
    // - ~discordreact id:mybot message:<[some_message]> remove reaction:middle_finger
    //
    //
    // @Usage
    // Use to clear all reactions from a message.
    // - ~discordreact id:mybot message:<[some_message]> clear reaction:all
    //
    // -->

    public enum DiscordReactInstruction { ADD, REMOVE, CLEAR }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordReactInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("channel")
                    && arg.matchesPrefix("channel")
                    && arg.matchesArgumentType(DiscordChannelTag.class)) {
                scriptEntry.addObject("channel", arg.asType(DiscordChannelTag.class));
            }
            else if (!scriptEntry.hasObject("message")
                    && arg.matchesPrefix("message")
                    && arg.matchesArgumentType(DiscordMessageTag.class)) {
                scriptEntry.addObject("message", arg.asType(DiscordMessageTag.class));
            }
            else if (!scriptEntry.hasObject("user")
                    && arg.matchesPrefix("user")
                    && arg.matchesArgumentType(DiscordUserTag.class)) {
                scriptEntry.addObject("user", arg.asType(DiscordUserTag.class));
            }
            else if (!scriptEntry.hasObject("reaction")
                    && arg.matchesPrefix("reaction")) {
                scriptEntry.addObject("reaction", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an ID!");
        }
        if (!scriptEntry.hasObject("instruction")) {
            throw new InvalidArgumentsException("Must have an instruction!");
        }
        if (!scriptEntry.hasObject("message")) {
            throw new InvalidArgumentsException("Must have a message!");
        }
        if (!scriptEntry.hasObject("reaction")) {
            throw new InvalidArgumentsException("Must have a reaction!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag instruction = scriptEntry.getElement("instruction");
        DiscordChannelTag channel = scriptEntry.getObjectTag("channel");
        DiscordMessageTag message = scriptEntry.getObjectTag("message");
        DiscordUserTag user = scriptEntry.getObjectTag("user");
        ElementTag reaction = scriptEntry.getElement("reaction");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, instruction, channel, user, message, reaction);
        }
        JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
        if (message.channel_id == 0) {
            if (channel != null) {
                message.channel_id = channel.channel_id;
            }
            else {
                Debug.echoError("Must specify a channel!");
                scriptEntry.setFinished(true);
                return;
            }
        }
        message.bot = id.asString();
        Message msg = message.getMessage();
        if (msg == null) {
            Debug.echoError("Unknown message, cannot add reaction.");
            scriptEntry.setFinished(true);
            return;
        }
        Emote emote = null;
        if (reaction.isInt()) {
            emote = client.getEmoteById(reaction.asLong());
        }
        else {
            List<Emote> emotesPossible = client.getEmotesByName(reaction.asString(), true);
            if (!emotesPossible.isEmpty()) {
                emote = emotesPossible.get(0);
            }
        }
        RestAction<Void> action;
        switch (DiscordReactInstruction.valueOf(instruction.asString().toUpperCase())) {
            case ADD: {
                if (emote != null) {
                    action = msg.addReaction(emote);
                }
                else {
                    action = msg.addReaction(reaction.asString());
                }
                break;
            }
            case REMOVE: {
                if (user != null) {
                    User userObj = client.getUserById(user.user_id);
                    if (userObj == null) {
                        Debug.echoError("Cannot remove reaction from unknown user ID.");
                        return;
                    }
                    if (emote != null) {
                        action = msg.removeReaction(emote, userObj);
                    }
                    else {
                        action = msg.removeReaction(reaction.asString(), userObj);
                    }
                }
                else {
                    if (emote != null) {
                        action = msg.removeReaction(emote);
                    }
                    else {
                        action = msg.removeReaction(reaction.asString());
                    }
                }
                break;
            }
            case CLEAR: {
                if (CoreUtilities.toLowerCase(reaction.asString()).equals("all")) {
                    action = msg.clearReactions();
                }
                else {
                    if (emote != null) {
                        action = msg.clearReactions(emote);
                    }
                    else {
                        action = msg.clearReactions(reaction.asString());
                    }
                }
                break;
            }
            default: {
                return; // Not possible, but required to prevent compiler error
            }
        }
        final RestAction<Void> actWait = action;
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            actWait.onErrorMap(t -> {
                Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, () -> {
                    Debug.echoError(t);
                });
                return null;
            });
            actWait.complete();
            scriptEntry.setFinished(true);
        });
    }
}
