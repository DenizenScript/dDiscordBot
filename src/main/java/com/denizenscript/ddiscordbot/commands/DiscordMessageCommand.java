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
import net.dv8tion.jda.api.entities.*;
import org.bukkit.Bukkit;

public class DiscordMessageCommand extends AbstractCommand implements Holdable {

    public DiscordMessageCommand() {
        setName("discordmessage");
        setSyntax("discordmessage [id:<id>] [reply:<message>/channel:<channel>/user:<user>] [<message>]");
        setRequiredArguments(3, 4);
    }

    // <--[command]
    // @Name discordmessage
    // @Syntax discordmessage [id:<id>] (reply:<message>/channel:<channel>/user:<user>) [<message>]
    // @Required 3
    // @Maximum 4
    // @Short Sends a message to a Discord channel.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Sends a message to a Discord channel.
    //
    // Command may fail if the bot does not have permission within the Discord group to send a message in that channel.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].message_id> returns the ID of the sent message, when the command is ~waited for.
    //
    // @Usage
    // Use to message a Discord channel.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!"
    //
    // @Usage
    // Use to reply to a message from a message recieved event.
    // - ~discordmessage id:mybot reply:<context.message> "Hello world!"
    //
    // @Usage
    // Use to message an embed to a Discord channel.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "<discord_embed.with[title].as[hi].with[description].as[This is an embed!]>"
    //
    // @Usage
    // Use to message a Discord channel and record the ID.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!" save:sent
    // - announce "Sent as <entry[sent].message_id>"
    //
    // @Usage
    // Use to send a message to a user through a private channel.
    // - ~discordmessage id:mybot message user:<[user]> "Hello world!"
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("channel")
                    && arg.matchesPrefix("to", "channel")
                    && arg.matchesArgumentType(DiscordChannelTag.class)) {
                scriptEntry.addObject("channel", arg.asType(DiscordChannelTag.class));
            }
            else if (!scriptEntry.hasObject("user")
                    && arg.matchesPrefix("to", "user")
                    && arg.matchesArgumentType(DiscordUserTag.class)) {
                scriptEntry.addObject("user", arg.asType(DiscordUserTag.class));
            }
            else if (!scriptEntry.hasObject("reply")
                    && arg.matchesPrefix("reply")
                    && arg.matchesArgumentType(DiscordMessageTag.class)) {
                scriptEntry.addObject("reply", arg.asType(DiscordMessageTag.class));
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new ElementTag(arg.getRawValue()));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an ID!");
        }
        if (!scriptEntry.hasObject("message")) {
            throw new InvalidArgumentsException("Must have a message!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        DiscordChannelTag channel = scriptEntry.getObjectTag("channel");
        ElementTag message = scriptEntry.getElement("message");
        DiscordUserTag user = scriptEntry.getObjectTag("user");
        DiscordMessageTag reply = scriptEntry.getObjectTag("reply");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id.debug()
                    + (channel != null ? channel.debug() : "")
                    + (message != null ? message.debug() : "")
                    + (user != null ? user.debug() : "")
                    + (reply != null ? reply.debug() : ""));
        }

        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
            if (client == null) {
                Debug.echoError("Failed to process DiscordMessage command: unknown bot ID!");
                scriptEntry.setFinished(true);
                return;
            }
            MessageChannel toChannel = null;
            if (reply != null && reply.channel_id != 0) {
                toChannel = client.getTextChannelById(reply.channel_id);
                if (toChannel == null) {
                    toChannel = client.getPrivateChannelById(reply.channel_id);
                }
            }
            else if (channel != null) {
                toChannel = client.getTextChannelById(channel.channel_id);
            }
            else if (user != null) {
                User userObj = client.getUserById(user.user_id);
                if (userObj == null) {
                    Debug.echoError("Invalid or unrecognized user (given user ID not valid? Have you enabled the 'members' intent?).");
                    scriptEntry.setFinished(true);
                    return;
                }
                toChannel = userObj.openPrivateChannel().complete();
            }
            if (toChannel == null) {
                Debug.echoError("Failed to process DiscordMessage command: no channel given!");
                scriptEntry.setFinished(true);
                return;
            }
            Message replyTo = null;
            if (reply != null) {
                replyTo = reply.bot != null ? reply.getMessage() : toChannel.retrieveMessageById(reply.message_id).complete();
            }
            Message sentMessage;
            if (message.asString().startsWith("discordembed@")) {
                MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                if (reply != null) {
                    sentMessage = reply.getMessage().reply(embed).complete();
                }
                else {
                    sentMessage = toChannel.sendMessage(embed).complete();
                }
            }
            else {
                if (reply != null) {
                    sentMessage = reply.getMessage().reply(message.asString()).complete();
                }
                else {
                    sentMessage = toChannel.sendMessage(message.asString()).complete();
                }
            }
            scriptEntry.addObject("message_id", new ElementTag(sentMessage.getId()));
            scriptEntry.setFinished(true);
        });
    }
}
