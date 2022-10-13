package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscordMessageCommand extends AbstractCommand implements Holdable {

    public DiscordMessageCommand() {
        setName("discordmessage");
        setSyntax("discordmessage [id:<id>] [reply:<message>/edit:<message>/channel:<channel>/user:<user>] [<message>] (no_mention) (rows:<rows>) (attach_file_name:<name> attach_file_text:<text>)");
        setRequiredArguments(3, 7);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordmessage
    // @Syntax discordmessage [id:<id>] (reply:<message>/edit:<message>/channel:<channel>/user:<user>) [<message>] (no_mention) (rows:<rows>) (attach_file_name:<name> attach_file_text:<text>)
    // @Required 3
    // @Maximum 7
    // @Short Sends a message to a Discord channel.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Sends a message to a Discord channel.
    //
    // Command may fail if the bot does not have permission within the Discord group to send a message in that channel.
    //
    // You can send the message to: a channel, user, or in reply to a previous message.
    // If sending as a reply, optionally use "no_mention" to disable the default reply pinging the original user.
    //
    // Channels can be specified as either a copied ID, or using any tag that returns a valid DiscordChannelTag.
    // To get IDs, enable "Developer Mode" in your Discord settings, then right click on the channel and press "Copy ID".
    //
    // You can edit an existing message by using "edit:<message>".
    //
    // You can use "attach_file_name:<name>" and "attach_file_text:<text>" to attach a text file with longer content than a normal message allows.
    //
    // You can use "rows" to attach action rows of components, such as buttons to the message, using <@link objecttype DiscordButtonTag>, and <@link objecttype DiscordSelectionTag>.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].message> returns the DiscordMessageTag of the sent message, when the command is ~waited for.
    // <discord[mybot].group[Denizen].channel[bot-spam]> is an example of a tag that will return an appropriate channel object for a named channel in a named group.
    //
    // @Usage
    // Use to message a Discord channel with a copied channel ID.
    // - ~discordmessage id:mybot channel:1234 "Hello world!"
    //
    // @Usage
    // Use to reply to a message from a message received event.
    // - ~discordmessage id:mybot reply:<context.message> "Hello world!"
    //
    // @Usage
    // Use to message an embed to a Discord channel.
    // - ~discordmessage id:mybot channel:1234 "<discord_embed[title=hi;description=this is an embed!]>"
    //
    // @Usage
    // Use to message a Discord channel and record the new message ID.
    // - ~discordmessage id:mybot channel:1234 "Hello world!" save:sent
    // - announce "Sent as <entry[sent].message.id>"
    //
    // @Usage
    // Use to send a message to a user through a private channel.
    // - ~discordmessage id:mybot user:<[user]> "Hello world!"
    //
    // @Usage
    // Use to send a text-file message to a channel.
    // - ~discordmessage id:mybot channel:<[channel]> attach_file_name:quote.xml "attach_file_text:<&lt>mcmonkey<&gt> haha text files amirite<n>gotta abuse em"
    //
    // @Usage
    // Use to send a message and attach a button to it.
    // - define my_button <discord_button.with[style].as[primary].with[id].as[my_button].with[label].as[Hello]>
    // - ~discordmessage id:mybot channel:<[channel]> rows:<[my_button]> "Hello world!"
    //
    // @Usage
    // Use to send a message to a Discord channel, then edit it after 5 seconds.
    // - ~discordmessage id:mybot channel:<[channel]> "Hello world!" save:msg
    // - wait 5s
    // - ~discordmessage id:mybot edit:<entry[msg].message> "Goodbye!"
    //
    // -->

    public static List<ActionRow> createRows(ScriptEntry scriptEntry, ObjectTag rowsObj) {
        if (rowsObj == null) {
            return null;
        }
        Collection<ObjectTag> rows = CoreUtilities.objectToList(rowsObj, scriptEntry.getContext());
        List<ActionRow> actionRows = new ArrayList<>();
        for (ObjectTag row : rows) {
            List<ItemComponent> components = new ArrayList<>();
            for (ObjectTag component : CoreUtilities.objectToList(row, scriptEntry.getContext())) {
                if (component.canBeType(DiscordButtonTag.class)) {
                    components.add(component.asType(DiscordButtonTag.class, scriptEntry.getContext()).build());
                }
                else if (component.canBeType(DiscordSelectionTag.class)) {
                    components.add(component.asType(DiscordSelectionTag.class, scriptEntry.getContext()).build(scriptEntry.getContext()).build());
                }
                else {
                    Debug.echoError("Unrecognized component list entry '" + component + "'");
                }
            }
            actionRows.add(ActionRow.of(components));
        }
        return actionRows;
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") DiscordBotTag bot,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("channel") DiscordChannelTag channel,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("user") DiscordUserTag user,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("reply") DiscordMessageTag reply,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("edit") DiscordMessageTag edit,
                                   @ArgName("no_mention") boolean noMention,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_name") String attachFileName,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("rows") ObjectTag rows,
                                   @ArgRaw @ArgLinear @ArgDefaultNull @ArgName("raw_message") ObjectTag message,
                                   // Note: attachFileText intentionally at end
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_text") String attachFileText) {
        if (message == null && attachFileName == null) {
            throw new InvalidArgumentsRuntimeException("Must have a message!");
        }
        Runnable runner = () -> {
            DiscordConnection connection = bot.getConnection();
            JDA client = connection.client;
            MessageChannel toChannel = null;
            if (reply != null && reply.channel_id != 0) {
                Channel result = connection.getChannel(reply.channel_id);
                if (result instanceof MessageChannel) {
                    toChannel = (MessageChannel) result;
                }
                else {
                    Debug.echoError(scriptEntry, "Invalid reply message channel ID given.");
                    return;
                }
            }
            else if (edit != null && edit.channel_id != 0) {
                Channel result = connection.getChannel(edit.channel_id);
                if (result instanceof MessageChannel) {
                    toChannel = (MessageChannel) result;
                }
                else {
                    Debug.echoError(scriptEntry, "Invalid edit message channel ID given.");
                    return;
                }
            }
            else if (channel != null) {
                Channel result = connection.getChannel(channel.channel_id);
                if (result instanceof MessageChannel) {
                    toChannel = (MessageChannel) result;
                }
                else {
                    Debug.echoError(scriptEntry, "Invalid channel ID given.");
                    return;
                }
            }
            else if (user != null) {
                User userObj = client.getUserById(user.user_id);
                if (userObj == null) {
                    Debug.echoError(scriptEntry, "Invalid or unrecognized user (given user ID not valid? Have you enabled the 'members' intent?).");
                    return;
                }
                toChannel = userObj.openPrivateChannel().complete();
            }
            if (toChannel == null) {
                Debug.echoError(scriptEntry, "Failed to process DiscordMessage command: no channel given!");
                return;
            }
            Message replyTo = null;
            if (reply != null) {
                replyTo = reply.bot != null ? reply.getMessage() : null;
                if (replyTo == null) {
                    replyTo = toChannel.retrieveMessageById(reply.message_id).complete();
                }
                if (replyTo == null) {
                    Debug.echoError(scriptEntry, "Failed to process DiscordMessage reply: invalid message to reply to!");
                    return;
                }
            }
            MessageRequest<?> action = null;
            FileUpload fileUpload = null;
            if (attachFileName != null) {
                if (attachFileText != null) {
                    fileUpload = FileUpload.fromData(attachFileText.getBytes(StandardCharsets.UTF_8), attachFileName);
                }
                else {
                    Debug.echoError(scriptEntry, "Failed to process attachment - missing content?");
                }
            }
            if (message == null || message.toString().length() == 0) {
                if (fileUpload != null) {
                    if (reply != null) {
                        action = replyTo.replyFiles(fileUpload);
                    }
                    else {
                        action = toChannel.sendFiles(fileUpload);
                    }
                }
            }
            else if (message.shouldBeType(DiscordEmbedTag.class)) {
                MessageEmbed embed = message.asType(DiscordEmbedTag.class, scriptEntry.context).build(scriptEntry.context).build();
                if (reply != null) {
                    action = replyTo.replyEmbeds(embed);
                }
                else if (edit != null) {
                    action = toChannel.editMessageEmbedsById(edit.message_id, embed);
                }
                else {
                    action = toChannel.sendMessageEmbeds(embed);
                }
            }
            else {
                if (reply != null) {
                    action = replyTo.reply(message.toString());
                }
                else if (edit != null) {
                    action = toChannel.editMessageById(edit.message_id, message.toString());
                }
                else {
                    action = toChannel.sendMessage(message.toString());
                }
            }
            if (action == null) {
                Debug.echoError(scriptEntry, "Failed to send message - missing content?");
                return;
            }
            if (fileUpload != null) {
                action = action.setFiles(fileUpload);
            }
            List<ActionRow> actionRows = createRows(scriptEntry, rows);
            if (actionRows != null) {
                action.setComponents(actionRows);
            }
            if (noMention) {
                action = action.mentionRepliedUser(false);
            }
            try {
                Message sentMessage = action instanceof MessageCreateAction ? ((MessageCreateAction) action).complete() : ((MessageEditAction) action).complete();
                scriptEntry.addObject("message", new DiscordMessageTag(bot.bot, sentMessage));
            }
            catch (Throwable ex) {
                Debug.echoError(scriptEntry, ex);
            }
        };
        if (scriptEntry.shouldWaitFor()) {
            Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
                runner.run();
                scriptEntry.setFinished(true);
            });
        }
        else {
            Debug.echoError("DiscordMessage command ran without ~waitable. This will freeze the server. If you wanted your server to freeze, ignore this message.");
            runner.run();
        }
    }
}
