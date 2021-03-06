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
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;

public class DiscordMessageCommand extends AbstractCommand implements Holdable {

    public DiscordMessageCommand() {
        setName("discordmessage");
        setSyntax("discordmessage [id:<id>] [reply:<message>/channel:<channel>/user:<user>] [<message>] (no_mention) (attach_file_name:<name> attach_file_text:<text>)");
        setRequiredArguments(3, 7);
    }

    // <--[command]
    // @Name discordmessage
    // @Syntax discordmessage [id:<id>] (reply:<message>/channel:<channel>/user:<user>) [<message>] (no_mention) (attach_file_name:<name> attach_file_text:<text>)
    // @Required 3
    // @Maximum 7
    // @Short Sends a message to a Discord channel.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Sends a message to a Discord channel.
    //
    // Command may fail if the bot does not have permission within the Discord group to send a message in that channel.
    //
    // You can send the message to: a channel or user, or optionally in reply to a previous message.
    // If sending as a reply, optionally use "no_mention" to disable the default reply pinging the original user.
    //
    // You can use "attach_file_name:<name>" and "attach_file_text:<text>" to attach a text file with longer content than a normal message allows.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].message> returns the DiscordMessageTag of the sent message, when the command is ~waited for.
    //
    // @Usage
    // Use to message a Discord channel.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!"
    //
    // @Usage
    // Use to reply to a message from a message received event.
    // - ~discordmessage id:mybot reply:<context.message> "Hello world!"
    //
    // @Usage
    // Use to message an embed to a Discord channel.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "<discord_embed.with[title].as[hi].with[description].as[This is an embed!]>"
    //
    // @Usage
    // Use to message a Discord channel and record the ID.
    // - ~discordmessage id:mybot channel:<discord[mybot].group[Denizen].channel[bot-spam]> "Hello world!" save:sent
    // - announce "Sent as <entry[sent].message.id>"
    //
    // @Usage
    // Use to send a message to a user through a private channel.
    // - ~discordmessage id:mybot message user:<[user]> "Hello world!"
    //
    // @Usage
    // Use to send a text-file message to a channel.
    // - ~discordmessage id:mybot channel:<[channel]> attach_file_name:quote.xml "attach_file_text:<&lt>mcmonkey<&gt> haha text files amirite<n>gotta abuse em"
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("attach_file_name")
                    && arg.matchesPrefix("attach_file_name")) {
                scriptEntry.addObject("attach_file_name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("attach_file_text")
                    && arg.matchesPrefix("attach_file_text")) {
                scriptEntry.addObject("attach_file_text", arg.asElement());
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
            else if (!scriptEntry.hasObject("no_mention")
                    && arg.matches("no_mention")) {
                scriptEntry.addObject("no_mention", new ElementTag(true));
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
        if (!scriptEntry.hasObject("message") && !scriptEntry.hasObject("attach_file_name")) {
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
        ElementTag noMention = scriptEntry.getElement("no_mention");
        ElementTag attachFileName = scriptEntry.getElement("attach_file_name");
        ElementTag attachFileText = scriptEntry.getElement("attach_file_text");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id.debug()
                    + (channel != null ? channel.debug() : "")
                    + (message != null ? message.debug() : "")
                    + (user != null ? user.debug() : "")
                    + (reply != null ? reply.debug() : "")
                    + (attachFileName != null ? attachFileName.debug() : "")
                    + (attachFileText != null ? attachFileText.debug() : "")
                    + (noMention != null ? noMention.debug() : ""));
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
            MessageAction action = null;
            boolean isFile = false;
            if (message == null || message.asString().length() == 0) {
                if (attachFileName != null) {
                    if (attachFileText != null) {
                        if (reply != null) {
                            action = replyTo.reply(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                        }
                        else {
                            action = toChannel.sendFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                        }
                        isFile = true;
                    }
                }
            }
            else if (message.asString().startsWith("discordembed@")) {
                MessageEmbed embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.context).build();
                if (reply != null) {
                    action = replyTo.reply(embed);
                }
                else {
                    action = toChannel.sendMessage(embed);
                }
            }
            else {
                if (reply != null) {
                    action = replyTo.reply(message.asString());
                }
                else {
                    action = toChannel.sendMessage(message.asString());
                }
            }
            if (action == null) {
                Debug.echoError("Failed to send message - missing content?");
                scriptEntry.setFinished(true);
                return;
            }
            if (!isFile && attachFileName != null) {
                if (attachFileText != null) {
                    action = action.addFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                }
                else {
                    Debug.echoError("Failed to send attachment - missing content?");
                }
            }
            if (noMention != null && noMention.asBoolean()) {
                action = action.mentionRepliedUser(false);
            }
            Message sentMessage = action.complete();
            scriptEntry.addObject("message", new DiscordMessageTag(id.asString(), sentMessage));
            scriptEntry.setFinished(true);
        });
    }
}
