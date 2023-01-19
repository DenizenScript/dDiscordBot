package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;
import java.util.stream.Collectors;

public class DiscordReactCommand extends AbstractCommand implements Holdable {

    public DiscordReactCommand() {
        setName("discordreact");
        setSyntax("discordreact (id:<bot>) (channel:<channel>) [message:<message>] [add/remove/clear] [reaction:<reaction>/all] (user:<user>)");
        setRequiredArguments(3, 6);
        isProcedural = false;
        autoCompile();
    }
    // <--[command]
    // @Name discordreact
    // @Syntax discordreact (id:<bot>) (channel:<channel>) [message:<message>] [add/remove/clear] [reaction:<reaction>/all] (user:<user>)
    // @Required 3
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
    // The command can be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <DiscordMessageTag.reactions>
    //
    // @Usage
    // Use to react to a previously sent message.
    // - discordreact id:mybot message:<[some_message]> add reaction:<[my_emoji_id]>
    //
    // @Usage
    // Use to remove a reaction from a previously sent message.
    // - discordreact id:mybot message:<[some_message]> remove reaction:<[some_reaction_emoji]>
    //
    //
    // @Usage
    // Use to clear all reactions from a message.
    // - discordreact id:mybot message:<[some_message]> clear reaction:all
    //
    // -->

    public enum DiscordReactInstruction { ADD, REMOVE, CLEAR }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") @ArgDefaultNull DiscordBotTag bot,
                                   @ArgName("instruction") DiscordReactInstruction instruction,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("channel") DiscordChannelTag channel,
                                   @ArgPrefixed @ArgName("message") DiscordMessageTag message,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("user") DiscordUserTag user,
                                   @ArgPrefixed @ArgName("reaction") ElementTag reaction) {
        bot = DiscordCommandUtils.inferBot(bot, channel, message, user);
        JDA client = bot.getConnection().client;
        message = new DiscordMessageTag(message.bot, message.channel_id, message.message_id);
        if (message.channel_id == 0) {
            if (channel != null) {
                message.channel_id = channel.channel_id;
            }
            else {
                throw new InvalidArgumentsRuntimeException("Must specify a channel!");
            }
        }
        message.bot = bot.bot;
        Message msg = message.getMessage();
        if (msg == null) {
            throw new InvalidArgumentsRuntimeException("Unknown message, cannot add reaction.");
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
            throw new InvalidArgumentsRuntimeException("Invalid emoji!");
        }
        DiscordCommandUtils.cleanWait(scriptEntry, switch (instruction) {
            case ADD -> {
                if (emoji == null) {
                    throw new InvalidArgumentsRuntimeException("Cannot add reaction 'all' - not a real reaction.");
                }
                yield msg.addReaction(emoji);
            }
            case REMOVE -> {
                if (user != null) {
                    User userObj = client.getUserById(user.user_id);
                    if (userObj == null) {
                        throw new InvalidArgumentsRuntimeException("Cannot remove reaction from unknown user ID.");
                    }
                    if (emoji != null) {
                        yield msg.removeReaction(emoji, userObj);
                    }
                    else {
                        yield RestAction.allOf(msg.getReactions().stream()
                                .filter(r -> r.retrieveUsers().stream().anyMatch(u -> u.getIdLong() == userObj.getIdLong()))
                                .map(r -> r.removeReaction(userObj)).collect(Collectors.toSet()));
                    }
                }
                else {
                    if (emoji != null) {
                        yield msg.removeReaction(emoji);
                    }
                    else {
                        yield msg.clearReactions();
                    }
                }
            }
            case CLEAR -> clearAll ? msg.clearReactions() : msg.clearReactions(emoji);
        });
    }
}
