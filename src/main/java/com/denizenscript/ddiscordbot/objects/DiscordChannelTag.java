package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;

public class DiscordChannelTag implements ObjectTag {

    // <--[language]
    // @name DiscordChannelTag Objects
    // @group Object System
    // @plugin dDiscordBot
    // @description
    // A DiscordChannelTag is an object that represents a channel (text or voice) on Discord, either as a generic reference,
    // or as a bot-specific reference (the relevant guild is inherently linked, and does not need to be specified).
    //
    // These use the object notation "discordchannel@".
    // The identity format for Discord channels is the bot ID (optional), followed by the channel ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
    // -->

    @Fetchable("discordchannel")
    public static DiscordChannelTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordchannel@")) {
            string = string.substring("discordchannel@".length());
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
                Debug.echoError("DiscordChannelTag input is not a number.");
            }
            return null;
        }
        long chanID = Long.parseLong(string);
        if (chanID == 0) {
            return null;
        }
        return new DiscordChannelTag(bot, chanID);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordchannel@")) {
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

    public DiscordChannelTag(String bot, long channelId) {
        this.bot = bot;
        this.channel_id = channelId;
    }

    public DiscordChannelTag(String bot, MessageChannel channel) {
        this.bot = bot;
        this.channel = channel;
        channel_id = channel.getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public MessageChannel getChannel() {
        if (channel != null) {
            return channel;
        }
        channel = getBot().client.getTextChannelById(channel_id);
        return channel;
    }

    public MessageChannel channel;

    public String bot;

    public long channel_id;

    public static void registerTags() {

        // <--[tag]
        // @attribute <DiscordChannelTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the channel.
        // -->
        registerTag("name", (attribute, object) -> {
            MessageChannel chan = object.getChannel();
            String name;
            if (chan instanceof GuildChannel) {
                name = ((GuildChannel) chan).getName();
            }
            else if (chan instanceof PrivateChannel) {
                name = "private";
            }
            else {
                name = "unknown";
            }
            return new ElementTag(name);

        });

        // <--[tag]
        // @attribute <DiscordChannelTag.channel_type>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the type of the channel.
        // Will be any of: TEXT, PRIVATE, VOICE, GROUP, CATEGORY, STORE, UNKNOWN.
        // -->
        registerTag("channel_type", (attribute, object) -> {
            return new ElementTag(object.getChannel().getType().name());

        }, "type");

        // <--[tag]
        // @attribute <DiscordChannelTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the channel.
        // -->
        registerTag("id", (attribute, object) -> {
            return new ElementTag(object.channel_id);

        });

        // <--[tag]
        // @attribute <DiscordChannelTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the channel.
        // -->
        registerTag("mention", (attribute, object) -> {
            return new ElementTag("<#" + object.channel_id + ">");

        });

        // <--[tag]
        // @attribute <DiscordChannelTag.group>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this channel.
        // -->
        registerTag("group", (attribute, object) -> {
            MessageChannel chan = object.getChannel();
            Guild guild;
            if (chan instanceof GuildChannel) {
                guild = ((GuildChannel) chan).getGuild();
            }
            else {
                return null;
            }
            return new DiscordGroupTag(object.bot, guild);

        });
    }

    public static ObjectTagProcessor<DiscordChannelTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<DiscordChannelTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordchannel";

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
        return "DiscordChannel";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordchannel@" + bot + "," + channel_id;
        }
        return "discordchannel@" + channel_id;
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
