package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.DiscordEmbedTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.*;

import java.util.List;

public class DiscordInteractionCommand extends AbstractCommand implements Holdable {

    public DiscordInteractionCommand() {
        setName("discordinteraction");
        setSyntax("discordinteraction [defer/defer_update/reply/update/edit/delete] [interaction:<interaction>] (ephemeral) (rows:<rows>) (<message>) (embed:<embed>|...) (attach_files:<map>)");
        setRequiredArguments(2, 7);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordinteraction
    // @Syntax discordinteraction [defer/defer_update/reply/update/edit/delete] [interaction:<interaction>] (ephemeral) (rows:<rows>) (<message>) (embed:<embed>|...) (attach_files:<map>)
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
    // You can defer, defer_update, reply to, update, edit, or delete an interaction. These instructions all require the 'interaction' argument.
    //
    // The 'ephemeral' argument can be used to have the reply message be visible to that one user.
    //
    // 'defer' acknowledges an interaction with the normal 'Thinking...' state before a later reply, which is useful if your reply may take more than a few seconds to be selected.
    // If you defer, the 'ephemeral' option can only be set by the defer - you cannot change it with the later reply.
    // 'defer_update' acknowledges a component interaction without a loading state, allowing its original message to be edited later.
    // 'reply' provides the initial interaction response, or sends a followup after the interaction has been acknowledged. It uses similar logic to normal messaging. See <@link command discordmessage>.
    // 'update' immediately updates the message associated with a component interaction as its initial response.
    // 'edit' edits the interaction's original response through its interaction hook.
    // 'delete' deletes the interaction's original response through its interaction hook.
    // If you deferred without using 'ephemeral', the 'delete' option will delete the 'Thinking...' message.
    // Ephemeral replies cannot have files.
    //
    // Slash commands, and replies to interactions, have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // Generally used alongside <@link command discordcommand>
    //
    // The command can be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a slash command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to quickly reply to a slash command interaction.
    // - discordinteraction reply interaction:<context.interaction> hello!
    //
    // @Usage
    // Use to defer and reply to a slash command interaction.
    // - ~discordinteraction defer interaction:<context.interaction>
    // - discordinteraction reply interaction:<context.interaction> <context.options.get[hello].if_null[world]>
    //
    // @Usage
    // Use to defer and reply to an interaction ephemerally.
    // - ~discordinteraction defer interaction:<context.interaction> ephemeral:true
    // - discordinteraction reply interaction:<context.interaction> "Shh, don't tell anyone!"
    //
    // @Usage
    // Use to immediately update the message associated with a component interaction.
    // - discordinteraction update interaction:<context.interaction> Updated!
    //
    // @Usage
    // Use to silently acknowledge a component interaction, then update its original message later.
    // - ~discordinteraction defer_update interaction:<context.interaction>
    // - wait 2s
    // - discordinteraction edit interaction:<context.interaction> "Updated after processing."
    //
    // -->

    public enum DiscordInteractionInstruction { DEFER, DEFER_UPDATE, REPLY, UPDATE, EDIT, DELETE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("instruction") DiscordInteractionInstruction instruction,
                                   @ArgPrefixed @ArgName("interaction") DiscordInteractionTag interaction,
                                   @ArgName("ephemeral") boolean ephemeral,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("rows") ObjectTag rows,
                                   @ArgRaw @ArgLinear @ArgDefaultNull @ArgName("message") ObjectTag message,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("embed") @ArgSubType(DiscordEmbedTag.class) List<DiscordEmbedTag> embeds,
                                   // Note: attachFiles intentionally at end
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_files") MapTag attachFilesMap,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_name") String attachFileName,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("attach_file_text") String attachFileText) {
        if (interaction.interaction == null) {
            throw new InvalidArgumentsRuntimeException("Invalid interaction! Has it expired?");
        }
        DiscordCommandUtils.cleanWait(scriptEntry, switch (instruction) {
            case DEFER -> {
                if (!(interaction.interaction instanceof IReplyCallback)) {
                    throw new InvalidArgumentsRuntimeException("Interaction is not a reply callback!");
                }
                yield ((IReplyCallback) interaction.interaction).deferReply(ephemeral);
            }
            case DEFER_UPDATE -> {
                if (!(interaction.interaction instanceof IMessageEditCallback editCallback)) {
                    throw new InvalidArgumentsRuntimeException("Interaction is not a message edit callback!");
                }
                yield editCallback.deferEdit();
            }
            case EDIT -> {
                AbstractMessageBuilder<?, ?> messageBuilder = DiscordMessageCommand.createMessageBuilder(scriptEntry, true, false, rows, message, embeds, attachFileName, attachFileText, attachFilesMap);
                InteractionHook hook = ((IDeferrableCallback) interaction.interaction).getHook();
                yield (RestAction<?>) hook.editOriginal((MessageEditData) messageBuilder.build());
            }
            case REPLY -> {
                MessageCreateBuilder messageBuilder = (MessageCreateBuilder) DiscordMessageCommand.createMessageBuilder(scriptEntry, false, false, rows, message, embeds, attachFileName, attachFileText, attachFilesMap);
                if (interaction.interaction.isAcknowledged()) {
                    InteractionHook hook = ((IDeferrableCallback) interaction.interaction).getHook();
                    yield hook.sendMessage(messageBuilder.build());
                }
                else {
                    IReplyCallback replyTo = (IReplyCallback) interaction.interaction;
                    yield replyTo.reply(messageBuilder.build()).setEphemeral(ephemeral);
                }
            }
            case UPDATE -> {
                if (!(interaction.interaction instanceof IMessageEditCallback editCallback)) {
                    throw new InvalidArgumentsRuntimeException("Interaction is not a message edit callback!");
                }
                AbstractMessageBuilder<?, ?> messageBuilder = DiscordMessageCommand.createMessageBuilder(scriptEntry, true, false, rows, message, embeds, attachFileName, attachFileText, attachFilesMap);
                yield editCallback.editMessage((MessageEditData) messageBuilder.build());
            }
            case DELETE -> {
                yield ((IDeferrableCallback) interaction.interaction).getHook().deleteOriginal();
            }
        });
    }
}
