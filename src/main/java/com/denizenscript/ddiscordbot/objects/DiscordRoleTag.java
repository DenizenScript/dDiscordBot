package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.List;

public class DiscordRoleTag implements ObjectTag {

    // <--[language]
    // @name DiscordRoleTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordRoleTag is an object that represents a role on Discord, either as a generic reference,
    // or as a guild-specific reference, or as a bot+guild-specific reference.
    //
    // These use the object notation "discordrole@".
    // The identity format for Discord roles  is the bot ID (optional), followed by the guild ID (optional), followed by the role ID (required).
    // For example: 4321
    // Or: 1234,4321
    // Or: mybot,1234,4321
    //
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
            if (!ArgumentHelper.matchesInteger(input.get(0))) {
                if (context == null || context.debug) {
                    Debug.echoError("DiscordRoleTag input is not a number.");
                }
                return null;
            }
            long roleId = Long.parseLong(input.get(0));
            if (roleId == 0) {
                return null;
            }
            return new DiscordRoleTag(null, 0, roleId);
        }
        else if (input.size() == 3) {
            if (!ArgumentHelper.matchesInteger(input.get(1)) || !ArgumentHelper.matchesInteger(input.get(2))) {
                if (context == null || context.debug) {
                    Debug.echoError("DiscordRoleTag input is not a number.");
                }
                return null;
            }
            long guildId = Long.parseLong(input.get(1));
            long roleId = Long.parseLong(input.get(2));
            if (guildId == 0 || roleId == 0) {
                return null;
            }
            return new DiscordRoleTag(input.get(0), guildId, roleId);
        }
        else if (input.size() == 2) {
            if (!ArgumentHelper.matchesInteger(input.get(0)) || !ArgumentHelper.matchesInteger(input.get(1))) {
                if (context == null || context.debug) {
                    Debug.echoError("DiscordRoleTag input is not a number.");
                }
                return null;
            }
            long guildId = Long.parseLong(input.get(0));
            long roleId = Long.parseLong(input.get(1));
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
            return ArgumentHelper.matchesInteger(arg);
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
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.role.getName());

        });

        // <--[tag]
        // @attribute <DiscordRoleTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the role.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.role_id);

        });

        // <--[tag]
        // @attribute <DiscordRoleTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string the role.
        // -->
        registerTag("mention", (attribute, object) -> {
            return new ElementTag(object.role.getMention());

        });

        // <--[tag]
        // @attribute <DiscordRoleTag.group>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this role.
        // -->
        registerTag("group", (attribute, object) -> {
            return new DiscordGroupTag(object.bot, object.role.getGuild().block());

        });
    }

    public static ObjectTagProcessor<DiscordRoleTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordRoleTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
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
        if (guild_id != 0) {
            return "discordrole@" + guild_id + "," + role_id;
        }
        return "discordrole@" + role_id;
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
