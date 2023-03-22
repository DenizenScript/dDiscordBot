package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.Permission;

import java.awt.Color;
import java.util.List;

public class DiscordRoleTag implements ObjectTag, FlaggableObject, Adjustable {

    // <--[ObjectType]
    // @name DiscordRoleTag
    // @prefix discordrole
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord roles  is the bot ID (optional), followed by the guild ID (optional), followed by the role ID (required).
    // For example: 4321
    // Or: 1234,4321
    // Or: mybot,1234,4321
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordRoleTag is an object that represents a role on Discord, either as a generic reference,
    // or as a guild-specific reference, or as a bot+guild-specific reference.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__roles"
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
                if (context == null || context.showErrors()) {
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
                if (context == null || context.showErrors()) {
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
                if (context == null || context.showErrors()) {
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
                role = conn.client.getRoleById(role_id);
            }
        }
    }

    public DiscordRoleTag(String bot, Role role) {
        this.bot = bot;
        this.role = role;
        role_id = role.getIdLong();
        guild_id = role.getGuild().getIdLong();
    }

    public Role role;

    public String bot;

    public long role_id;

    public long guild_id;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenDiscordBot.instance.connections.get(bot).flags, "__roles." + guild_id + "." + role_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordRoleTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the role.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.role.getName());
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the role.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.role_id);
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string of the role.
        // -->
        tagProcessor.registerTag(ElementTag.class, "mention", (attribute, object) -> {
            return new ElementTag(object.role.getAsMention());
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.group>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this role.
        // -->
        tagProcessor.registerTag(DiscordGroupTag.class, "group", (attribute, object) -> {
            return new DiscordGroupTag(object.bot, object.role.getGuild());
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.color>
        // @returns ColorTag
        // @plugin dDiscordBot
        // @description
        // Returns the display color of the role, if any.
        // -->
        tagProcessor.registerTag(ColorTag.class, "color", (attribute, object) -> {
            Color color = object.role.getColor();
            if (color == null) {
                return null;
            }
            return new ColorTag(color.getRed(), color.getGreen(), color.getBlue());
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.users>
        // @returns ListTag(DiscordUserTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all users with this role.
        // -->
        tagProcessor.registerTag(ListTag.class, "users", (attribute, object) -> {
            ListTag result = new ListTag();
            for (Member member : object.role.getGuild().getMembersWithRoles(object.role)) {
                result.addObject(new DiscordUserTag(object.bot, member.getUser()));
            }
            return result;
        });

        // <--[tag]
        // @attribute <DiscordRoleTag.permissions>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of permissions that the role provides for users. You can get a list of possible outputs here: <@link url https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/Permission.html>
        // -->
        tagProcessor.registerTag(ListTag.class, "permissions", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Permission perm : object.role.getPermissions()) {
                list.addObject(new ElementTag(perm));
            }
            return list;
        });
    }

    public static ObjectTagProcessor<DiscordRoleTag> tagProcessor = new ObjectTagProcessor<>();

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
    public String debuggable() {
        if (role != null) {
            return identify() + " <GR>(" + role.getName() + ")";
        }
        return identify();
    }

    @Override
    public boolean isUnique() {
        return true;
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

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to Discord roles.");
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }
}
