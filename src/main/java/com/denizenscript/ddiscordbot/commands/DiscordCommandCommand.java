package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscordCommandCommand extends AbstractDiscordCommand implements Holdable {

    public DiscordCommandCommand() {
        setName("discordcommand");
        setSyntax("discordcommand [id:<id>] [create/perms/delete] (group:<group>) (name:<name>) (description:<description>) (options:<options>) (enabled:{true}/false) (enable_for:<list>) (disable_for:<list>)");
        setRequiredArguments(3, 9);
        setPrefixesHandled("id");
        isProcedural = false;
    }

    // <--[command]
    // @Name discordcommand
    // @Syntax discordcommand [id:<id>] [create/perms/delete] (group:<group>) (name:<name>) (description:<description>) (options:<options>) (enabled:{true}/false) (enable_for:<list>) (disable_for:<list>)
    // @Required 3
    // @Maximum 9
    // @Short Manages Discord slash commands.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Manages Discord slash commands.
    //
    // You can manage slash commands using the "command" instruction, specifying further instructions.
    //
    // Using the "command" instruction, you can create a new slash command, or edit the permissions of or delete an existing command.
    //
    // To create (or delete) a command in a specific Discord guild, use the "group" argument. If not present, a global command will be created. NOTE: Global slash commands take up to an hour to register.
    // When creating, both a name and description are required.
    //
    // The "options" argument controls the command parameters. It is a MapTag of ordered MapTags that can sometimes hold ordered MapTags. It is recommended to use <@link command definemap> or a data script key when creating commands.
    // All option MapTags must have "type", "name", and "description" keys, with an optional "required" key (defaulting to true). The "type" key can be one of: STRING, INTEGER, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE.
    // Additionally, the option map can include a "choices" key, which is a MapTag of ordered MapTags that have a "name" (what displays to the user) and a "value" (what gets passed to the client).
    //
    // You can use the "enabled" argument to set whether the command should be enabled for everyone by default.
    // To edit the permissions of a command (who can use it, and who can't), use the "perms" instruction. Permissions MUST be edited AFTER creation.
    // Use the "enable_for" and "disable_for" arguments (ListTags of DiscordUserTags or DiscordRoleTags - note: actual objects not raw IDs) when editing permissions.
    //
    // You DO NOT need to create a command on startup every time! Once a command is created, it will persist until you delete it.
    // Using the "create" instruction on an existing command will update it.
    //
    // Slash commands and replies to interactions, have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // See also Discord's internal API documentation for commands: <@link url https://discord.com/developers/docs/interactions/application-commands>
    //
    // Generally used alongside <@link command discordinteraction>
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a slash command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to create a simple slash command without options, which is disabled by default, and save it.
    // - ~discordcommand id:mybot create group:<discord[mybot].group[Denizen]> name:hello "description:Hello world!" enabled:false save:mycmd
    // - debug log <entry[mycmd].command.name>
    //
    // @Usage
    // Use to create a global slash command with one option, using definemap.
    // - definemap options:
    //     1:
    //       type: string
    //       name: animal
    //       description: Your favorite animal
    //       required: true
    // - ~discordcommand id:mybot create name:animal "description:Pick your favorite!" options:<[options]>
    //
    // @Usage
    // Use to edit the permissions of a command.
    // - ~discordcommand id:mybot perms name:mycmd group:<discord[mybot].group[mygroup]> disable_for:<discord[mybot].group[mygroup].role[Muted]> enabled:true
    //
    // -->

    public enum DiscordCommandInstruction { CREATE, PERMS, DELETE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("instruction")
                    && arg.matchesEnum(DiscordCommandInstruction.values())) {
                scriptEntry.addObject("instruction", arg.asElement());
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
                    && arg.matchesPrefix("options")
                    && arg.matchesArgumentType(MapTag.class)) {
                scriptEntry.addObject("options", arg.asType(MapTag.class));
            }
            else if (!scriptEntry.hasObject("enabled")
                    && arg.matchesPrefix("enabled")) {
                scriptEntry.addObject("enabled", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("enable_for")
                    && arg.matchesPrefix("enable_for")) {
                scriptEntry.addObject("enable_for", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("disable_for")
                    && arg.matchesPrefix("disable_for")) {
                scriptEntry.addObject("disable_for", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("instruction")) {
            throw new InvalidArgumentsException("Must have an instruction!");
        }
    }

    public static Command matchCommandByName(ScriptEntry scriptEntry, ElementTag name, JDA client, DiscordGroupTag group) {
        List<Command> retrievedCmds;
        if (group == null) {
            retrievedCmds = client.retrieveCommands().complete();
        }
        else {
            retrievedCmds = group.getGuild().retrieveCommands().complete();
        }
        String matchString = CoreUtilities.toLowerCase(name.asString());
        Command bestMatch = null;
        for (Command cmd : retrievedCmds) {
            String commandName = cmd.getName();
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
                User user = DiscordUserTag.valueOf(item, scriptEntry.getContext()).getUser();
                if (enable) {
                    result = CommandPrivilege.enable(user);
                }
                else {
                    result = CommandPrivilege.disable(user);
                }
            }
            else if (item.startsWith("discordrole@")) {
                Role role = DiscordRoleTag.valueOf(item, scriptEntry.getContext()).role;
                if (enable) {
                    result = CommandPrivilege.enable(role);
                }
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
        DiscordBotTag bot = scriptEntry.requiredArgForPrefix("id", DiscordBotTag.class);
        ElementTag commandInstruction = scriptEntry.getElement("instruction");
        DiscordGroupTag group = scriptEntry.getObjectTag("group");
        ElementTag name = scriptEntry.getElement("name");
        ElementTag description = scriptEntry.getElement("description");
        MapTag options = scriptEntry.getObjectTag("options");
        ElementTag enabled = scriptEntry.getElement("enabled");
        ListTag enableFor = scriptEntry.getObjectTag("enable_for");
        ListTag disableFor = scriptEntry.getObjectTag("disable_for");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), bot, commandInstruction, group, name, description, options, enabled, enableFor, disableFor);
        }
        if (group != null && group.bot == null) {
            group.bot = bot.bot;
        }
        JDA client = bot.getConnection().client;
        Bukkit.getScheduler().runTaskAsynchronously(DenizenDiscordBot.instance, () -> {
            try {
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
                DiscordCommandInstruction commandInstructionEnum = DiscordCommandInstruction.valueOf(commandInstruction.asString().toUpperCase());
                boolean isEnabled = enabled == null || enabled.asBoolean();
                switch (commandInstructionEnum) {
                    case CREATE: {
                        if (description == null) {
                            Debug.echoError(scriptEntry, "Must specify a description!");
                            scriptEntry.setFinished(true);
                            return;
                        }
                        CommandData data = new CommandData(name.asString(), description.asString());
                        if (options != null) {
                            for (ObjectTag optionObj : options.map.values()) {
                                MapTag option = optionObj.asType(MapTag.class, scriptEntry.getContext());
                                ElementTag typeStr = (ElementTag) option.getObject("type");
                                if (typeStr == null) {
                                    Debug.echoError(scriptEntry, "Command options must specify a type!");
                                    scriptEntry.setFinished(true);
                                    return;
                                }
                                OptionType optionType = OptionType.valueOf(typeStr.toString().toUpperCase());
                                ElementTag optionName = (ElementTag) option.getObject("name");
                                ElementTag optionDescription = (ElementTag) option.getObject("description");
                                ElementTag optionIsRequired = (ElementTag) option.getObject("required");
                                MapTag optionChoices = (MapTag) option.getObject("choices");
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
                                        for (Map.Entry<StringHolder, ObjectTag> subChoiceValue : optionChoices.map.entrySet()) {
                                            MapTag choice = subChoiceValue.getValue().asType(MapTag.class, scriptEntry.getContext());
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
                        CommandCreateAction action;
                        if (group == null) {
                            Debug.log("Registering a slash command globally may take up to an hour.");
                            action = client.upsertCommand(data);
                        }
                        else {
                            action = group.getGuild().upsertCommand(data);
                        }
                        action.setDefaultEnabled(isEnabled);
                        Command slashCommand = action.complete();
                        scriptEntry.addObject("command", new DiscordCommandTag(bot.bot, group == null ? null : group.getGuild(), slashCommand));
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
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
            scriptEntry.setFinished(true);
        });
    }
}
