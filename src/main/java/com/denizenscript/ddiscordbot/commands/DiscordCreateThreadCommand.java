package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.RestAction;

public class DiscordCreateThreadCommand extends AbstractCommand implements Holdable {

    public DiscordCreateThreadCommand() {
        setName("discordcreatethread");
        setSyntax("discordcreatethread (id:<bot>) [name:<name>] [message:<message>/parent:<channel> (private)]");
        setRequiredArguments(2, 4);
        isProcedural = false;
        autoCompile();
    }
    // <--[command]
    // @Name discordcreatethread
    // @Syntax discordcreatethread (id:<bot>) [name:<name>] [message:<message>/parent:<channel> (private)]
    // @Required 2
    // @Maximum 4
    // @Short Creates a new Discord thread.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Creates a new Discord thread.
    //
    // You must specify the bot object ID, and the thread name.
    //
    // You can either specify a full DiscordMessageTag instance to create a thread based on that message,
    // OR specify a DiscordChannelTag parent and optionally mark it private (otherwise it's public).
    //
    // The command can be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].created_thread> returns the newly created thread.
    //
    // @Usage
    // Use to create a new thread for a specific message and send a bot message to it.
    // - ~discordcreatethread id:mybot "name:The bot's awesome thread" message:<context.message> save:thread
    // - discordmessage id:mybot channel:<entry[thread].created_thread> "Here I made this thread just for you"
    //
    // @Usage
    // Use to create a new thread in a channel and link to it in some channel.
    // - ~discordcreatethread id:mybot "name:The bot's awesome thread" parent:<[some_channel]> save:thread
    // - discordmessage id:mybot channel:<[some_channel]> "I created thread <&lt>#<entry[thread].created_thread.id><&gt>!"
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") @ArgDefaultNull DiscordBotTag bot,
                                   @ArgPrefixed @ArgName("name") String name,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("message") DiscordMessageTag message,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("parent") DiscordChannelTag channel,
                                   @ArgName("private") boolean isPrivate) {
        bot = DiscordCommandUtils.inferBot(bot, channel, message);
        if (message != null) {
            if (channel != null || isPrivate) {
                throw new InvalidArgumentsRuntimeException("Cannot have both a 'message:' and channel/private");
            }
            if (message.channel_id == 0) {
                throw new InvalidArgumentsRuntimeException("DiscordMessageTag not fully formed - missing channel ID.");
            }
        }
        else if (channel == null) {
            throw new InvalidArgumentsRuntimeException("Missing message or channel argument!");
        }
        RestAction<ThreadChannel> action;
        if (message != null) {
            DiscordMessageTag forMessage = message;
            if (forMessage.bot == null || !forMessage.bot.equals(bot.bot)) {
                forMessage = forMessage.duplicate();
                forMessage.bot = bot.bot;
            }
            Message actualMessage = forMessage.getMessage();
            if (actualMessage == null) {
                throw new InvalidArgumentsRuntimeException("Invalid message reference.");
            }
            checkChannel(actualMessage.getChannel());
            action = ((TextChannel) actualMessage.getChannel()).createThreadChannel(name, actualMessage.getIdLong());
        }
        else {
            DiscordChannelTag forChannel = channel;
            if (forChannel.bot == null || !forChannel.bot.equals(bot.bot)) {
                forChannel = forChannel.duplicate();
                forChannel.bot = bot.bot;
            }
            Channel actualChannel = forChannel.getChannel();
            if (actualChannel == null) {
                throw new InvalidArgumentsRuntimeException("Invalid channel reference.");
            }
            checkChannel(actualChannel);
            action = ((TextChannel) actualChannel).createThreadChannel(name, isPrivate);
        }
        final DiscordBotTag finalBot = bot;
        DiscordCommandUtils.cleanWait(scriptEntry, action.onSuccess(created -> scriptEntry.addObject("created_thread", new DiscordChannelTag(finalBot.bot, created))));
    }

    static void checkChannel(Channel actualChannel) {
        if (!(actualChannel instanceof GuildChannel)) {
            throw new InvalidArgumentsRuntimeException("Channel referenced is not in a group (can't create threads in a DM).");
        }
        if (!(actualChannel instanceof TextChannel)) {
            throw new InvalidArgumentsRuntimeException("Channel referenced is not a text channel (can't create threads in a voice channel).");
        }
        if (actualChannel instanceof ThreadChannel) {
            throw new InvalidArgumentsRuntimeException("Channel referenced is a thread - you can't have threads inside threads.");
        }
    }
}
