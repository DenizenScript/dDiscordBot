package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import discord4j.core.object.entity.*;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import discord4j.rest.util.Snowflake;

public class DiscordGroupTag implements ObjectTag {

    // <--[language]
    // @name DiscordGroupTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordGroupTag is an object that represents a group on Discord, either as a generic reference,
    // or as a bot-specific reference.
    //
    // Note that the correct name for what we call here a 'group' is inconsistent between different people.
    // The Discord API calls it a "guild" (for historical reasons, not called that by *people* anymore usually),
    // messages in the Discord app call it a "server" (which is a convenient name but is factually inaccurate, as they are not servers),
    // many people will simply say "a Discord" (which is awkward for branding and also would be confusing if used in documentation).
    // So we're going with "group" (which is still confusing because "group" sometimes refers to DM groups, but... it's good enough).
    //
    // These use the object notation "discordgroup@".
    // The identity format for Discord groups is the bot ID (optional), followed by the guild ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
    // -->

    @Fetchable("discordgroup")
    public static DiscordGroupTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordgroup@")) {
            string = string.substring("discordgroup@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        int comma = string.indexOf(',');
        String bot = null;
        if (comma > 0) {
            bot = CoreUtilities.toLowerCase(string.substring(0, comma));
            string = string.substring(comma + 1);
        }
        if (!ArgumentHelper.matchesInteger(string)) {
            if (context == null || context.debug) {
                Debug.echoError("DiscordGroupTag input is not a number.");
            }
            return null;
        }
        long grpId = Long.parseLong(string);
        if (grpId == 0) {
            return null;
        }
        return new DiscordGroupTag(bot, grpId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordgroup@")) {
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

    public DiscordGroupTag(String bot, long guildId) {
        this.bot = bot;
        this.guild_id = guildId;
    }

    public DiscordGroupTag(String bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        guild_id = guild.getId().asLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public DiscordConnection.GuildCache getCacheGuild() {
        return getBot().getCachedGuild(guild_id);
    }

    public Guild getGuild() {
        if (guild != null) {
            return guild;
        }
        guild = getBot().client.getGuildById(Snowflake.of(guild_id)).block();
        return guild;
    }

    public Guild guild;

    public String bot;

    public long guild_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordGroupTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the group.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.getCacheGuild().name);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the group.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.guild_id);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.channels>
        // @returns ListTag(DiscordChannelTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all channels in the group.
        // -->
        registerTag("channels", (attribute, object) -> {
            ListTag list = new ListTag();
            for (DiscordConnection.ChannelCache chan : object.getCacheGuild().channels) {
                list.addObject(new DiscordChannelTag(object.bot, chan.id));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.members>
        // @returns ListTag(DiscordUserTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all users in the group.
        // -->
        registerTag("members", (attribute, object) -> {
            ListTag list = new ListTag();
            for (DiscordConnection.UserCache member : object.getCacheGuild().users) {
                list.addObject(new DiscordUserTag(object.bot, member.id));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.roles>
        // @returns ListTag(DiscordRoleTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all roles in the group.
        // -->
        registerTag("roles", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Role role : object.getGuild().getRoles().toIterable()) {
                list.addObject(new DiscordRoleTag(object.bot, role));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.member[<name>]>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the group member that best matches the input name, or null if there's no match.
        // For input of username#id, will always only match for the exact user.
        // For input of only the username, return value might be unexpected if multiple members have the same username
        // (this happens more often than you might expect - many users accidentally join new Discord groups from the
        // web on a temporary web account, then rejoin on a local client with their 'real' account).
        // -->
        registerTag("member", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getContext(1));
            int discrimMark = matchString.indexOf('#');
            String discrimVal = null;
            if (discrimMark > 0 && discrimMark == matchString.length() - 5) {
                discrimVal = matchString.substring(discrimMark + 1);
                matchString = matchString.substring(0, discrimMark);
            }
            final String discrim = discrimVal;
            final String matchName = matchString;
            for (DiscordConnection.UserCache user : object.getCacheGuild().users) {
                if (user.username.equalsIgnoreCase(matchName) && (discrim == null || user.discriminator.equals(discrim))) {
                    return new DiscordUserTag(object.bot, user.id);
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.channel[<name>]>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the channel that best matches the input name, or null if there's no match.
        // -->
        registerTag("channel", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getContext(1));
            DiscordConnection.ChannelCache bestMatch = null;
            for (DiscordConnection.ChannelCache chan : object.getCacheGuild().channels) {
                String chanName = CoreUtilities.toLowerCase(chan.name);
                if (matchString.equals(chanName)) {
                    bestMatch = chan;
                    break;
                }
                if (chanName.contains(matchString)) {
                    bestMatch = chan;
                }
            }
            if (bestMatch == null) {
                return null;
            }
            return new DiscordChannelTag(object.bot, bestMatch.id);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.role[<name>]>
        // @returns DiscordRoleTag
        // @plugin dDiscordBot
        // @description
        // Returns the role that best matches the input name, or null if there's no match.
        // -->
        registerTag("role", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getContext(1));
            Role bestMatch = null;
            for (Role role : object.getGuild().getRoles().toIterable()) {
                String roleName = CoreUtilities.toLowerCase(role.getName());
                if (matchString.equals(roleName)) {
                    bestMatch = role;
                    break;
                }
                if (roleName.contains(matchString)) {
                    bestMatch = role;
                }
            }
            if (bestMatch == null) {
                return null;
            }
            return new DiscordRoleTag(object.bot, bestMatch);
        });
    }

    public static ObjectTagProcessor<DiscordGroupTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordGroupTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordgroup";

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
        return "DiscordGroup";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordgroup@" + bot + "," + guild_id;
        }
        return "discordgroup@" + guild_id;
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
