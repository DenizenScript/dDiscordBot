package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.stream.Collectors;

public class DiscordGroupTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordGroupTag
    // @prefix discordgroup
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord groups is the bot ID (optional), followed by the guild ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
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
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__guilds"
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
            if (context == null || context.showErrors()) {
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
        guild_id = guild.getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public Guild getGuild() {
        if (guild != null) {
            return guild;
        }
        guild = getBot().client.getGuildById(guild_id);
        return guild;
    }

    public Guild guild;

    public String bot;

    public long guild_id;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__guilds." + guild_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordGroupTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the group.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.getGuild().getName());
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the group.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.guild_id);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.channels>
        // @returns ListTag(DiscordChannelTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all channels in the group.
        // -->
        tagProcessor.registerTag(ListTag.class, "channels", (attribute, object) -> {
            ListTag list = new ListTag();
            for (GuildChannel chan : object.getGuild().getChannels()) {
                list.addObject(new DiscordChannelTag(object.bot, chan));
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
        tagProcessor.registerTag(ListTag.class, "members", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Member member : object.getGuild().getMembers()) {
                list.addObject(new DiscordUserTag(object.bot, member.getUser()));
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
        tagProcessor.registerTag(ListTag.class, "roles", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Role role : object.getGuild().getRoles()) {
                list.addObject(new DiscordRoleTag(object.bot, role));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.commands>
        // @returns ListTag(DiscordCommandTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all commands in the group.
        // -->
        tagProcessor.registerTag(ListTag.class, "commands", (attribute, object) -> {
            ListTag list = new ListTag();
            for (Command command : object.getGuild().retrieveCommands().complete()) {
                list.addObject(new DiscordCommandTag(object.bot, object.getGuild(), command));
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
        tagProcessor.registerTag(DiscordUserTag.class, "member", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getParam());
            int discrimMark = matchString.indexOf('#');
            String discrimVal = null;
            if (discrimMark > 0 && discrimMark == matchString.length() - 5) {
                discrimVal = matchString.substring(discrimMark + 1);
                matchString = matchString.substring(0, discrimMark);
            }
            final String discrim = discrimVal;
            final String matchName = matchString;
            for (Member user : object.getGuild().getMembers()) {
                if (user.getUser().getName().equalsIgnoreCase(matchName) && (discrim == null || user.getUser().getDiscriminator().equals(discrim))) {
                    return new DiscordUserTag(object.bot, user.getUser());
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
        tagProcessor.registerTag(DiscordChannelTag.class, "channel", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getParam());
            GuildChannel bestMatch = null;
            for (GuildChannel chan : object.getGuild().getChannels()) {
                String chanName = CoreUtilities.toLowerCase(chan.getName());
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
            return new DiscordChannelTag(object.bot, bestMatch);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.role[<name>]>
        // @returns DiscordRoleTag
        // @plugin dDiscordBot
        // @description
        // Returns the role that best matches the input name, or null if there's no match.
        // -->
        tagProcessor.registerTag(DiscordRoleTag.class, "role", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getParam());
            Role bestMatch = null;
            for (Role role : object.getGuild().getRoles()) {
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

        // <--[tag]
        // @attribute <DiscordGroupTag.command[<name>]>
        // @returns DiscordCommandTag
        // @plugin dDiscordBot
        // @description
        // Returns the guild command that best matches the input name, or null if there's no match.
        // -->
        tagProcessor.registerTag(DiscordCommandTag.class, "command", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getParam());
            Command bestMatch = null;
            for (Command command : object.getGuild().retrieveCommands().complete()) {
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
            return new DiscordCommandTag(object.bot, object.getGuild(), bestMatch);
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.emoji_names>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of emoji names in the group.
        // -->
        tagProcessor.registerTag(ListTag.class, "emoji_names", (attribute, object) -> {
            ListTag result = new ListTag();
            for (Emote emote : object.getGuild().getEmotes()) {
                result.add(emote.getName());
            }
            return result;
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.emoji_id[<name>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the ID of the emoji that best matches the input name, or null if there's no match.
        // -->
        tagProcessor.registerTag(ElementTag.class, "emoji_id", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            String matchString = CoreUtilities.toLowerCase(attribute.getParam());
            Emote bestMatch = null;
            for (Emote emote : object.getGuild().getEmotes()) {
                String emoteName = CoreUtilities.toLowerCase(emote.getName());
                if (matchString.equals(emoteName)) {
                    bestMatch = emote;
                    break;
                }
                if (emoteName.contains(matchString)) {
                    bestMatch = emote;
                }
            }
            if (bestMatch == null) {
                return null;
            }
            return new ElementTag(bestMatch.getId());
        });

        // <--[tag]
        // @attribute <DiscordGroupTag.users_with_roles[<role>|...]>
        // @returns ListTag(DiscordUserTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all users in the group who have all the specified roles.
        // -->
        tagProcessor.registerTag(ListTag.class, "users_with_roles", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            List<Role> roles = attribute.paramAsType(ListTag.class).filter(DiscordRoleTag.class, attribute.context).stream().map(roleTag -> roleTag.role).collect(Collectors.toList());
            ListTag result = new ListTag();
            for (Member member : object.guild.getMembersWithRoles(roles)) {
                result.addObject(new DiscordUserTag(object.bot, member.getUser()));
            }
            return result;
        });
    }

    public static ObjectTagProcessor<DiscordGroupTag> tagProcessor = new ObjectTagProcessor<>();

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
    public String debuggable() {
        if (guild != null) {
            return identify() + " <GR>(" + guild.getName() + ")";
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
