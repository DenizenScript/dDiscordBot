package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordEmbedTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DiscordInteractionCommand extends AbstractCommand implements Holdable {

    public DiscordInteractionCommand() {
        setName("discordinteraction");
        setSyntax("discordinteraction [defer/reply/delete] (interaction:<interaction>) (ephemeral:true/{false}) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)");
        setRequiredArguments(3, 7);
    }

    // <--[command]
    // @Name discordinteraction
    // @Syntax discordinteraction [defer/reply/delete] (interaction:<interaction>) (ephemeral:true/{false}) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)
    // @Required 3
    // @Maximum 7
    // @Short Manages Discord interactions.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Manages Discord interactions.
    //
    // You can defer, reply to, or delete an interaction. These instructions all require the "interaction" argument.
    //
    // The "ephemeral" argument can be used to have the reply message be visible to that one user.
    //
    // You should almost always defer an interaction before replying. A defer will override a reply in terms of being ephemeral.
    // Replying to an interaction uses similar logic to normal messaging. See <@link command discordmessage>.
    //
    // Slash commands and replies to interactions, have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // Generally used alongside <@link command discordcommand>
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a slash command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to defer and reply to a slash command interaction.
    // - ~discordinteraction defer interaction:<context.interaction>
    // - ~discordinteraction reply interaction:<context.interaction> <context.options.get[hello].if_null[world]>
    //
    // @Usage
    // Use to defer and reply to an interaction ephemerally.
    // - ~discordinteraction defer interaction:<context.interaction> ephemeral:true
    // - ~discordinteraction reply interaction:<context.interaction> "Shh, don't tell anyone!"
    //
    // -->

    public enum DiscordInteractionInstruction { DEFER, REPLY, DELETE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordInteractionInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("interaction")
                    && arg.matchesPrefix("interaction")
                    && arg.matchesArgumentType(DiscordInteractionTag.class)) {
                scriptEntry.addObject("interaction", arg.asType(DiscordInteractionTag.class));
            }
            else if (!scriptEntry.hasObject("ephemeral")
                    && arg.matchesPrefix("ephemeral")) {
                scriptEntry.addObject("ephemeral", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("attach_file_name")
                    && arg.matchesPrefix("attach_file_name")) {
                scriptEntry.addObject("attach_file_name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("attach_file_text")
                    && arg.matchesPrefix("attach_file_text")) {
                scriptEntry.addObject("attach_file_text", arg.asElement());
            }
            else if (!scriptEntry.hasObject("rows")
                    && arg.matchesPrefix("rows")) {
                scriptEntry.addObject("rows", arg.asType(ListTag.class).filter(ListTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new ElementTag(arg.getRawValue()));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("instruction")) {
            throw new InvalidArgumentsException("Must have an instruction!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag instruction = scriptEntry.getElement("instruction");
        DiscordInteractionTag interaction = scriptEntry.getObjectTag("interaction");
        ElementTag ephemeral = scriptEntry.getElement("ephemeral");
        ElementTag attachFileName = scriptEntry.getElement("attach_file_name");
        ElementTag attachFileText = scriptEntry.getElement("attach_file_text");
        List<ListTag> rows = scriptEntry.getObjectTag("rows");
        ElementTag message = scriptEntry.getElement("message");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), instruction, interaction, ephemeral, attachFileName, attachFileText, rows != null ? ArgumentHelper.debugList("rows", rows) : null, message);
        }
        DiscordInteractionInstruction instructionEnum = DiscordInteractionInstruction.valueOf(instruction.asString().toUpperCase());
        boolean isEphemeral = ephemeral != null && ephemeral.asBoolean();
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            try {
                switch (instructionEnum) {
                    case DEFER: {
                        if (interaction == null) {
                            Debug.echoError(scriptEntry, "Must specify an interaction!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        else if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        interaction.interaction.deferReply(isEphemeral).complete();
                        break;
                    }
                    case REPLY: {
                        if (interaction == null) {
                            Debug.echoError(scriptEntry, "Must specify an interaction!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        else if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        /*
                         * Messages aren't allowed to have attachments in ephemeral messages
                         * Since you can't see if the acknowledged message is ephemeral or not, this is a requirement so we don't have to try/catch
                         */
                        else if (message == null) {
                            Debug.echoError(scriptEntry, "Must have a message!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        MessageEmbed embed = null;
                        List<ActionRow> actionRows = DiscordMessageCommand.createRows(scriptEntry, rows);
                        if (message.asString().startsWith("discordembed@")) {
                            embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.getContext()).build();
                        }
                        if (interaction.interaction.isAcknowledged()) {
                            WebhookMessageAction<Message> action;
                            InteractionHook hook = interaction.interaction.getHook();
                            if (embed != null) {
                                action = hook.sendMessageEmbeds(embed);
                            }
                            else {
                                action = hook.sendMessage(message.asString());
                            }
                            if (attachFileName != null) {
                                if (attachFileText != null) {
                                    action.addFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                                }
                                else {
                                    Debug.echoError("Failed to send attachment - missing content?");
                                }
                            }
                            if (actionRows != null) {
                                action.addActionRows(actionRows);
                            }
                            action.complete();
                        } else {
                            ReplyAction action;
                            Interaction replyTo = interaction.interaction;
                            if (embed != null) {
                                action = replyTo.replyEmbeds(embed);
                            }
                            else {
                                action = replyTo.reply(message.asString());
                            }
                            if (attachFileName != null) {
                                if (attachFileText != null) {
                                    action = action.addFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                                }
                                else {
                                    Debug.echoError("Failed to send attachment - missing content?");
                                }
                            }
                            if (actionRows != null) {
                                action.addActionRows(actionRows);
                            }
                            action.setEphemeral(isEphemeral);
                            action.complete();
                        }
                        break;
                    }
                    case DELETE: {
                        if (interaction == null) {
                            Debug.echoError(scriptEntry, "Must specify an interaction!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        else if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        interaction.interaction.getHook().deleteOriginal().complete();
                        break;
                    }
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
            scriptEntry.setFinished(true);
        });
    }
}
