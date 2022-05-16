package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
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

public class DiscordModalCommand extends AbstractDiscordCommand implements Holdable {

    public DiscordModalCommand() {
        setName("discordmodal");
        setSyntax("discordmodal [interaction:<interaction>] [name:<name>] [rows:<rows>] (title:<title>)");
        setRequiredArguments(3, 4);
        setPrefixesHandled("interaction", "rows", "title", "name");
        isProcedural = false;
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
    // With this command you can respond to an interaction using a modal.
    //
    // You should usually defer an interaction before using a modal.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Usage
    // Use to create a modal from text replies.
    // - definemap rows:
    //      1:
    //          1: <discord_text_input.with[name].as[textinput].with[label].as[Type here!].with[style].as[short]>
    // - ~discordmodal interaction:<context.interaction> name:example_modal title:Modal! rows:<[rows]>
    //
    // -->

    @Override
    public void execute(ScriptEntry scriptEntry) {
        DiscordInteractionTag interaction = scriptEntry.requiredArgForPrefix("interaction", DiscordInteractionTag.class);
        ElementTag name = scriptEntry.requiredArgForPrefixAsElement("name");
        ElementTag title = scriptEntry.argForPrefixAsElement("title", "");
        ObjectTag rows = scriptEntry.requiredArgForPrefix("rows", ObjectTag.class);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), interaction, name, rows, title);
        }
        Runnable runner = () -> {
            try {
                if (interaction.interaction == null) {
                    handleError(scriptEntry, "Invalid interaction! Has it expired?");
                    return;
                }
                List<ActionRow> actionRows = createRows(scriptEntry, rows);
                if(actionRows == null || actionRows.isEmpty()) {
                    handleError(scriptEntry, "Invalid action rows!");
                    return;
                }
                if (!interaction.interaction.isAcknowledged()) {
                    IModalCallback replyTo = (IModalCallback) interaction.interaction;

                    Modal modal = Modal.create(name.toString(), title.toString())
                            .addActionRows(actionRows)
                            .build();

                    ModalCallbackAction action = replyTo.replyModal(modal);
                    action.complete();
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
