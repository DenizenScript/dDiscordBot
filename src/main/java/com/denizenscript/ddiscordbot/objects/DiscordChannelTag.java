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

public class DiscordChannelTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name DiscordChannelTag
    // @prefix discordchannel
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord channels is the bot ID (optional), followed by the channel ID (required).
    // For example: 1234
    // Or: mybot,1234
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordChannelTag is an object that represents a channel (text or voice) on Discord, either as a generic reference,
    // or as a bot-specific reference (the relevant guild is inherently linked, and does not need to be specified).
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__channels"
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
            if (context == null || context.showErrors()) {
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

    public Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        if (bot == null) {
            return null;
        }
        channel = getBot().getChannel(channel_id);
        return channel;
    }

    public Channel channel;

    public String bot;

    public long channel_id;

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__channels." + channel_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordChannelTag.name>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the name of the channel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.getChannel().getName());
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.channel_type>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the type of the channel.
        // Will be any of: TEXT, PRIVATE, VOICE, GROUP, CATEGORY, NEWS, STORE, STAGE, GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD, or UNKNOWN.
        // -->
        tagProcessor.registerTag(ElementTag.class, "channel_type", (attribute, object) -> {
            return new ElementTag(object.getChannel().getType().name());
        }, "type");

        // <--[tag]
        // @attribute <DiscordChannelTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID number of the channel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.channel_id);
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.parent>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the parent channel of this thread channel (if this channel is a thread).
        // -->
        tagProcessor.registerTag(DiscordChannelTag.class, "parent", (attribute, object) -> {
            Channel channel = object.getChannel();
            if (!(channel instanceof ThreadChannel)) {
                attribute.echoError("Cannot get 'parent' tag: this channel is not a thread.");
                return null;
            }
            GuildMessageChannel parent = ((ThreadChannel) channel).getParentMessageChannel();
            return new DiscordChannelTag(object.bot, parent);
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.mention>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the raw mention string for the channel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "mention", (attribute, object) -> {
            return new ElementTag("<#" + object.channel_id + ">");
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.group>
        // @returns DiscordGroupTag
        // @plugin dDiscordBot
        // @description
        // Returns the group that owns this channel.
        // -->
        tagProcessor.registerTag(DiscordGroupTag.class, "group", (attribute, object) -> {
            Channel chan = object.getChannel();
            Guild guild;
            if (chan instanceof GuildChannel) {
                guild = ((GuildChannel) chan).getGuild();
            }
            else {
                return null;
            }
            return new DiscordGroupTag(object.bot, guild);
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.pinned_messages>
        // @returns ListTag(DiscordMessageTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of the messages that are pinned in the channel.
        // -->
        tagProcessor.registerTag(ListTag.class, "pinned_messages", (attribute, object) -> {
            ListTag list = new ListTag();
            MessageChannel channel = (MessageChannel) object.getChannel();
            if (channel == null) {
                return null;
            }
            for (Message message : channel.retrievePinnedMessages().complete()) {
                list.addObject(new DiscordMessageTag(object.bot, message));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.first_message>
        // @returns DiscordMessageTag
        // @plugin dDiscordBot
        // @description
        // Returns the first message sent in the channel.
        // -->
        tagProcessor.registerTag(DiscordMessageTag.class, "first_message", (attribute, object) -> {
            MessageChannel channel = (MessageChannel) object.getChannel();
            if (channel == null) {
                return null;
            }
            Message first = channel.getHistoryFromBeginning(1).complete().getRetrievedHistory().get(0);
            return new DiscordMessageTag(object.bot, first);
        });

        // <--[tag]
        // @attribute <DiscordChannelTag.last_message>
        // @returns DiscordMessageTag
        // @plugin dDiscordBot
        // @description
        // Returns the last message sent in the channel.
        // -->
        tagProcessor.registerTag(DiscordMessageTag.class, "last_message", (attribute, object) -> {
            MessageChannel channel = (MessageChannel) object.getChannel();
            if (channel == null) {
                return null;
            }
            if (channel.hasLatestMessage()) {
                return new DiscordMessageTag(object.bot, object.channel_id, channel.getLatestMessageIdLong());
            }
            else {
                return null;
            }
        });
    }

    public static ObjectTagProcessor<DiscordChannelTag> tagProcessor = new ObjectTagProcessor<>();

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
    public String debuggable() {
        if (channel != null) {
            return identify() + " <GR>(" + channel.getName() + ")";
        }
        return identify();
    }

    @Override
    public boolean isUnique() {
        return true;
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
