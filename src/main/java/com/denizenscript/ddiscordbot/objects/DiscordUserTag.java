package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class DiscordUserTag implements ObjectTag {

    // <--[language]
    // @name DiscordUserTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordUserTag is an object that represents a user (human or bot) on Discord, either as a generic reference,
    // or as a bot-specific reference.
    //
    // These use the object notation "discorduser@".
    // The identity format for Discord users is the bot ID (optional), followed by the user ID (required).
    // For example: 1234
    // Or: mybot,1234
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
        user = getBot().client.getUserById(user_id);
        return user;
    }

    public User user;

    public String bot;

    public long user_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordUserTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the user name of the user.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.getUser().getName());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.is_bot>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns a boolean indicating whether the user is a bot.
        // -->
        registerTag("is_bot", (attribute, object) -> {
            return new ElementTag(object.getUser().isBot());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.avatar_url>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the URL to the user's avatar.
        // -->
        registerTag("avatar_url", (attribute, object) -> {
            return new ElementTag(object.getUser().getEffectiveAvatarUrl());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.nickname[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the group-specific nickname of the user (if any).
        // -->
        registerTag("nickname", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            String nickname = group.guild.getMember(object.getUser()).getNickname();
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
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.user_id);

        });

        // <--[tag]
        // @attribute <DiscordUserTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the user.
        // -->
        registerTag("mention", (attribute, object) -> {
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
        registerTag("status", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            return new ElementTag(group.guild.getMember(object.getUser()).getOnlineStatus().getKey());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.activity_type[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the activity type of the user, as seen from the given group.
        // Can be any of: PLAYING, LISTENING, STREAMING, WATCHING.
        // Not present for all users.
        // -->
        registerTag("activity_type", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            List<Activity> activities = group.guild.getMember(object.getUser()).getActivities();
            if (activities.isEmpty()) {
                return null;
            }
            return new ElementTag(activities.get(0).getType().name());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.activity_name[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the activity of the user, as seen from the given group.
        // Can be any of: PLAYING, LISTENING, STREAMING, WATCHING.
        // Not present for all users.
        // -->
        registerTag("activity_name", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            List<Activity> activities = group.guild.getMember(object.getUser()).getActivities();
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
        // Can be any of: PLAYING, LISTENING, STREAMING, WATCHING.
        // Not present for all users.
        // -->
        registerTag("activity_url", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            List<Activity> activities = group.guild.getMember(object.getUser()).getActivities();
            if (activities.isEmpty()) {
                return null;
            }
            if (activities.get(0).getUrl() == null) {
                return null;
            }
            return new ElementTag(activities.get(0).getUrl());
        });

        // <--[tag]
        // @attribute <DiscordUserTag.roles[<group>]>
        // @returns ListTag(DiscordRoleTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all roles the user has in the given group.
        // -->
        registerTag("roles", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                return null;
            }
            DiscordGroupTag group = attribute.contextAsType(1, DiscordGroupTag.class);
            if (group == null) {
                return null;
            }
            ListTag list = new ListTag();
            for (Role role : group.guild.getMember(object.getUser()).getRoles()) {
                list.addObject(new DiscordRoleTag(object.bot, role));
            }
            return list;
        });
    }

    public static ObjectTagProcessor<DiscordUserTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordUserTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }
}
