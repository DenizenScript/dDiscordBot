package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;

public class DiscordUserTag implements ObjectTag, FlaggableObject, Adjustable {

    // <--[ObjectType]
    // @name DiscordUserTag
    // @prefix discorduser
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord users is the bot ID (optional), followed by the user ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordUserTag is an object that represents a user (human or bot) on Discord, either as a generic reference,
    // or as a bot-specific reference.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__users"
    //
    // -->

    @Fetchable("discorduser")
    public static DiscordUserTag valueOf(String string, TagContext context) {
        if (string.startsWith("discorduser@")) {
            string = string.substring("discorduser@".length());
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
                Debug.echoError("DiscordUserTag input is not a number.");
            }
            return null;
        }
        long usrId = Long.parseLong(string);
        if (usrId == 0) {
            return null;
        }
        return new DiscordUserTag(bot, usrId);
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
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public DiscordUserTag(String bot, long userId) {
        this.bot = bot;
        this.user_id = userId;
    }

    public DiscordUserTag(String bot, User user) {
        this.bot = bot;
        this.user = user;
        user_id = user.getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public User getUser() {
        if (user != null) {
            return user;
        }
        if (bot == null) {
            return null;
        }
        DiscordConnection botObject = getBot();
        if (botObject.client == null) {
            return null;
        }
        user = botObject.client.getUserById(user_id);
        return user;
    }

    public User getUserForTag(Attribute attribute) {
        User user = getUser();
        if (user == null) {
            DiscordConnection botObject = getBot();
            if (botObject == null) {
                if (bot == null) {
                    attribute.echoError("DiscordUserTag failed to get original user: bot is missing.");
                }
                else {
                    attribute.echoError("DiscordUserTag failed to get original user: bot is not connected.");
                }
            }
            else if (botObject.client == null) {
                attribute.echoError("DiscordUserTag failed to get original user: bot is present, but is disconnected or invalid.");
            }
            else {
                attribute.echoError("DiscordUserTag failed to get original user: bot is valid, but user ID is not.");
            }
            return null;
        }
        return user;
    }

    public User user;

    public String bot;

    public long user_id;

    @Override
    public boolean isTruthy() {
        return getUser() != null;
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__users." + user_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordUserTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the base username of the user.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            return new ElementTag(object.getUser().getName());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_valid>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the user exists and is recognized, or false if it can't be seen.
        // If this returns false, some usages of the object may still be valid.
        // It may return false due to caching issues or because the user doesn't share a guild with the bot.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_valid", (attribute, object) -> {
            return new ElementTag(object.getUser() != null);
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_in_group[<group>]>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the user exists and is recognized, or false if it can't be seen.
        // If this returns false, some usages of the object may still be valid.
        // It may return false due to caching issues or because the user doesn't share a guild with the bot.
        // -->
        tagProcessor.registerTag(ElementTag.class, DiscordGroupTag.class, "is_in_group", (attribute, object, group) -> {
            if (object.getUser() == null) {
                return new ElementTag(false);
            }
            group = new DiscordGroupTag(object.bot, group.guild_id);
            Member member = group.getGuild().getMember(object.getUser());
            return new ElementTag(member != null);
        });

        // <--[tag]
        // @attribute <DiscordUserTag.discriminator>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the discriminator ID of the user.
        // -->
        tagProcessor.registerTag(ElementTag.class, "discriminator", (attribute, object) -> {
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            return new ElementTag(object.getUser().getDiscriminator());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_bot>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns a boolean indicating whether the user is a bot.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_bot", (attribute, object) -> {
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            return new ElementTag(object.getUser().isBot());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_boosting[<group>]>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns a boolean indicating whether the user is boosting the specified server or not.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_boosting", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            Member member = group.getGuild().getMember(object.getUser());
            if (member == null) {
                return null;
            }
            return new ElementTag(member.isBoosting());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.avatar_url>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the URL to the user's avatar.
        // -->
        tagProcessor.registerTag(ElementTag.class, "avatar_url", (attribute, object) -> {
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            return new ElementTag(object.getUser().getEffectiveAvatarUrl());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.nickname[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the group-specific nickname of the user (if any).
        // -->
        tagProcessor.registerTag(ElementTag.class, "nickname", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            if (group.bot == null && object.bot != null) {
                group = new DiscordGroupTag(object.bot, group.guild_id);
            }
            Member member = group.getGuild().getMember(object.getUser());
            if (member == null) {
                return null;
            }
            String nickname = member.getNickname();
            if (nickname == null) {
                return null;
            }
            return new ElementTag(nickname);
        });

        // <--[tag]
        // @attribute <DiscordUserTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the user.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.user_id);

        });

        // <--[tag]
        // @attribute <DiscordUserTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the user.
        // -->
        tagProcessor.registerTag(ElementTag.class, "mention", (attribute, object) -> {
            return new ElementTag("<@" + object.user_id + ">");
        });

        // <--[tag]
        // @attribute <DiscordUserTag.status[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the status of the user, as seen from the given group.
        // Can be any of: online, dnd, idle, invisible, offline.
        // -->
        tagProcessor.registerTag(ElementTag.class, "status", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            return new ElementTag(group.getGuild().getMember(object.getUser()).getOnlineStatus().getKey());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.activity_type[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the activity type of the user, as seen from the given group.
        // Can be any of: DEFAULT, STREAMING, LISTENING, WATCHING, CUSTOM_STATUS, COMPETING.
        // Not present for all users.
        // -->
        tagProcessor.registerTag(ElementTag.class, "activity_type", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            List<Activity> activities = group.getGuild().getMember(object.getUser()).getActivities();
            if (activities.isEmpty()) {
                return null;
            }
            return new ElementTag(activities.get(0).getType());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.activity_name[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the activity of the user, as seen from the given group.
        // Not present for all users.
        // -->
        tagProcessor.registerTag(ElementTag.class, "activity_name", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            List<Activity> activities = group.getGuild().getMember(object.getUser()).getActivities();
            if (activities.isEmpty()) {
                return null;
            }
            return new ElementTag(activities.get(0).getName());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.activity_url[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the stream URL of the activity of the user, as seen from the given group.
        // Not present for all users.
        // -->
        tagProcessor.registerTag(ElementTag.class, "activity_url", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            List<Activity> activities = group.getGuild().getMember(object.getUser()).getActivities();
            if (activities.isEmpty()) {
                return null;
            }
            if (activities.get(0).getUrl() == null) {
                return null;
            }
            return new ElementTag(activities.get(0).getUrl());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.display_name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the global display name of the user, if any.
        // To obtain a server-specific display name, use <@link tag DiscordUserTag.nickname>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "display_name", (attribute, object) -> {
            String globalName = object.getUser().getGlobalName();
            if (globalName == null) {
                return null;
            }
            return new ElementTag(globalName, true);
        });

        // <--[tag]
        // @attribute <DiscordUserTag.roles[<group>]>
        // @returns ListTag(DiscordRoleTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all roles the user has in the given group.
        // -->
        tagProcessor.registerTag(ListTag.class, DiscordGroupTag.class, "roles", (attribute, object, group) -> {
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            group = new DiscordGroupTag(object.bot, group.guild_id);
            ListTag list = new ListTag();
            Member member = group.getGuild().getMember(object.getUser());
            if (member == null) {
                return null;
            }
            for (Role role : member.getRoles()) {
                list.addObject(new DiscordRoleTag(object.bot, role));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordUserTag.permissions[<group>]>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of permissions that the user has in a certain group. You can get a list of possible outputs here: <@link url https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/Permission.html>
        // -->
        tagProcessor.registerTag(ListTag.class, "permissions", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            DiscordGroupTag group = attribute.paramAsType(DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            if (object.getUserForTag(attribute) == null) {
                return null;
            }
            ListTag list = new ListTag();
            for (Permission perm : group.getGuild().getMember(object.getUser()).getPermissions()) {
                list.addObject(new ElementTag(perm));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_banned[<group>]>
        // @returns ElementTag(boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether the user is banned from a certain group.
        // -->
        tagProcessor.registerTag(ElementTag.class, DiscordGroupTag.class, "is_banned", (attribute, object, group) -> {
            UserSnowflake user = UserSnowflake.fromId(object.user_id);
            try {
                group.getGuild().retrieveBan(user).complete();
            }
            catch (ErrorResponseException ex) {
                if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_BAN) {
                    return new ElementTag(false);
                }
                attribute.echoError(ex);
                return null;
            }
            return new ElementTag(true);
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_timed_out[<group>]>
        // @returns ElementTag(boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether the user is timed out in a certain group.
        // -->
        tagProcessor.registerTag(ElementTag.class, DiscordGroupTag.class, "is_timed_out", (attribute, object, group) -> {
            Guild guild = group.getGuild();
            Member member = guild.getMemberById(object.user_id);
            if (member == null) {
                attribute.echoError("Invalid user! Are they in the Discord Group?");
                return null;
            }
            return new ElementTag(member.isTimedOut());
        });

        // <--[mechanism]
        // @object DiscordUserTag
        // @name move
        // @input DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // If this user is connected to a voice channel, moves them to the specified voice channel.
        // -->
        tagProcessor.registerMechanism("move", false, DiscordChannelTag.class, (object, mechanism, channel) -> {
            GuildChannel guildChannel;
            if (channel.getChannel() instanceof GuildChannel) {
                guildChannel = (GuildChannel) channel.getChannel();
            }
            else {
                mechanism.echoError("Invalid channel!");
                return;
            }
            if (guildChannel.getType() != ChannelType.VOICE) {
                mechanism.echoError("Input must be a voice channel!");
                return;
            }
            Member member = guildChannel.getGuild().getMember(object.getUser());
            if (member == null) {
                mechanism.echoError("Invalid group member!");
                return;
            }
            if (member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
                mechanism.echoError("User isn't in a voice channel!");
                return;
            }
            guildChannel.getGuild().moveVoiceMember(member, (AudioChannel) channel.getChannel()).complete();
        });
    }

    public static ObjectTagProcessor<DiscordUserTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discorduser";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        if (user != null) {
            return identify() + " <GR>(" + user.getName() + "#" + user.getDiscriminator() + ")";
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply a property to a DiscordUserTag!");
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }
}
