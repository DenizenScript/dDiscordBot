package com.denizenscript.ddiscordbot.objects;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class DiscordMessageTag implements ObjectTag, FlaggableObject, Adjustable {

    // <--[ObjectType]
    // @name DiscordMessageTag
    // @prefix discordmessage
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for Discord messages is the bot ID (optional), followed by the channel ID (optional), followed by the message ID (required).
    // For example: 1234
    // Or: 12,1234
    // Or: mybot,12,1234
    //
    // @plugin dDiscordBot
    // @description
    // A DiscordMessageTag is an object that represents a message already sent on Discord, either as a generic reference,
    // or as a bot-specific reference.
    // Note that this is not used for messages that *are going to be* sent.
    // Note that this often does not contain data for messages that have been deleted (unless that data is cached).
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in: plugins/dDiscordBot/flags/bot_(botname).dat, under special sub-key "__messages"
    //
    // -->

    @Fetchable("discordmessage")
    public static DiscordMessageTag valueOf(String string, TagContext context) {
        if (string.startsWith("discordmessage@")) {
            string = string.substring("discordmessage@".length());
        }
        if (string.contains("@")) {
            return null;
        }
        List<String> commaSplit = CoreUtilities.split(string, ',');
        if (commaSplit.size() == 0 || commaSplit.size() > 3) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag input is not valid.");
            }
            return null;
        }
        String msgIdText = commaSplit.get(commaSplit.size() - 1);
        if (!ArgumentHelper.matchesInteger(msgIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag input is not a number.");
            }
            return null;
        }
        long msgId = Long.parseLong(msgIdText);
        if (msgId == 0) {
            return null;
        }
        if (commaSplit.size() == 1) {
            return new DiscordMessageTag(null, 0, msgId);
        }
        String chanIdText = commaSplit.get(commaSplit.size() - 2);
        if (!ArgumentHelper.matchesInteger(chanIdText)) {
            if (context == null || context.showErrors()) {
                Debug.echoError("DiscordMessageTag channel ID input is not a number.");
            }
            return null;
        }
        long chanId = Long.parseLong(chanIdText);
        if (chanId == 0) {
            return null;
        }
        return new DiscordMessageTag(commaSplit.size() == 3 ? commaSplit.get(0) : null, chanId, msgId);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("discordmessage@")) {
            return true;
        }
        if (arg.contains("@")) {
            return false;
        }
        int comma = arg.lastIndexOf(',');
        if (comma == -1) {
            return ArgumentHelper.matchesInteger(arg);
        }
        if (comma == arg.length() - 1) {
            return false;
        }
        return ArgumentHelper.matchesInteger(arg.substring(comma + 1));
    }

    public DiscordMessageTag(String bot, long channel_id, long message_id) {
        this.bot = bot;
        this.channel_id = channel_id;
        this.message_id = message_id;
    }

    public DiscordMessageTag(String bot, Message message) {
        this.bot = bot;
        this.message_id = message.getIdLong();
        this.message = message;
        this.channel = message.getChannel();
        this.channel_id = this.channel.getIdLong();
    }

    public DiscordConnection getBot() {
        return DenizenDiscordBot.instance.connections.get(bot);
    }

    public MessageChannel getChannel() {
        if (channel != null) {
            return channel;
        }
        if (bot == null) {
            return null;
        }
        Channel result = getBot().getChannel(channel_id);
        if (result instanceof MessageChannel) {
            channel = (MessageChannel) result;
        }
        return channel;
    }

    public Message getMessage() {
        if (message != null) {
            return message;
        }
        message = getBot().getMessage(channel_id, message_id);
        return message;
    }

    public String bot;

    public MessageChannel channel;

    public Message message;

    public long channel_id;

    public long message_id;

    @Override
    public DiscordMessageTag duplicate() {
        return new DiscordMessageTag(bot, channel_id, message_id);
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(getBot().flags, "__messages." + channel_id + "." + message_id);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static AsciiMatcher digits = new AsciiMatcher(AsciiMatcher.DIGITS);

    public static String stripMentions(String message) {
        message = message.replace("@everyone", "").replace("@here", "");
        StringBuilder output = new StringBuilder(message.length());
        char[] rawChars = message.toCharArray();
        for (int i = 0; i < rawChars.length; i++) {
            char c = rawChars[i];
            if (c == '<' && (i + 3) < rawChars.length && rawChars[i + 1] == '@') {
                char next = rawChars[i + 2];
                if (digits.isMatch(next) || next == '!' || next == '&') {
                    int end = message.indexOf('>', i);
                    if (end > i + 3 && end < i + 32) {
                        if (digits.isOnlyMatches(message.substring(i + 3, end))) {
                            i = end;
                            continue;
                        }
                    }
                }
            }
            output.append(c);
        }
        return output.toString();
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <DiscordMessageTag.id>
        // @returns ElementTag(Number)
        // @plugin dDiscordBot
        // @description
        // Returns the ID of the message.
        // -->
        tagProcessor.registerTag(ElementTag.class, "id", (attribute, object) -> {
            return new ElementTag(object.message_id);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.url>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the full jump URL to this message.
        // -->
        tagProcessor.registerTag(ElementTag.class, "url", (attribute, object) -> {
            return new ElementTag(object.getMessage().getJumpUrl());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.channel>
        // @returns DiscordChannelTag
        // @plugin dDiscordBot
        // @description
        // Returns the channel that the message was sent to.
        // -->
        tagProcessor.registerTag(DiscordChannelTag.class, "channel", (attribute, object) -> {
            return new DiscordChannelTag(object.bot, object.channel_id);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the full text of the message.
        // -->
        tagProcessor.registerTag(ElementTag.class, "text", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentRaw());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_stripped>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the stripped text of the message (format codes like bold removed).
        // -->
        tagProcessor.registerTag(ElementTag.class, "text_stripped", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentStripped());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_display>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the display text of the message (special codes like pings formatted to how they should look for users).
        // -->
        tagProcessor.registerTag(ElementTag.class, "text_display", (attribute, object) -> {
            return new ElementTag(object.getMessage().getContentDisplay());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.text_no_mentions>
        // @returns ElementTag
        // @plugin dDiscordBot
        // @description
        // Returns the text of the message, with '@' mentions removed.
        // -->
        tagProcessor.registerTag(ElementTag.class, "text_no_mentions", (attribute, object) -> {
            return new ElementTag(stripMentions(object.getMessage().getContentRaw()));
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.author>
        // @returns DiscordUserTag
        // @plugin dDiscordBot
        // @description
        // Returns the author of the message.
        // -->
        tagProcessor.registerTag(DiscordUserTag.class, "author", (attribute, object) -> {
            return new DiscordUserTag(object.bot, object.getMessage().getAuthor());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.was_edited>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether this message was edited.
        // -->
        tagProcessor.registerTag(ElementTag.class, "was_edited", (attribute, object) -> {
            return new ElementTag(object.getMessage().isEdited());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.is_pinned>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns whether this message is pinned.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_pinned", (attribute, object) -> {
            return new ElementTag(object.getMessage().isPinned());
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.mentioned_users>
        // @returns ListTag(DiscordUserTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of users mentioned (pinged) by this message.
        // -->
        tagProcessor.registerTag(ListTag.class, "mentioned_users", (attribute, object) -> {
            ListTag list = new ListTag();
            for (User user : object.getMessage().getMentions().getUsers()) {
                list.addObject(new DiscordUserTag(object.bot, user));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.is_direct>
        // @returns ElementTag(Boolean)
        // @plugin dDiscordBot
        // @description
        // Returns true if the message was sent in a direct (private) channel, or false if in a public channel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_direct", (attribute, object) -> {
            return new ElementTag(object.getChannel() instanceof PrivateChannel);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.embed>
        // @returns ListTag(DiscordEmbedTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of "embed" formatted data on this message.
        // -->
        tagProcessor.registerTag(ListTag.class, "embed", (attribute, object) -> {
            ListTag list = new ListTag();
            for (MessageEmbed embed : object.getMessage().getEmbeds()) {
                list.addObject(new DiscordEmbedTag(embed));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.reactions>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of reaction on this message.
        // -->
        tagProcessor.registerTag(ListTag.class, "reactions", (attribute, object) -> {
            ListTag list = new ListTag();
            for (MessageReaction reaction : object.getMessage().getReactions()) {
                list.addObject(new DiscordReactionTag(object.bot, object.getMessage(), reaction));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.previous_messages[<#>]>
        // @returns ListTag(DiscordMessageTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of the last (specified number) messages sent in the channel prior to this message.
        // The list is ordered from most recent to least recent.
        // -->
        tagProcessor.registerTag(ListTag.class, "previous_messages", (attribute, object) -> {
            int limit = attribute.getIntParam();
            MessageHistory history = object.getChannel().getHistoryBefore(object.message_id, limit).complete();
            ListTag list = new ListTag();
            for (Message message : history.getRetrievedHistory()) {
                list.addObject(new DiscordMessageTag(object.bot, message));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.next_messages[<#>]>
        // @returns ListTag(DiscordMessageTag)
        // @plugin dDiscordBot
        // @description
        // Returns a list of the next (specified number) messages sent in the channel after this message.
        // The list is ordered from most recent to least recent.
        // -->
        tagProcessor.registerTag(ListTag.class, "next_messages", (attribute, object) -> {
            int limit = attribute.getIntParam();
            MessageHistory history = object.getChannel().getHistoryAfter(object.message_id, limit).complete();
            ListTag list = new ListTag();
            for (Message message : history.getRetrievedHistory()) {
                list.addObject(new DiscordMessageTag(object.bot, message));
            }
            return list;
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.replied_to>
        // @returns DiscordMessageTag
        // @plugin dDiscordBot
        // @description
        // Returns the message that this message was in reply to (if any).
        // -->
        tagProcessor.registerTag(DiscordMessageTag.class, "replied_to", (attribute, object) -> {
            Message message = object.getMessage().getReferencedMessage();
            if (message == null) {
                return null;
            }
            return new DiscordMessageTag(object.bot, message);
        });

        // <--[tag]
        // @attribute <DiscordMessageTag.attachments>
        // @returns ListTag
        // @plugin dDiscordBot
        // @description
        // Returns a list of attachment URLs for this message. Most messages will return an empty list, or a single-entry list,
        // however it is possible in some cases for a single message to have multiple attachments.
        // -->
        tagProcessor.registerTag(ListTag.class, "attachments", (attribute, object) -> {
            ListTag result = new ListTag();
            for (Message.Attachment attachment : object.getMessage().getAttachments()) {
                result.addObject(new ElementTag(attachment.getUrl()));
            }
            return result;
        });
    }

    public static ObjectTagProcessor<DiscordMessageTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    String prefix = "discordmessage";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "DiscordMessage";
    }

    @Override
    public String identify() {
        if (bot != null) {
            return "discordmessage@" + bot + "," + channel_id + "," + message_id;
        }
        if (channel_id != 0) {
            return "discordmessage@" + channel_id + "," + message_id;
        }
        return "discordmessage@" + message_id;
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
        mechanism.echoError("Cannot apply properties to a DiscordMessageTag!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object DiscordMessageTag
        // @name delete
        // @input None
        // @description
        // Deletes the message.
        // -->
        if (mechanism.matches("delete")) {
            Message message = getMessage();
            try {
                message.delete().submit();
            }
            catch (Throwable ex) {
                mechanism.echoError("Failed to delete message: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
            }
        }
    }
}
