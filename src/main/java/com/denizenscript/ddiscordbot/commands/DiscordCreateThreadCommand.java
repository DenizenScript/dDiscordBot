package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.*;
import org.bukkit.Bukkit;

public class DiscordCreateThreadCommand extends AbstractCommand implements Holdable {

    public DiscordCreateThreadCommand() {
        setName("discordcreatethread");
        setSyntax("discordcreatethread [id:<id>] [name:<name>] [message:<message>/parent:<channel> (private)]");
        setRequiredArguments(3, 4);
        setPrefixesHandled("id", "name", "message", "parent");
        setBooleansHandled("private");
    }
    // <--[command]
    // @Name discordcreatethread
    // @Syntax discordcreatethread [id:<id>] [name:<name>] [message:<message>/parent:<channel> (private)]
    // @Required 3
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
    // @Tags
    // <entry[saveName].created_thread>
    //
    // @Usage
    // Use to create a new thread for a specific message and send a bot message to it.
    // - ~discordcreatethread id:mybot "name:The bot's awesome thread" message:<context.message> save:thread
    // - ~discordmessage id:mybot channel:<entry[thread].created_thread> "Here I made this thread just for you"
    //
    // @Usage
    // Use to create a new thread in a channel and link to it in some channel.
    // - ~discordcreatethread id:mybot "name:The bot's awesome thread" parent:<[some_channel]> save:thread
    // - ~discordmessage id:mybot channel:<[some_channel]> "I created thread <&lt>#<entry[thread].created_thread.id><&gt>!"
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Legacy parseArgs not used
    }

    public static void handleError(ScriptEntry entry, String message) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenDiscordBot.instance, () -> {
            Debug.echoError(entry, "Error in DiscordCreateThread command: " + message);
        });
    }
    public static void handleError(ScriptEntry entry, Throwable ex) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenDiscordBot.instance, () -> {
            Debug.echoError(entry, "Exception in DiscordCreateThread command:");
            Debug.echoError(ex);
        });
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.requiredArgForPrefixAsElement("id");
        ElementTag name = scriptEntry.requiredArgForPrefixAsElement("name");
        DiscordMessageTag message = scriptEntry.argForPrefix("message", DiscordMessageTag.class, true);
        DiscordChannelTag channel = scriptEntry.argForPrefix("channel", DiscordChannelTag.class, true);
        boolean isPrivate = scriptEntry.argAsBoolean("private");
        if (message != null) {
            if (channel != null || isPrivate) {
                throw new InvalidArgumentsRuntimeException("Cannot have both a 'message:' and channel/private");
            }
            if (message.channel_id == 0) {
                throw new InvalidArgumentsRuntimeException("DiscordMessageTag not fully formed - missing channel ID.");
            }
        }
        else if (channel == null) {
            throw new InvalidArgumentsRuntimeException("Missing message or channel/initial_message argument!");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, name, message, channel, isPrivate ? db("private", true) : "");
        }
        DiscordConnection bot = DenizenDiscordBot.instance.connections.get(id.asString());
        if (bot == null) {
            Debug.echoError("Invalid bot ID. Are you sure the bot is connected, or did you make a typo?");
            return;
        }
        Runnable runner = () -> {
            if (message != null) {
                DiscordMessageTag forMessage = message;
                if (forMessage.bot == null || !forMessage.bot.equals(id.asString())) {
                    forMessage = forMessage.duplicate();
                    forMessage.bot = id.asString();
                }
                Message actualMessage = forMessage.getMessage();
                if (actualMessage == null) {
                    handleError(scriptEntry, "Invalid message reference.");
                    return;
                }
                if (!(actualMessage.getChannel() instanceof GuildChannel)) {
                    handleError(scriptEntry, "Message referenced is not in a group (can't create threads in a DM).");
                    return;
                }
                if (actualMessage.getChannel() instanceof ThreadChannel) {
                    handleError(scriptEntry, "Message referenced is in a thread - you can't have threads inside threads.");
                    return;
                }
                try {
                    ThreadChannel created = ((TextChannel) actualMessage.getChannel()).createThreadChannel(name.asString(), actualMessage.getIdLong()).complete();
                    if (created != null) {
                        scriptEntry.addObject("created_thread", new DiscordChannelTag(id.asString(), created));
                    }
                }
                catch (Throwable ex) {
                    handleError(scriptEntry, ex);
                }
            }
            else {
                DiscordChannelTag forChannel = channel;
                if (forChannel.bot == null || !forChannel.bot.equals(id.asString())) {
                    forChannel = forChannel.duplicate();
                    forChannel.bot = id.asString();
                }
                Channel actualChannel = forChannel.getChannel();
                if (actualChannel == null) {
                    handleError(scriptEntry, "Invalid channel reference.");
                    return;
                }
                if (!(actualChannel instanceof GuildChannel)) {
                    handleError(scriptEntry, "Channel referenced is not in a group (can't create threads in a DM).");
                    return;
                }
                if (!(actualChannel instanceof TextChannel)) {
                    handleError(scriptEntry, "Channel referenced is not a text channel (can't create threads in a voice channel).");
                    return;
                }
                if (actualChannel instanceof ThreadChannel) {
                    handleError(scriptEntry, "Channel referenced is a thread - you can't have threads inside threads.");
                    return;
                }
                try {
                    ThreadChannel created = ((TextChannel) actualChannel).createThreadChannel(name.asString(), isPrivate).complete();
                    if (created != null) {
                        scriptEntry.addObject("created_thread", new DiscordChannelTag(id.asString(), created));
                    }
                }
                catch (Throwable ex) {
                    handleError(scriptEntry, ex);
                }
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
