package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.List;

public class DiscordRoleTag implements ObjectTag {

    // <--[language]
    // @name DiscordRoleTag
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordRoleTag is an object that represents a role on Discord, either as a generic reference,
    // or as a guild-specific reference, or as a bot+guild-specific reference.
    //
    // For format info, see <@link language discordrole@>
    //
    // -->

    // <--[language]
    // @name discordrole@
    // @group Object Fetcher System
    // @plugin dDiscordBot
    // @description
    // discordrole@ refers to the 'object identifier' of a DiscordRoleTag. The 'discordrole@' is notation for Denizen's Object
    // Fetcher. The constructor for a DiscordRoleTag is the bot ID (optional), followed by the guild ID (optional), followed by the role ID (required).
    // For example: 4321
    // Or: 1234,4321
    // Or: mybot,1234,4321
    //
    // For general info, see <@link language DiscordRoleTag>
    // -->

    @Fetchable("discordrole")
    public static DiscordRoleTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordrole@")) {
            string = string.substring("discordrole@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> input = CoreUtilities.split(string, ',');
        if (input.size() == 1) {
            long roleId = ArgumentHelper.getLongFrom(input.get(0));
            if (roleId == 0) {
                return null;
            }
            return new DiscordRoleTag(null, 0, roleId);
        }
        else if (input.size() == 3) {
            long guildId = ArgumentHelper.getLongFrom(input.get(1));
            long roleId = ArgumentHelper.getLongFrom(input.get(2));
            if (guildId == 0 || roleId == 0) {
                return null;
            }
            return new DiscordRoleTag(input.get(0), guildId, roleId);
        }
        else if (input.size() == 2) {
            long guildId = ArgumentHelper.getLongFrom(input.get(0));
            long roleId = ArgumentHelper.getLongFrom(input.get(1));
            if (guildId == 0 || roleId == 0) {
                return null;
            }
            return new DiscordRoleTag(null, guildId, roleId);
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
            return ArgumentHelper.matchesInteger(after) && ArgumentHelper.matchesInteger(arg.substring(0, comma));
        }
        if (secondComma == after.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(after.substring(secondComma + 1)) && ArgumentHelper.matchesInteger(after.substring(0, secondComma));
    }

    public DiscordRoleTag(String bot, long guildId, long roleId) {
        if (bot != null) {
            bot = CoreUtilities.toLowerCase(bot);
        }
        this.bot = bot;
        this.guild_id = guildId;
        this.role_id = roleId;
        if (bot != null) {
            DiscordConnection conn = DenizenDiscordBot.instance.connections.get(bot);
            if (conn != null) {
                role = conn.client.getRoleById(Snowflake.of(guild_id), Snowflake.of(role_id)).block();
            }
        }
    }

    public DiscordRoleTag(String bot, Role role) {
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
        // @attribute <DiscordRoleTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the role.
        // -->
        registerTag("name", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordRoleTag) object).role.getName())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the role.
        // -->
        registerTag("id", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordRoleTag) object).role_id)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string the role.
        // -->
        registerTag("mention", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordRoleTag) object).role.getMention())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.group>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this role.
        // -->
        registerTag("group", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new DiscordGroupTag(((DiscordRoleTag) object).bot, ((DiscordRoleTag) object).role.getGuild().block())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });
    }

    public static ObjectTagProcessor tagProcessor = new ObjectTagProcessor();

    public static void registerTag(String name, TagRunnable.ObjectForm runnable) {
        tagProcessor.registerTag(name, runnable);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
