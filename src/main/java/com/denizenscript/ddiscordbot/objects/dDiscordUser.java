package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.dDiscordBot;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.HashMap;

public class dDiscordUser implements dObject {

    @Fetchable("discorduser")
    public static dDiscordUser valueOf(String string, TagContext context) {
        if (string.startsWith("discorduser@")) {
            string = string.substring("discorduser@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        int comma = string.indexOf(',');
        String bot = null;
        if (comma > 0) {
            bot = string.substring(0, comma);
            string = string.substring(comma + 1);
        }
        long usrId = aH.getLongFrom(string);
        if (usrId == 0) {
            return null;
        }
        return new dDiscordUser(bot, usrId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discorduser@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.indexOf(',');
        if (comma == -1) {
            return aH.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return aH.matchesInteger(arg.substring(comma + 1));
    }

    public dDiscordUser(String bot, long userId) {
        this.bot = bot;
        this.user_id = userId;
        if (bot != null) {
            DiscordConnection conn = dDiscordBot.instance.connections.get(bot);
            if (conn != null) {
                conn.client.getUserById(Snowflake.of(user_id)).block();
            }
        }
    }

    public dDiscordUser(String bot, User user) {
        this.bot = bot;
        this.user = user;
        user_id = user.getId().asLong();
    }

    public User user;

    public String bot;

    public long user_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <discorduser@user.name>
        // @returns Element
        // @description
        // Returns the user name of the user.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordUser) object).user.getUsername())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discorduser@user.id>
        // @returns Element(Number)
        // @description
        // Returns the ID number of the user.
        // -->
        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordUser) object).user.getId().asLong())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discorduser@user.mention>
        // @returns Element(Number)
        // @description
        // Returns the raw mention string for the user.
        // -->
        registerTag("mention", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordUser) object).user.getMention())
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
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        return new Element(identify()).getAttribute(attribute);
    }

    String prefix = "discorduser";

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
        return "DiscordUser";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discorduser@" + bot + "," + user_id;
        }
        return "discorduser@" + user_id;
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
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
