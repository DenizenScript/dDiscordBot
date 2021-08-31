package com.denizenscript.ddiscordbot.commands;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordCommandTag;
import com.denizenscript.ddiscordbot.objects.DiscordEmbedTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordInteractionTag;
import com.denizenscript.ddiscordbot.objects.DiscordRoleTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;

import org.bukkit.Bukkit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

public class DiscordInteractionCommand extends AbstractCommand implements Holdable {

    public DiscordInteractionCommand() {
        setName("discordinteraction");
        setSyntax("discordinteraction [id:<id>] [command [create/perms/delete]/defer/reply/delete] (group:<group>) (name:<name>) (description:<description>) (options:<options>) (enabled:{true}/false) (enable_for:<list>) (disable_for:<list>) (interaction:<interaction>) (ephermal:true/{false}) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)");
        setRequiredArguments(3, 10);
    }

    // <--[command]
    // @Name discordinteraction
    // @Syntax discordinteraction [id:<id>] [command [create/perms/delete]/defer/reply/delete] (group:<group>) (name:<name>) (description:<description>) (options:<options>) (enabled:{true}/false) (enable_for:<list>) (disable_for:<list>) (interaction:<interaction>) (ephermal:true/{false}) (attach_file_name:<name>) (attach_file_text:<text>) (rows:<rows>) (<message>)
    // @Required 3
    // @Maximum 10
    // @Short Manages Discord interactions.
    // @Plugin dDiscordBot
    // @Group external
    //
    // @Description
    // Manages Discord interactions.
    //
    // You can manage slash commands using the "command" instruction, specifying further instructions.
    //
    // Using the "command" instruction, you can create a new slash command, or edit the permissions of or delete an existing command.
    //
    // To create (or delete) a command in a specific Discord guild, use the "group" argument. If not present, a global command will be created. NOTE: Global slash commands take up to an hour to register.
    // When creating, both a name and description are required.
    //
    // The "options" argument controls the command parameters. It is a ListTag of MapTags that can sometimes hold ListTags. It is recommended to use external YAML files as a simpler form of data representation when creating commands. See <@link command yaml>.
    // All option MapTags must have "type", "name", and "description" keys, with an optional "required" key (defaulting to true). The "type" key can be one of: STRING, INTEGER, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE, SUBCOMMAND, or SUBCOMMANDGROUP.
    // Additionally, the option map can include a "choices" key, which is a ListTag of MapTags that have a "name" (what displays to the user) and a "value" (what gets passed to you).
    // 
    // You can use the "enabled" argument to set whether the command should be enabled for everyone by default.
    // To edit the permissions of a command (who can use it, and who can't), use the "perms" instruction. Permissions MUST be edited AFTER creation.
    // Use the "enable_for" and "disable_for" arguments (ListTags of DiscordUserTags or DiscordRoleTags) when editing permissions.
    //
    // You DO NOT need to create a command on startup every time! Once a command is created, it will persist until deleted.
    // Using the "create" instruction on an existing command will update it.
    //
    // Otherwise, you can defer, reply to, or delete an interaction. These instructions all require the "interaction" argument.
    //
    // The "ephermal" argument can be used to only have the reply message be visible to the user.
    //
    // You should almost always defer an interaction before replying. A defer will override a reply in terms of being ephermal.
    // Replying to an interaction uses similar logic to normal messaging. See <@link command discordmessage>.
    //
    // Slash commands, and replies to interactions, have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a slash command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to create a simple slash command without options, which is disabled by default, and save it.
    // - ~discordinteraction id:mybot command create group:<discord[mybot].group[Denizen]> name:hello "description:Hello world!" enabled:false save:mycmd
    // - debug log <entry[mycmd].command.name>
    //
    // @Usage
    // Use to create a global slash command with one option.
    // - ~discordinteraction id:mybot command create name:animal "description:Pick your favorite!" "options:<list_single[<map[type=string;name=animal;description=Your favorite animal]>]>"
    //
    // @Usage
    // Use to edit the permissions of a command.
    // - ~discordinteraction id:mybot command perms name:mycmd group:<discord[mybot].group[mygroup]> disable_for:<discord[mybot].group[mygroup].role[Muted]> enabled:true
    //
    // @Usage
    // Use to defer and reply to a slash command interaction.
    // - ~discordinteraction id:mybot defer interaction:<context.interaction>
    // - ~discordinteraction id:mybot reply interaction:<context.interaction> <context.options.get[hello].if_null[world]>
    //
    // @Usage
    // Use to defer and reply to an interaction ephermally.
    // - ~discordinteraction id:mybot defer interaction:<context.interaction> ephermal:true
    // - ~discordinteraction id:mybot reply interaction:<context.interaction> "Shh, don't tell anyone!"
    //
    // -->

