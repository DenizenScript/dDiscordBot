package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscordModalCommand extends AbstractCommand implements Holdable {

    public DiscordModalCommand() {
        setName("discordmodal");
        setSyntax("discordmodal [interaction:<interaction>] [name:<name>] [rows:<rows>] (title:<title>)");
        setRequiredArguments(3, 4);
        setPrefixesHandled("interaction", "rows", "title", "name");
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
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // Note that the interaction can be any button or application command, but cannot be a modal submission - you cannot reply to a modal submit with a second modal.
    //
    // @Usage
    // Use to create a modal that only requests one single direct text input.
    // - definemap rows:
    //      1:
    //          1: <discord_text_input.with[id].as[textinput].with[label].as[Type here!].with[style].as[short]>
    // - ~discordmodal interaction:<context.interaction> name:example_modal title:Modal! rows:<[rows]>
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("interaction") DiscordInteractionTag interaction,
                                   @ArgPrefixed @ArgName("name") String name,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("title") String title,
                                   @ArgPrefixed @ArgName("rows") ObjectTag rows) {
        Runnable runner = () -> {
            try {
                if (interaction.interaction == null) {
                    Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                    return;
                }
                List<ActionRow> actionRows = createRows(scriptEntry, rows);
                if (actionRows == null || actionRows.isEmpty()) {
                    Debug.echoError(scriptEntry, "Invalid action rows!");
                    return;
                }
                if (!interaction.interaction.isAcknowledged()) {
                    IModalCallback replyTo = (IModalCallback) interaction.interaction;
                    Modal modal = Modal.create(name, title).addActionRows(actionRows).build();
                    ModalCallbackAction action = replyTo.replyModal(modal);
                    action.complete();
                }
                else {
                    Debug.echoError(scriptEntry, "Interaction already acknowledged!");
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

    public static List<ActionRow> createRows(ScriptEntry scriptEntry, ObjectTag rowsObj) {
        if (rowsObj == null) {
            return null;
        }
        Collection<ObjectTag> rows = CoreUtilities.objectToList(rowsObj, scriptEntry.getContext());
        List<ActionRow> actionRows = new ArrayList<>();
        for (ObjectTag row : rows) {
            List<ItemComponent> components = new ArrayList<>();
            for (ObjectTag component : CoreUtilities.objectToList(row, scriptEntry.getContext())) {
                if (component.canBeType(DiscordTextInputTag.class)) {
                    components.add(component.asType(DiscordTextInputTag.class, scriptEntry.getContext()).build());
                }
                else {
                    Debug.echoError("Unsupported modal component list entry '" + component + "'");
                }
            }
            actionRows.add(ActionRow.of(components));
        }
        return actionRows;
    }
}
