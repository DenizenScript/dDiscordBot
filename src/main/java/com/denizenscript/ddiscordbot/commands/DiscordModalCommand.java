package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.modals.Modal;

import java.util.ArrayList;
import java.util.Collection;

public class DiscordModalCommand extends AbstractCommand implements Holdable {

    public DiscordModalCommand() {
        setName("discordmodal");
        setSyntax("discordmodal [interaction:<interaction>] [name:<name>] [rows:<rows>] (title:<title>)");
        setRequiredArguments(3, 4);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordmodal
    // @Syntax discordmodal [interaction:<interaction>] [name:<name>] [rows:<rows>] (title:<title>)
    // @Required 3
    // @Maximum 4
    // @Short Manages Discord modals.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Tags
    // None
    //
    // @Description
    // With this command you can respond to an interaction using a modal.
    // A "modal" is a popup window that presents the user with a form to fill out.
    //
    // You can specify the modal's internal name for matching with in events.
    // You can specify the title as text to display to the user.
    // You can specify rows of user-inputs using <@link objecttype DiscordTextInputTag>. At time of writing, Selection input is not supported.
    //
    // You can listen to the responses to forms using <@link event discord modal submitted>.
    //
    // You cannot defer an interaction before using a modal. It must be sent immediately.
    //
    // Note that the interaction can be any button or application command, but cannot be a modal submission - you cannot reply to a modal submit with a second modal.
    //
    // The command can be ~waited for. See <@link language ~waitable>.
    //
    // @Usage
    // Use to create a modal that only requests one single direct text input.
    // - definemap rows:
    //      1:
    //          1: <discord_text_input.with[id].as[textinput].with[label].as[Type here!].with[style].as[short]>
    // - discordmodal interaction:<context.interaction> name:example_modal title:Modal! rows:<[rows]>
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("interaction") DiscordInteractionTag interaction,
                                   @ArgPrefixed @ArgName("name") String name,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("title") String title,
                                   @ArgPrefixed @ArgName("rows") ObjectTag rows) {
        if (interaction.interaction == null) {
            throw new InvalidArgumentsRuntimeException("Invalid interaction! Has it expired?");
        }
        Collection<Label> modalRows = createRows(scriptEntry, rows);
        if (modalRows == null || modalRows.isEmpty()) {
            throw new InvalidArgumentsRuntimeException("Invalid action rows!");
        }
        if (interaction.interaction.isAcknowledged()) {
            throw new InvalidArgumentsRuntimeException("Interaction already acknowledged!");
        }
        IModalCallback replyTo = (IModalCallback) interaction.interaction;
        Modal modal = Modal.create(name, title).addComponents(modalRows).build();
        DiscordCommandUtils.cleanWait(scriptEntry, replyTo.replyModal(modal));
    }

    public static Collection<Label> createRows(ScriptEntry scriptEntry, ObjectTag rowsObj) {
        if (rowsObj == null) {
            return null;
        }
        Collection<ObjectTag> rows = CoreUtilities.objectToList(rowsObj, scriptEntry.getContext());
        Collection<Label> modalRows = new ArrayList<>();
        for (ObjectTag row : rows) {
            for (ObjectTag component : CoreUtilities.objectToList(row, scriptEntry.getContext())) {
                if (component.canBeType(DiscordTextInputTag.class)) {
                    DiscordTextInputTag textInput = component.asType(DiscordTextInputTag.class, scriptEntry.getContext());
                    Label label = Label.of(textInput.textInputData.getElement("label").asString(), textInput.build());
                    modalRows.add(label);
                }
                else {
                    Debug.echoError("Unsupported modal component list entry '" + component + "'");
                }
            }
        }
        return modalRows;
    }
}
