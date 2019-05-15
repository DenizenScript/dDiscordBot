package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.dDiscordBot;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.HashMap;
import java.util.List;

public class dDiscordRole implements dObject {

    @Fetchable("discordrole")
    public static dDiscordRole valueOf(String string, TagContext context) {
        if (string.startsWith("discordrole@")) {
            string = string.substring("discordrole@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> input = CoreUtilities.split(string, ',');
        if (input.size() == 1) {
            long roleId = aH.getLongFrom(input.get(0));
            if (roleId == 0) {
                return null;
            }
            return new dDiscordRole(null, 0, roleId);
        }
        else if (input.size() == 3) {
            long guildId = aH.getLongFrom(input.get(1));
            long roleId = aH.getLongFrom(input.get(2));
            if (guildId == 0 || roleId == 0) {
                return null;
            }
            return new dDiscordRole(input.get(0), guildId, roleId);
        }
        else if (input.size() == 2) {
            long guildId = aH.getLongFrom(input.get(0));
            long roleId = aH.getLongFrom(input.get(1));
            if (guildId == 0 || roleId == 0) {
                return null;
            }
            return new dDiscordRole(null, guildId, roleId);
        }
        else {
            return null;
        }
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordrole@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.indexOf(',');
        if (comma == -1) {
            return false;
        }
        String after = arg.substring(comma + 1);
        int secondComma = after.indexOf(',');
        if (secondComma == -1) {
            return aH.matchesInteger(after) && aH.matchesInteger(arg.substring(0, comma));
        }
        if (secondComma == after.length() - 1) {
            return false;
        }
        return aH.matchesInteger(after.substring(secondComma + 1)) && aH.matchesInteger(after.substring(0, secondComma));
    }

    public dDiscordRole(String bot, long guildId, long roleId) {
        if (bot != null) {
            bot = CoreUtilities.toLowerCase(bot);
        }
        this.bot = bot;
        this.guild_id = guildId;
        this.role_id = roleId;
        if (bot != null) {
            DiscordConnection conn = dDiscordBot.instance.connections.get(bot);
            if (conn != null) {
                role = conn.client.getRoleById(Snowflake.of(guild_id), Snowflake.of(role_id)).block();
            }
        }
    }

    public dDiscordRole(String bot, Role role) {
        this.bot = bot;
        this.role = role;
        role_id = role.getId().asLong();
        guild_id = role.getGuildId().asLong();
    }

    public Role role;

    public String bot;

    public long role_id;

    public long guild_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <discordrole@role.name>
        // @returns Element
        // @plugin dDiscordBot
        // @description
        // Returns the name of the role.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordRole) object).role.getName())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordrole@role.id>
        // @returns Element(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the role.
        // -->
        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordRole) object).role_id)
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordrole@role.mention>
        // @returns Element
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string the role.
        // -->
        registerTag("mention", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dDiscordRole) object).role.getMention())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <discordrole@role.group>
        // @returns DiscordGroup
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this role.
        // -->
        registerTag("group", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new dDiscordGroup(((dDiscordRole) object).bot, ((dDiscordRole) object).role.getGuild().block())
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

    String prefix = "discordrole";

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
        return "DiscordRole";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordrole@" + bot + "," + guild_id + "," + role_id;
        }
        return "discordrole@" + guild_id + "," + role_id;
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
