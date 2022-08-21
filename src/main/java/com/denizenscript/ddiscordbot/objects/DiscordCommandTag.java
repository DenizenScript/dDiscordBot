package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class DiscordCommandTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordCommandTag
    // @prefix discordcommand
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord commands is the bot ID (optional), followed by the guild ID (optional), followed by the command ID (required).
    // For example: 1234
    // Or: 12,1234
    // Or: mybot,12,1234
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordCommandTag is an object that represents a created slash command on Discord, as a bot-specific reference.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__commands"
    //
    // -->

    @Fetchable("discordcommand")
    public static DiscordCommandTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordcommand@")) {
            string = string.substring("discordcommand@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> commaSplit = CoreUtilities.split(string, ',');
        if (commaSplit.size() == 0 || commaSplit.size() > 3) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordCommandTag input is not valid.");
            }
            return null;
        }
        String cmdIdText = commaSplit.get(commaSplit.size() - 1);
        if (!ArgumentHelper.matchesInteger(cmdIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordCommandTag input is not a number.");
            }
            return null;
        }
        long cmdId = Long.parseLong(cmdIdText);
        if (cmdId == 0) {
            return null;
        }
        if (commaSplit.size() == 1) {
            return new DiscordCommandTag(null, 0, cmdId);
        }
        String grpIdText = commaSplit.get(commaSplit.size() - 2);
        if (!ArgumentHelper.matchesInteger(grpIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordCommandTag group ID input is not a number.");
            }
            return null;
        }
        long grpId = Long.parseLong(grpIdText);
        if (grpId == 0) {
            return null;
        }
        return new DiscordCommandTag(commaSplit.size() == 3 ? commaSplit.get(0) : null, grpId, cmdId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordcommand@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.lastIndexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public DiscordCommandTag(String bot, long guild_id, long command_id) {
        this.bot = bot;
        this.guild_id = guild_id;
        this.command_id = command_id;
    }

    public DiscordCommandTag(String bot, Guild guild, Command command) {
        this.bot = bot;
        this.guild = guild;
        this.guild_id = guild != null ? this.guild.getIdLong() : 0;
        this.command = command;
        this.command_id = this.command.getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public Guild getGuild() {
        if (guild != null) {
            return guild;
        }
        if (bot == null) {
            return null;
        }
        if (guild_id == 0) {
            return null;
        }
        guild = getBot().client.getGuildById(guild_id);
        return guild;
    }

    public Command getCommand() {
        if (command != null) {
            return command;
        }
        if (bot == null) {
            return null;
        }
        if (getGuild() != null) {
            command = getGuild().retrieveCommandById(command_id).complete();
        }
        else {
            command = getBot().client.retrieveCommandById(command_id).complete();
        }
        return command;
    }

    public String bot;

    public Guild guild;

    public Command command;

    public long guild_id;

    public long command_id;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__commands." + command_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordCommandTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID of the command.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.command_id);
        });

        // <--[tag]
        // @attribute <DiscordCommandTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the command.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.getCommand().getName());
        });

        // <--[tag]
        // @attribute <DiscordCommandTag.description>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the description of the command.
        // -->
        tagProcessor.registerTag(ElementTag.class, "description", (attribute, object) -> {
            return new ElementTag(object.getCommand().getDescription());
        });

        // <--[tag]
        // @attribute <DiscordCommandTag.options>
        // @returns ListTag(MapTag)
        // @plugin dDiscordBot
        // @description
        // Returns the option MapTags of the command. This is the same value as the one provided when creating a command, as documented in <@link command DiscordCommand>.
        // -->
        tagProcessor.registerTag(ListTag.class, "options", (attribute, object) -> {
            ListTag options = new ListTag();
            for (Command.Option option : object.getCommand().getOptions()) {
                MapTag map = new MapTag();
                map.putObject("type", new ElementTag(option.getType().toString().toLowerCase().replaceAll("_", "")));
                map.putObject("name", new ElementTag(option.getName()));
                map.putObject("description", new ElementTag(option.getDescription()));
                map.putObject("required", new ElementTag(option.isRequired()));
                if (option.getType() == OptionType.STRING || option.getType() == OptionType.INTEGER) {
                    ListTag choices = new ListTag();
                    for (Command.Choice choice : option.getChoices()) {
                        MapTag choiceData = new MapTag();
                        choiceData.putObject("name", new ElementTag(choice.getName()));
                        if (option.getType() == OptionType.STRING) {
                            choiceData.putObject("value", new ElementTag(choice.getAsString()));
                        }
                        else {
                            choiceData.putObject("value", new ElementTag(choice.getAsLong()));
                        }
                        choices.addObject(choiceData);
                    }
                    map.putObject("choices", choices);
                }
                options.addObject(map);
            }
            return options;
        });
    }

    public static ObjectTagProcessor<DiscordCommandTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordcommand";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordcommand@" + bot + "," + guild_id + "," + command_id;
        }
        if (guild_id != 0) {
            return "discordcommand@" + guild_id + "," + command_id;
        }
        return "discordcommand@" + command_id;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
