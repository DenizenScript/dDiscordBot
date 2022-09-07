package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordEmbedTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DiscordInteractionCommand extends AbstractCommand implements Holdable {

    public DiscordInteractionCommand() {
        setName("discordinteraction");
        setSyntax("discordinteraction [defer/reply/edit/delete] [interaction:<interaction>] (ephemeral) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)");
        setRequiredArguments(2, 7);
        setPrefixesHandled("interaction", "rows", "attach_file_name", "attach_file_text");
        isProcedural = false;
        autoCompile();
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
    // You can defer an interaction before replying, which is useful if your reply may take more than a few seconds to be selected.
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

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("instruction") DiscordInteractionInstruction instruction,
                                   @ArgPrefixed @ArgName("interaction") DiscordInteractionTag interaction,
                                   @ArgName("ephemeral") boolean ephemeral,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_name") String attachFileName,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_text") String attachFileText,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("rows") ObjectTag rows,
                                   @ArgLinear @ArgDefaultNull @ArgName("message") ObjectTag message) {
        Runnable runner = () -> {
            try {
                switch (instruction) {
                    case DEFER: {
                        if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        if (interaction.interaction instanceof IReplyCallback) {
                            ((IReplyCallback) interaction.interaction).deferReply(ephemeral).complete();
                        } else {
                            Debug.echoError(scriptEntry, "Interaction is not a reply callback!");
                        }
                        break;
                    }
                    case EDIT:
                    case REPLY: {
                        if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        /*
                         * Messages aren't allowed to have attachments in ephemeral messages
                         * Since you can't see if the acknowledged message is ephemeral or not, this is a requirement so we don't have to try/catch
                         */
                        else if (message == null) {
                            Debug.echoError(scriptEntry, "Must have a message!");
                            return;
                        }
                        MessageEmbed embed = null;
                        List<ActionRow> actionRows = DiscordMessageCommand.createRows(scriptEntry, rows);
                        if (message.shouldBeType(DiscordEmbedTag.class)) {
                            embed = message.asType(DiscordEmbedTag.class, scriptEntry.context).build(scriptEntry.getContext()).build();
                        }
                        MessageRequest<?> action;
                        InteractionHook hook = ((IDeferrableCallback) interaction.interaction).getHook();
                        FileUpload fileUpload = null;
                        if (attachFileName != null) {
                            if (attachFileText != null) {
                                fileUpload = FileUpload.fromData(attachFileText.getBytes(StandardCharsets.UTF_8), attachFileName);
                            }
                            else {
                                Debug.echoError(scriptEntry, "Failed to process attachment - missing content?");
                            }
                        }
                        if (instruction == DiscordInteractionInstruction.EDIT) {
                            if (embed != null) {
                                action = hook.editOriginalEmbeds(embed);
                            }
                            else {
                                action = hook.editOriginal(message.toString());
                            }
                        }
                        else if (interaction.interaction.isAcknowledged()) {
                            if (embed != null) {
                                action = hook.sendMessageEmbeds(embed);
                            }
                            else {
                                action = hook.sendMessage(message.toString());
                            }
                        } else {
                            IReplyCallback replyTo = (IReplyCallback) interaction.interaction;
                            if (embed != null) {
                                action = replyTo.replyEmbeds(embed);
                            }
                            else {
                                action = replyTo.reply(message.toString());
                            }
                            action = ((ReplyCallbackAction) action).setEphemeral(ephemeral);
                        }
                        if (fileUpload != null) {
                            action = action.setFiles(fileUpload);
                        }
                        if (actionRows != null) {
                            action = action.setComponents(actionRows);
                        }
                        if (action instanceof WebhookMessageEditAction<?>) {
                            ((WebhookMessageEditAction<?>) action).complete();
                        }
                        else if (action instanceof WebhookMessageCreateAction<?>) {
                            ((WebhookMessageCreateAction<?>) action).complete();
                        }
                        else {
                            ((ReplyCallbackAction) action).complete();
                        }
                        break;
                    }
                    case DELETE: {
                        if (interaction.interaction == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            return;
                        }
                        ((IDeferrableCallback) interaction.interaction).getHook().deleteOriginal().complete();
                        break;
                    }
                }
            }
            catch (Exception ex) {
                Debug.echoError(scriptEntry, ex);
            }
        };
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            runner.run();
            scriptEntry.setFinished(true);
        });
    }
}
