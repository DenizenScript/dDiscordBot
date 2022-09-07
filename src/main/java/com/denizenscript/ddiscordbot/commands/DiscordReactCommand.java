package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;

import java.util.List;

public class DiscordReactCommand extends AbstractCommand implements Holdable {

    public DiscordReactCommand() {
        setName("discordreact");
        setSyntax("discordreact [id:<id>] [message:<message_id>] [add/remove/clear] [reaction:<reaction>/all] (user:<user>)");
        setRequiredArguments(4, 6);
        setPrefixesHandled("id");
        isProcedural = false;
        autoCompile();
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
    // For custom emoji, the ID is the numeric ID. For default emoji, the ID is the unicode symbol of the emoji.
    // In both cases, you can copy the correct value by typing the emoji into Discord and prefixing it with a "\" symbol, like "\:myemoji:" and sending it - the sent message will show the internal form of the emoji.
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
    // - ~discordreact id:mybot message:<[some_message]> remove reaction:<[some_reaction_emoji]>
    //
    //
    // @Usage
    // Use to clear all reactions from a message.
    // - ~discordreact id:mybot message:<[some_message]> clear reaction:all
    //
    // -->

    public enum DiscordReactInstruction { ADD, REMOVE, CLEAR }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") DiscordBotTag bot,
                                   @ArgName("instruction") DiscordReactInstruction instruction,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("channel") DiscordChannelTag channel,
                                   @ArgPrefixed @ArgName("message") DiscordMessageTag message,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("user") DiscordUserTag user,
                                   @ArgPrefixed @ArgName("reaction") ElementTag reaction) {
        JDA client = bot.getConnection().client;
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
        message.bot = bot.bot;
        Message msg = message.getMessage();
        if (msg == null) {
            Debug.echoError("Unknown message, cannot add reaction.");
            scriptEntry.setFinished(true);
            return;
        }
        Emoji emoji;
        boolean clearAll = false;
        if (reaction.isInt()) {
            emoji = client.getEmojiById(reaction.asLong());
        }
        else {
            if (CoreUtilities.toLowerCase(reaction.asString()).equals("all")) {
                clearAll = true;
            }
            List<RichCustomEmoji> emotesPossible = client.getEmojisByName(reaction.asString(), true);
            if (!emotesPossible.isEmpty()) {
                emoji = emotesPossible.get(0);
            }
            else {
                emoji = Emoji.fromUnicode(reaction.asString());
            }
        }
        if (emoji == null && !clearAll) {
            Debug.echoError("Invalid emoji!");
            scriptEntry.setFinished(true);
            return;
        }
        RestAction<Void> action = null;
        switch (instruction) {
            case ADD: {
                if (emoji != null) {
                    action = msg.addReaction(emoji);
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
                    if (emoji != null) {
                        action = msg.removeReaction(emoji, userObj);
                    }
                }
                else {
                    if (emoji != null) {
                        action = msg.removeReaction(emoji);
                    }
                }
                break;
            }
            case CLEAR: {
                if (clearAll) {
                    action = msg.clearReactions();
                }
                else {
                    action = msg.clearReactions(emoji);
                }
                break;
            }
        }
        final RestAction<Void> actWait = action;
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            actWait.onErrorMap(t -> {
                Debug.echoError(scriptEntry, t);
                return null;
            });
            actWait.complete();
            scriptEntry.setFinished(true);
        });
    }
}
