package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.Optional;

public class DiscordUserTag implements ObjectTag {

    // <--[language]
    // @name DiscordUserTag
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordUserTag is an object that represents a user (human or bot) on Discord, either as a generic reference,
    // or as a bot-specific reference.
    //
    // For format info, see <@link language discorduser@>
    //
    // -->

    // <--[language]
    // @name discorduser@
    // @group Object Fetcher System
    // @plugin dDiscordBot
    // @description
    // discorduser@ refers to the 'object identifier' of a DiscordUserTag. The 'discorduser@' is notation for Denizen's Object
    // Fetcher. The constructor for a DiscordUserTag is the bot ID (optional), followed by the user ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
    // For general info, see <@link language DiscordUserTag>
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
        long usrId = ArgumentHelper.getLongFrom(string);
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
        if (bot != null) {
            DiscordConnection conn = DenizenDiscordBot.instance.connections.get(bot);
            if (conn != null) {
                user = conn.client.getUserById(Snowflake.of(user_id)).block();
            }
        }
    }

    public DiscordUserTag(String bot, User user) {
        this.bot = bot;
        this.user = user;
        user_id = user.getId().asLong();
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
        registerTag("name", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordUserTag) object).user.getUsername())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordUserTag.nickname[<group>]>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the group-specific nickname of the user (if any).
        // -->
        registerTag("nickname", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                DiscordGroupTag group = DiscordGroupTag.valueOf(attribute.getContext(1), attribute.context);
                if (group == null) {
                    return null;
                }
                Optional<String> nickname = ((DiscordUserTag) object).user.asMember(Snowflake.of(group.guild_id)).block().getNickname();
                if (!nickname.isPresent()) {
                    return null;
                }
                return new ElementTag(nickname.get())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordUserTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the user.
        // -->
        registerTag("id", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordUserTag) object).user_id)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordUserTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the user.
        // -->
        registerTag("mention", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((DiscordUserTag) object).user.getMention())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <DiscordUserTag.roles[<group>]>
        // @returns ListTag(DiscordRoleTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of all roles the user has in the given group.
        // -->
        registerTag("roles", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                DiscordGroupTag group = DiscordGroupTag.valueOf(attribute.getContext(1), attribute.context);
                if (group == null) {
                    return null;
                }
                ListTag list = new ListTag();
                for (Role role : ((DiscordUserTag) object).user.asMember(Snowflake.of(group.guild_id)).block().getRoles().toIterable()) {
                    list.addObject(new DiscordRoleTag(((DiscordUserTag) object).bot, role));
                }
                return list.getObjectAttribute(attribute.fulfill(1));
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