    public enum DiscordInteractionInstruction { COMMAND, DEFER, REPLY, DELETE }
    public enum DiscordInteractionCommandInstruction { CREATE, PERMS, DELETE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordInteractionInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("command_instruction")
                    && arg.matchesEnum(DiscordInteractionCommandInstruction.values())) {
                scriptEntry.addObject("command_instruction", arg.asElement());
            }
            else if (!scriptEntry.hasObject("group")
                    && arg.matchesPrefix("group")
                    && arg.matchesArgumentType(DiscordGroupTag.class)) {
                scriptEntry.addObject("group", arg.asType(DiscordGroupTag.class));
            }
            else if (!scriptEntry.hasObject("name")
                    && arg.matchesPrefix("name")) {
                scriptEntry.addObject("name", new ElementTag(CoreUtilities.toLowerCase(arg.getValue())));
            }
            else if (!scriptEntry.hasObject("description")
                    && arg.matchesPrefix("description")) {
                scriptEntry.addObject("description", arg.asElement());
            }
            else if (!scriptEntry.hasObject("options")
                    && arg.matchesPrefix("options")) {
                scriptEntry.addObject("options", arg.asType(ListTag.class).filter(MapTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("enabled")
                    && arg.matchesPrefix("enabled")) {
                scriptEntry.addObject("enabled", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("enable_for")
                    && arg.matchesPrefix("enable_for")) {
                scriptEntry.addObject("enable_for", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()));
            }
            else if (!scriptEntry.hasObject("disable_for")
                    && arg.matchesPrefix("disable_for")) {
                scriptEntry.addObject("disable_for", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()));
            }
            else if (!scriptEntry.hasObject("interaction")
                    && arg.matchesPrefix("interaction")
                    && arg.matchesArgumentType(DiscordInteractionTag.class)) {
                scriptEntry.addObject("interaction", arg.asType(DiscordInteractionTag.class));
            }
            else if (!scriptEntry.hasObject("ephermal")
                    && arg.matchesPrefix("ephermal")) {
                scriptEntry.addObject("ephermal", new ElementTag(arg.getValue()));
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
                scriptEntry.addObject("rows", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()).filter(ListTag.class, scriptEntry));
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

    Command matchCommandByName(ScriptEntry scriptEntry, ElementTag name, JDA client, DiscordGroupTag group) {
        List<Command> retrievedCmds = null;
        if (group == null) {
            retrievedCmds = client.retrieveCommands().complete();
        }
        else {
            retrievedCmds = group.getGuild().retrieveCommands().complete();
        }
        String matchString = CoreUtilities.toLowerCase(name.asString());
        Command bestMatch = null;
        for (Command cmd : retrievedCmds) {
            String commandName = CoreUtilities.toLowerCase(cmd.getName());
            if (matchString.equals(commandName)) {
                bestMatch = cmd;
                break;
            }
            if (commandName.contains(matchString)) {
                bestMatch = cmd;
            }
        }
        if (bestMatch == null) {
            Debug.echoError(scriptEntry, "Invalid command name!");
            scriptEntry.setFinished(true);
            return null;
        }
        return bestMatch;
    }

    void addPrivileges(ScriptEntry scriptEntry, boolean enable, ListTag actionFor, List<CommandPrivilege> privileges) {
        for (String item : actionFor) {
            CommandPrivilege result = null;
            if (item.startsWith("discorduser@")) {
                result = CommandPrivilege.enable(DiscordUserTag.valueOf(item, scriptEntry.getContext()).getUser());
            } 
            else if (item.startsWith("discordrole@")) {
                result = CommandPrivilege.enable(DiscordRoleTag.valueOf(item, scriptEntry.getContext()).role);
            }
            if (result != null) {
                privileges.add(result);
            }
            else {
                Debug.echoError("Privileged input must be a DiscordUserTag or DiscordRoleTag!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("id");
        ElementTag instruction = scriptEntry.getElement("instruction");
        ElementTag commandInstruction = scriptEntry.getElement("command_instruction");
        DiscordGroupTag group = scriptEntry.getObjectTag("group");
        ElementTag name = scriptEntry.getElement("name");
        ElementTag description = scriptEntry.getElement("description");
        List<MapTag> options = (List<MapTag>) scriptEntry.getObjectTag("options");
        ElementTag enabled = scriptEntry.getElement("enabled");
        ListTag enableFor = scriptEntry.getObjectTag("enable_for");
        ListTag disableFor = scriptEntry.getObjectTag("disable_for");
        DiscordInteractionTag interaction = scriptEntry.getObjectTag("interaction");
        ElementTag ephermal = scriptEntry.getElement("ephermal");
        ElementTag attachFileName = scriptEntry.getElement("attach_file_name");
        ElementTag attachFileText = scriptEntry.getElement("attach_file_text");
        List<ListTag> rows = (List<ListTag>) scriptEntry.getObjectTag("rows");
        ElementTag message = scriptEntry.getElement("message");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, instruction, commandInstruction, group, name, description, options != null ? ArgumentHelper.debugList("options", options) : options, enabled, enableFor, disableFor, interaction, ephermal, attachFileName, attachFileText, rows != null ? ArgumentHelper.debugList("rows", rows) : rows, message);
        }
        DiscordInteractionInstruction instructionEnum = DiscordInteractionInstruction.valueOf(instruction.asString().toUpperCase());
        if (!DenizenDiscordBot.instance.connections.containsKey(id.asString())) {
            Debug.echoError("Failed to process DiscordInteraction command: unknown bot ID!");
            scriptEntry.setFinished(true);
            return;
        }
        JDA client = DenizenDiscordBot.instance.connections.get(id.asString()).client;
        boolean isEphermal = ephermal != null && ephermal.asBoolean();
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            try {
                switch (instructionEnum) {
                    case COMMAND: {
                        if (commandInstruction == null) {
                            Debug.echoError(scriptEntry, "Must have a command instruction!");
                            scriptEntry.setFinished(true);
                            return;
                        } 
                        else if (name == null) {
                            Debug.echoError(scriptEntry, "Must specify a name!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        DiscordInteractionCommandInstruction commandInstructionEnum = DiscordInteractionCommandInstruction.valueOf(commandInstruction.asString().toUpperCase());
                        boolean isEnabled = enabled == null ? true : enabled.asBoolean();
                        switch (commandInstructionEnum) {
                            case CREATE: {
                                if (description == null) {
                                    Debug.echoError(scriptEntry, "Must specify a description!");
                                    scriptEntry.setFinished(true);
                                    return;
                                }
                                CommandData data = new CommandData(name.asString(), description.asString());
                                if (options != null) {
                                    for (MapTag option : options) {
                                        ElementTag typeStr = (ElementTag) option.getObject("type");
                                        if (typeStr == null) {
                                            Debug.echoError(scriptEntry, "Command options must specify a type!");
                                            scriptEntry.setFinished(true);
                                            return;
                                        }
                                        OptionType optionType = null;
                                        for (OptionType val : OptionType.values()) {
                                            if (val.name().toUpperCase().replace("_", "").equals(typeStr.asString().toUpperCase().replace("_", ""))) {
                                                optionType = val;
                                                break;
                                            }
                                        }
                                        if (optionType == null) {
                                            Debug.echoError(scriptEntry, "Invalid option type!");
                                            scriptEntry.setFinished(true);
                                            return;
                                        }
                                        ElementTag optionName = (ElementTag) option.getObject("name");
                                        ElementTag optionDescription = (ElementTag) option.getObject("description");
                                        ElementTag optionIsRequired = (ElementTag) option.getObject("required");
                                        ListTag optionChoices = (ListTag) option.getObject("choices");
                                        if (optionName == null) {
                                            Debug.echoError(scriptEntry, "Command options must specify a name!");
                                            scriptEntry.setFinished(true);
                                            return;
                                        } 
                                        else if (optionDescription == null) {
                                            Debug.echoError(scriptEntry, "Command options must specify a description!");
                                            scriptEntry.setFinished(true);
                                            return;
                                        }
                                        if (optionType == OptionType.SUB_COMMAND) {
                                            data.addSubcommands(new SubcommandData(optionName.asString(), optionDescription.asString()));
                                        }
                                        /*
                                        support these later
                                        needs recursive logic

                                        else if (optionType == OptionType.SUB_COMMAND_GROUP) {
                                            data.addSubcommandGroups(new SubcommandGroupData(optionName.asString(), optionDescription.asString()));
                                        }
                                        */
                                        else {
                                            OptionData optionData = new OptionData(optionType, optionName.asString(), optionDescription.asString(), optionIsRequired == null ? true : optionIsRequired.asBoolean());
                                            if (optionChoices != null) {
                                                if (optionType != OptionType.STRING && optionType != OptionType.INTEGER) {
                                                    Debug.echoError(scriptEntry, "Command options with choices must be either STRING or INTEGER!");
                                                    scriptEntry.setFinished(true);
                                                    return;
                                                }
                                                for (MapTag choice : optionChoices.filter(MapTag.class, scriptEntry.getContext())) {
                                                    ElementTag choiceName = (ElementTag) choice.getObject("name");
                                                    ElementTag choiceValue = (ElementTag) choice.getObject("value");
                                                    if (choiceName == null) {
                                                        Debug.echoError(scriptEntry, "Command option choices must specify a name!");
                                                        scriptEntry.setFinished(true);
                                                        return;
                                                    } 
                                                    else if (choiceValue == null) {
                                                        Debug.echoError(scriptEntry, "Command option choices must specify a value!");
                                                        scriptEntry.setFinished(true);
                                                        return;
                                                    }
                                                    if (optionType == OptionType.STRING) {
                                                        optionData.addChoice(choiceName.asString(), choiceValue.asString());
                                                    }
                                                    else {
                                                        optionData.addChoice(choiceName.asString(), choiceValue.asInt());
                                                    }
                                                }
                                            }
                                            data.addOptions(optionData);
                                        }   
                                    }
                                }
                                CommandCreateAction action = null;
                                if (group == null) {
                                    Debug.log("Registering a slash command globally may take up to an hour.");
                                    action = client.upsertCommand(data);
                                } 
                                else {
                                    action = group.getGuild().upsertCommand(data);
                                }
                                action.setDefaultEnabled(isEnabled);
                                Command slashCommand = action.complete();
                                scriptEntry.addObject("command", new DiscordCommandTag(id.asString(), group == null ? null : group.getGuild(), slashCommand));
                                break;
                            }
                            case PERMS: {
                                if (enableFor == null && disableFor == null) {
                                    Debug.echoError(scriptEntry, "Must specify privileges!");
                                    scriptEntry.setFinished(true);
                                    return;
                                } 
                                else if (group == null) {
                                    Debug.echoError(scriptEntry, "Must specify a group!");
                                    scriptEntry.setFinished(true);
                                    return;
                                }
                                Command bestMatch = matchCommandByName(scriptEntry, name, client, group);
                                if (bestMatch == null) {
                                    return;
                                }
                                List<CommandPrivilege> privileges = new ArrayList<>();
                                if (enableFor != null) {
                                    addPrivileges(scriptEntry, true, enableFor, privileges);
                                }
                                if (disableFor != null) {
                                    addPrivileges(scriptEntry, false, disableFor, privileges);
                                }                            
                                bestMatch.editCommand().setDefaultEnabled(isEnabled).complete();
                                group.guild.updateCommandPrivilegesById(bestMatch.getIdLong(), privileges).complete();
                                break;
                            }
                            case DELETE: {
                                Command bestMatch = matchCommandByName(scriptEntry, name, client, group);
                                if (bestMatch == null) {
                                    return;
                                }
                                if (group == null) {
                                    client.deleteCommandById(bestMatch.getIdLong()).complete();
                                } 
                                else {
                                    group.getGuild().deleteCommandById(bestMatch.getIdLong()).complete();
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case DEFER: {
                        if (interaction == null) {
                            Debug.echoError(scriptEntry, "Must specify an interaction!");
                            scriptEntry.setFinished(true);
                            return;
                        } 
                        else if (interaction.getInteraction() == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        interaction.getInteraction().deferReply(isEphermal).complete();
                        break;
                    }
                    case REPLY: {
                        if (interaction == null) {
                            Debug.echoError(scriptEntry, "Must specify an interaction!");
                            scriptEntry.setFinished(true);
                            return;
                        } 
                        else if (interaction.getInteraction() == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        /**
                         * Messages aren't allowed to have attachments in ephermal messages
                         * Since you can't see if the acknowledged message is ephermal or not, this is a requirement so we don't have to try/catch
                         */
                        else if (message == null) {
                            Debug.echoError(scriptEntry, "Must have a message!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        MessageEmbed embed = null;
                        List<ActionRow> actionRows = DiscordMessageCommand.createRows(scriptEntry, rows);
                        if (message != null && message.asString().startsWith("discordembed@")) {
                            embed = DiscordEmbedTag.valueOf(message.asString(), scriptEntry.context).build(scriptEntry.getContext()).build();
                        }
                        if (interaction.getInteraction().isAcknowledged()) {
                            WebhookMessageAction<Message> action = null;
                            InteractionHook hook = interaction.getInteraction().getHook();
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
                            if (action == null) {
                                Debug.echoError("Failed to send message - missing content?");
                                scriptEntry.setFinished(true);
                                return;
                            }
                            if (actionRows != null) {
                                action.addActionRows(actionRows);
                            }
                            action.complete();
                        } else {
                            ReplyAction action = null;
                            Interaction replyTo = interaction.getInteraction();
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
                            if (action == null) {
                                Debug.echoError("Failed to send message - missing content?");
                                scriptEntry.setFinished(true);
                                return;
                            }
                            if (actionRows != null) {
                                action.addActionRows(actionRows);
                            }
                            action.setEphemeral(isEphermal);
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
                        else if (interaction.getInteraction() == null) {
                            Debug.echoError(scriptEntry, "Invalid interaction! Has it expired?");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        interaction.getInteraction().getHook().deleteOriginal().complete();
                        break;
                    }
                }
            } catch (Exception e) {
                Debug.echoError(e);
            }
            scriptEntry.setFinished(true);
        });
    }
}
