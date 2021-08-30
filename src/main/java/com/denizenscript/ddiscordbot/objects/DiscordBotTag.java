package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

public class DiscordBotTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordBotTag
    // @prefix discord
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord bots is the bot ID (as chosen in <@link command discord>).
    // For example: mybot
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordBotTag is an object that represents a Discord bot powered by dDiscordBot.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat
    //
    // -->

    @Fetchable("discord")
    public static DiscordBotTag valueOf(String string, TagContext context) {
        if (string.startsWith("discord@")) {
            string = string.substring("discord@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string);
        if (!DenizenDiscordBot.instance.connections.containsKey(string)) {
            return null;
        }
        return new DiscordBotTag(string);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discord@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.indexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public DiscordBotTag(String bot) {
        this.bot = bot;
    }

    public String bot;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return DenizenDiscordBot.instance.connections.get(bot).flags;
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordBotTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the bot.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.bot);

        });

        // <--[tag]
        // @attribute <DiscordBotTag.self_user>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the bot's own Discord user object.
        // -->
        registerTag("self_user", (attribute, object) -> {
            DiscordConnection connection = DenizenDiscordBot.instance.connections.get(object.bot);
            if (connection == null) {
                return null;
            }
            return new DiscordUserTag(object.bot, connection.client.getSelfUser());

        });

        // <--[tag]
        // @attribute <DiscordBotTag.groups>
        // @returns ListTag(DiscordGroupTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all groups (aka 'guilds' or 'servers') that this Discord bot has access to.
        // -->
        registerTag("groups", (attribute, object) -> {
            DiscordConnection connection = DenizenDiscordBot.instance.connections.get(object.bot);
            if (connection == null) {
                return null;
            }
            ListTag list = new ListTag();
            for (Guild guild : connection.client.getGuilds()) {
                list.addObject(new DiscordGroupTag(object.bot, guild));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordBotTag.commands>
        // @returns ListTag(DiscordCommandTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all application commands.
        // -->
        registerTag("commands", (attribute, object) -> {
            DiscordConnection connection = DenizenDiscordBot.instance.connections.get(object.bot);
            if (connection == null) {
                return null;
            }
            ListTag list = new ListTag();
            for (Command command : connection.client.retrieveCommands().complete()) {
                list.addObject(new DiscordCommandTag(object.bot, null, command));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordBotTag.group[<name>]>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the Discord group (aka 'guild' or 'server') that best matches the input name, or null if there's no match.
        // -->
        registerTag("group", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordConnection connection = DenizenDiscordBot.instance.connections.get(object.bot);
            if (connection == null) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getContext(1));
            Guild bestMatch = null;
            for (Guild guild : connection.client.getGuilds()) {
                String guildName = CoreUtilities.toLowerCase(guild.getName());
                if (matchString.equals(guildName)) {
                    bestMatch = guild;
                    break;
                }
                if (guildName.contains(matchString)) {
                    bestMatch = guild;
                }
            }
            if (bestMatch == null) {
                return null;
            }
            return new DiscordGroupTag(object.bot, bestMatch);

        });

        // <--[tag]
        // @attribute <DiscordBotTag.command[<name>]>
        // @returns DiscordCommandTag
        // @plugin dDiscordBot
        // @description
        // Returns the application command that best matches the input name, or null if there's no match.
        // -->
        registerTag("command", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordConnection connection = DenizenDiscordBot.instance.connections.get(object.bot);
            if (connection == null) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getContext(1));
            Command bestMatch = null;
            for (Command command : connection.client.retrieveCommands().complete()) {
                String commandName = CoreUtilities.toLowerCase(command.getName());
                if (matchString.equals(commandName)) {
                    bestMatch = command;
                    break;
                }
                if (commandName.contains(matchString)) {
                    bestMatch = command;
                }
            }
            if (bestMatch == null) {
                return null;
            }
            return new DiscordCommandTag(object.bot, null, bestMatch);
        });
    }

    public static ObjectTagProcessor<DiscordBotTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordBotTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discord";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "Discord";
    }

    @Override
    public String identify() {
        return "discord@" + bot;
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
