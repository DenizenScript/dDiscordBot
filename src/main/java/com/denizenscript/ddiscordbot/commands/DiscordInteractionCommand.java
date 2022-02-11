package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordEmbedTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DiscordInteractionCommand extends AbstractDiscordCommand implements Holdable {

    public DiscordInteractionCommand() {
        setName("discordinteraction");
        setSyntax("discordinteraction [defer/reply/edit/delete] [interaction:<interaction>] (ephemeral) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)");
        setRequiredArguments(2, 7);
        setPrefixesHandled("interaction", "rows", "attach_file_name", "attach_file_text");
        setBooleansHandled("ephemeral");
        isProcedural = false;
    }

    // <--[command]
    // @Name discordinteraction
    // @Syntax discordinteraction [defer/reply/delete] [interaction:<interaction>] (ephemeral) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)
    // @Required 2
    // @Maximum 7
    // @Short Manages Discord interactions.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Manages Discord interactions.
    //
    // You can defer, reply to, edit, or delete an interaction. These instructions all require the "interaction" argument.
    //
    // The "ephemeral" argument can be used to have the reply message be visible to that one user.
    //
    // You should usually defer an interaction before replying.
    // If you defer, the 'ephemeral' option can only be set by the defer - you cannot change it with the later reply.
    // Replying to an interaction uses similar logic to normal messaging. See <@link command discordmessage>.
    // If you deferred without using 'ephemeral', the 'delete' option will delete the "Thinking..." message.
    //
    // Slash commands, and replies to interactions, have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // Generally used alongside <@link command discordcommand>
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a slash command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to quickly reply to a slash command interaction.
    // - ~discordinteraction reply interaction:<context.interaction> "hello!"
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

    public enum DiscordInteractionInstruction { DEFER, REPLY, EDIT, DELETE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordInteractionInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("message")
                    && !arg.hasPrefix()) {
                scriptEntry.addObject("message", new ElementTag(arg.getRawValue()));
            }
            else {
                arg.reportUnhandled();
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag instruction = scriptEntry.getElement("instruction");
        DiscordInteractionTag interaction = scriptEntry.requiredArgForPrefix("interaction", DiscordInteractionTag.class);
        boolean ephemeral = scriptEntry.argAsBoolean("ephemeral");
        ElementTag attachFileName = scriptEntry.argForPrefixAsElement("attach_file_name", null);
        ElementTag attachFileText = scriptEntry.argForPrefixAsElement("attach_file_text", null);
        ListTag rows = scriptEntry.argForPrefix("rows", ListTag.class, true);
        ElementTag message = scriptEntry.getElement("message");
        if (scriptEntry.dbCallShouldDebug()) {
            // Note: attachFileText intentionally at end
            Debug.report(scriptEntry, getName(), instruction, interaction, ephemeral, rows, message, attachFileName, attachFileText);
        }
        DiscordInteractionInstruction instructionEnum = DiscordInteractionInstruction.valueOf(instruction.asString().toUpperCase());
        Runnable runner = () -> {
            try {
                switch (instructionEnum) {
                    case DEFER: {
                        if (interaction.interaction == null) {
                            handleError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        interaction.interaction.deferReply(ephemeral).complete();
                        break;
                    }
                    case EDIT:
                    case REPLY: {
                        if (interaction.interaction == null) {
                            handleError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        /*
                         * Messages aren't allowed to have attachments in ephemeral messages
                         * Since you can't see if the acknowledged message is ephemeral or not, this is a requirement so we don't have to try/catch
                         */
                        else if (message == null) {
                            handleError(scriptEntry, "Must have a message!");
                            return;
                        }
                        MessageEmbed embed = null;
                        List<ActionRow> actionRows = DiscordMessageCommand.createRows(scriptEntry, rows);
                        if (message.asString().startsWith("discordembed@")) {
                            embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.getContext()).build();
                        }
                        if (instructionEnum == DiscordInteractionInstruction.EDIT) {
                            WebhookMessageUpdateAction<Message> action;
                            InteractionHook hook = interaction.interaction.getHook();
                            if (embed != null) {
                                action = hook.editOriginalEmbeds(embed);
                            }
                            else {
                                action = hook.editOriginal(message.asString());
                            }
                            if (attachFileName != null) {
                                if (attachFileText != null) {
                                    action = action.addFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                                }
                                else {
                                    handleError(scriptEntry, "Failed to send attachment - missing content?");
                                }
                            }
                            if (actionRows != null) {
                                action = action.setActionRows(actionRows);
                            }
                            action.complete();
                        }
                        else if (interaction.interaction.isAcknowledged()) {
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
                                    action = action.addFile(attachFileText.asString().getBytes(StandardCharsets.UTF_8), attachFileName.asString());
                                }
                                else {
                                    handleError(scriptEntry, "Failed to send attachment - missing content?");
                                }
                            }
                            if (actionRows != null) {
                                action = action.addActionRows(actionRows);
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
                                    handleError(scriptEntry, "Failed to send attachment - missing content?");
                                }
                            }
                            if (actionRows != null) {
                                action = action.addActionRows(actionRows);
                            }
                            action = action.setEphemeral(ephemeral);
                            action.complete();
                        }
                        break;
                    }
                    case DELETE: {
                        if (interaction.interaction == null) {
                            handleError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        interaction.interaction.getHook().deleteOriginal().complete();
                        break;
                    }
                }
            }
            catch (Exception ex) {
                handleError(scriptEntry, ex);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
