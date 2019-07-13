package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.dDiscordBot;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import discord4j.core.object.entity.Guild;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.HashMap;

public class dDiscordConnection implements ObjectTag {

    @Fetchable("discord")
    public static dDiscordConnection valueOf(String string, TagContext context) {
        if (string.startsWith("discord@")) {
            string = string.substring("discord@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string);
        if (!dDiscordBot.instance.connections.containsKey(string)) {
            return null;
        }
        return new dDiscordConnection(string);
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

    public dDiscordConnection(String bot) {
        this.bot = bot;
    }

    public String bot;

    public static void registerTags() {

        // <--[tag]
        // @attribute <discord@bot.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the bot.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((dDiscordConnection) object).bot)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discord@bot.groups>
        // @returns ListTag(DiscordGroup)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all groups (aka 'guilds' or 'servers') that this Discord bot has access to.
        // -->
        registerTag("groups", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                DiscordConnection connection = dDiscordBot.instance.connections.get(((dDiscordConnection) object).bot);
                if (connection == null) {
                    return null;
                }
                ListTag list = new ListTag();
                for (Guild guild : connection.client.getGuilds().toIterable()) {
                    list.addObject(new dDiscordGroup(((dDiscordConnection) object).bot, guild));
                }
                return list.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discord@bot.group[<name>]>
        // @returns DiscordGroup
        // @plugin dDiscordBot
        // @description
        // Returns the Discord group (aka 'guild' or 'server') that best matches the input name, or null if there's no match.
        // -->
        registerTag("group", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                DiscordConnection connection = dDiscordBot.instance.connections.get(((dDiscordConnection) object).bot);
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
                return new dDiscordGroup(((dDiscordConnection) object).bot, bestMatch)
                        .getAttribute(attribute.fulfill(1));
            }
        });
    }

        public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        return new ElementTag(identify()).getAttribute(attribute);
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
