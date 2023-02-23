package com.denizenscript.ddiscordbot.commands;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordCommandUtils;
import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.ddiscordbot.objects.DiscordCommandTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultText;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.util.List;
import java.util.Map;

public class DiscordCommandCommand extends AbstractCommand implements Holdable {

    public DiscordCommandCommand() {
        setName("discordcommand");
        setSyntax("discordcommand (id:<bot>) [create/delete] (group:<group>) (name:<name>) (type:{slash}/user/message) (description:<description>) (options:<options>)");
        setRequiredArguments(2, 7);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name discordcommand
    // @Syntax discordcommand (id:<bot>) [create/delete] (group:<group>) (name:<name>) (type:{slash}/user/message) (description:<description>) (options:<options>)
    // @Required 2
    // @Maximum 7
    // @Short Manages Discord application commands.
    // @Plugin dDiscordBot
    // @Guide https://guide.denizenscript.com/guides/expanding/ddiscordbot.html
    // @Group external
    //
    // @Description
    // Manages Discord application commands.
    //
    // You can create a new command, edit the permissions of an existing command, or delete an existing command.
    //
    // To create (or delete) a command in a specific Discord guild, use the "group" argument. If not present, a global command will be created. NOTE: Global commands take up to an hour to register.
    // When creating, both a name and description are required.
    //
    // Commands can be slash commands - activated via typing "/", message commands - activated by right-clicking a message, or user commands - activated by right-clicking a user.
    // "Description" and "options" are only valid for slash commands.
    //
    // The "options" argument controls the command parameters. It is a MapTag of ordered MapTags that can sometimes hold ordered MapTags. It is recommended to use <@link command definemap> or a data script key when creating commands.
    // All option MapTags must have "type", "name", and "description" keys, with an optional "required" key (defaulting to true). The "type" key can be one of: STRING, INTEGER, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE, NUMBER, ATTACHMENT.
    // Additionally, the option map can include a "choices" key, which is a MapTag of ordered MapTags that have a "name" (what displays to the user) and a "value" (what gets passed to the client).
    // Instead of choices, the option map can also include an "autocomplete" key controlling whether dynamic suggestions can be provided to the client (defaulting to false). See <@link event on discord command autocomplete>.
    //
    // Editing application command permissions has been moved to the "Integrations" section in the server settings.
    // Read more about it here: <@link url https://discord.com/blog/slash-commands-permissions-discord-apps-bots>
    //
    // You DO NOT need to create a command on startup every time! Once a command is created, it will persist until you delete it.
    // Using the "create" instruction on an existing command will update it.
    //
    // Commands and replies to interactions have limitations. See <@link url https://gist.github.com/MinnDevelopment/b883b078fdb69d0e568249cc8bf37fe9>.
    //
    // See also Discord's internal API documentation for commands: <@link url https://discord.com/developers/docs/interactions/application-commands>
    //
    // Generally used alongside <@link command discordinteraction>
    //
    // The command should usually be ~waited for. See <@link language ~waitable>.
    //
    // @Tags
    // <entry[saveName].command> returns the DiscordCommandTag of a command upon creation, when the command is ~waited for.
    //
    // @Usage
    // Use to create a simple slash command without options and save it.
    // - ~discordcommand create group:<discord[mybot].group[Denizen]> name:hello "description:Hello world!" save:mycmd
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
    // -->

    public enum DiscordCommandInstruction { CREATE, DELETE, PERMS }

    public static Command matchCommandByName(ScriptEntry scriptEntry, String name, JDA client, DiscordGroupTag group) {
        List<Command> retrievedCmds;
        if (group == null) {
            retrievedCmds = client.retrieveCommands().complete();
        }
        else {
            retrievedCmds = group.getGuild().retrieveCommands().complete();
        }
        String matchString = CoreUtilities.toLowerCase(name);
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

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgPrefixed @ArgName("id") @ArgDefaultNull DiscordBotTag rawBot,
                                   @ArgName("instruction") DiscordCommandInstruction instruction,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("group") DiscordGroupTag rawGroup,
                                   @ArgPrefixed @ArgName("name") String name,
                                   @ArgPrefixed @ArgDefaultText("slash") @ArgName("type") Command.Type type,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("description") String description,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("options") MapTag options,
                                   // Past-deprecated arguments
                                   @ArgPrefixed @ArgDefaultNull @ArgName("enabled") ElementTag enabled,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("enable_for") ListTag enableFor,
                                   @ArgPrefixed @ArgDefaultNull @ArgName("disable_for") ListTag disableFor) {
        final DiscordBotTag bot = DiscordCommandUtils.inferBot(rawBot, rawGroup);
        if (rawGroup != null && rawGroup.bot == null) {
            rawGroup = new DiscordGroupTag(bot.bot, rawGroup.guild_id);
        }
        final DiscordGroupTag group = rawGroup;
        if (enabled != null || enableFor != null || disableFor != null || instruction == DiscordCommandInstruction.PERMS) {
            DenizenDiscordBot.oldCommandPermissions.warn(scriptEntry);
        }
        JDA client = bot.getConnection().client;
        DiscordCommandUtils.cleanWait(scriptEntry, switch (instruction) {
            case CREATE -> {
                if (type == Command.Type.UNKNOWN) {
                    throw new InvalidArgumentsRuntimeException("Invalid command creation type!");
                }
                CommandData data;
                if (type == Command.Type.SLASH) {
                    if (description == null) {
                        throw new InvalidArgumentsRuntimeException("Must specify a description!");
                    }
                    data = Commands.slash(name, description);
                }
                else {
                    data = Commands.context(type, name);
                }
                if (options != null) {
                    if (!(data instanceof SlashCommandData) && !options.map.isEmpty()) {
                        throw new InvalidArgumentsRuntimeException("Command options are only valid for SLASH commands.");
                    }
                    for (ObjectTag optionObj : options.map.values()) {
                        MapTag option = optionObj.asType(MapTag.class, scriptEntry.getContext());
                        ElementTag typeStr = option.getElement("type");
                        if (typeStr == null) {
                            throw new InvalidArgumentsRuntimeException("Command options must specify a type!");
                        }
                        OptionType optionType = typeStr.asEnum(OptionType.class);
                        ElementTag optionName = option.getElement("name");
                        ElementTag optionDescription = option.getElement("description");
                        ElementTag optionIsRequired = option.getElement("required");
                        ElementTag optionIsAutocomplete = option.getElement("autocomplete");
                        boolean isAutocomplete = optionIsAutocomplete != null && optionIsAutocomplete.asBoolean();
                        MapTag optionChoices = option.getObjectAs("choices", MapTag.class, scriptEntry.context);
                        if (optionName == null) {
                            throw new InvalidArgumentsRuntimeException("Command options must specify a name!");
                        }
                        else if (optionDescription == null) {
                            throw new InvalidArgumentsRuntimeException("Command options must specify a description!");
                        }
                        if (isAutocomplete && optionChoices != null) {
                            throw new InvalidArgumentsRuntimeException("Command options cannot be autocompletable and have choices!");
                        }
                        if (optionType == OptionType.SUB_COMMAND) {
                            ((SlashCommandData) data).addSubcommands(new SubcommandData(optionName.asString(), optionDescription.asString()));
                        }
                        // TODO: support these later, needs recursive logic
                        /*
                        else if (optionType == OptionType.SUB_COMMAND_GROUP) {
                            data.addSubcommandGroups(new SubcommandGroupData(optionName.asString(), optionDescription.asString()));
                        }
                        */
                        else {
                            OptionData optionData = new OptionData(optionType, optionName.asString(), optionDescription.asString(), optionIsRequired == null || optionIsRequired.asBoolean(), isAutocomplete);
                            if (optionChoices != null) {
                                if (!optionType.canSupportChoices()) {
                                    throw new InvalidArgumentsRuntimeException("Command options with choices must be STRING, INTEGER, or NUMBER!");
                                }
                                for (Map.Entry<StringHolder, ObjectTag> subChoiceValue : optionChoices.map.entrySet()) {
                                    MapTag choice = subChoiceValue.getValue().asType(MapTag.class, scriptEntry.getContext());
                                    ElementTag choiceName = choice.getElement("name");
                                    ElementTag choiceValue = choice.getElement("value");
                                    if (choiceName == null) {
                                        throw new InvalidArgumentsRuntimeException("Command option choices must specify a name!");
                                    }
                                    else if (choiceValue == null) {
                                        throw new InvalidArgumentsRuntimeException("Command option choices must specify a value!");
                                    }
                                    if (optionType == OptionType.INTEGER) {
                                        optionData.addChoice(choiceName.asString(), choiceValue.asInt());
                                    }
                                    else if (optionType == OptionType.NUMBER) {
                                        optionData.addChoice(choiceName.asString(), choiceValue.asDouble());
                                    }
                                    else {
                                        optionData.addChoice(choiceName.asString(), choiceValue.asString());
                                    }
                                }
                            }
                            ((SlashCommandData) data).addOptions(optionData);
                        }
                    }
                }
                CommandCreateAction createAction;
                if (group == null) {
                    Debug.log("Registering a slash command globally may take up to an hour.");
                    createAction = (CommandCreateAction) client.upsertCommand(data);
                }
                else {
                    createAction = (CommandCreateAction) group.getGuild().upsertCommand(data);
                }
                yield createAction.onSuccess(s -> scriptEntry.saveObject("command", new DiscordCommandTag(bot.bot, group == null ? null : group.getGuild(), s)));
            }
            case DELETE -> {
                Command bestMatch = matchCommandByName(scriptEntry, name, client, group);
                if (bestMatch == null) {
                    yield null;
                }
                if (group == null) {
                    yield client.deleteCommandById(bestMatch.getIdLong());
                }
                else {
                    yield group.getGuild().deleteCommandById(bestMatch.getIdLong());
                }
            }
            case PERMS -> null;
        });
    }
}
