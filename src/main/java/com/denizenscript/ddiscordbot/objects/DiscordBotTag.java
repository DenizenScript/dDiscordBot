package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import discord4j.core.object.entity.Guild;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class DiscordBotTag implements ObjectTag {

    // <--[language]
    // @name DiscordBotTag
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordBotTag is an object that represents a Discord bot powered by dDiscordBot.
    //
    // For format info, see <@link language discord@>
    //
    // -->

    // <--[language]
    // @name discord@
    // @group Object Fetcher System
    // @plugin dDiscordBot
    // @description
    // discord@ refers to the 'object identifier' of a DiscordBotTag. The 'discord@' is notation for Denizen's Object
    // Fetcher. The constructor for a DiscordBotTag is the bot ID (as chosen in <@link command discord>).
    // For example: mybot
    //
    // For general info, see <@link language DiscordBotTag>
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

    public static void registerTags() {

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
            return new DiscordUserTag(object.bot, connection.client.getSelf().block());

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
            for (Guild guild : connection.client.getGuilds().toIterable()) {
                list.addObject(new DiscordGroupTag(object.bot, guild));
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
            for (Guild guild : connection.client.getGuilds().toIterable()) {
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
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
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
