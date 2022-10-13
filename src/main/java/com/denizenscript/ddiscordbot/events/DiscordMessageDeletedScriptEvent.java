package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordConnection;
import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

public class DiscordMessageDeletedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord message deleted
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user deletes a message.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.channel> returns the DiscordChannelTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.old_message> returns the original DiscordMessageTag (data may be missing if not cached).
    //
    // -->

    public static DiscordMessageDeletedScriptEvent instance;

    public Message oldMessage;

    public DiscordMessageDeletedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord message deleted");
        registerSwitches("channel", "group");
    }

    public MessageDeleteEvent getEvent() {
        return (MessageDeleteEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().isFromGuild() ? getEvent().getGuild() : null)) {
            return false;
        }
        return super.matches(path);
    }

    public Message getOldMessage() {
        if (oldMessage != null) {
            return oldMessage;
        }
        DiscordConnection connection = getConnection();
        if (connection == null) {
            return null;
        }
        oldMessage = connection.cache.getMessage(getEvent().getChannel().getIdLong(), getEvent().getMessageIdLong());
        return oldMessage;
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "channel":
                return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group":
                if (getEvent().isFromGuild()) {
                    return new DiscordGroupTag(botID, getEvent().getGuild());
                }
                break;
            case "old_message":
                Message oldMessage = getOldMessage();
                if (oldMessage != null) {
                    return new DiscordMessageTag(botID, oldMessage);
                }
            case "old_message_valid":
                return new ElementTag(getOldMessage() != null);
            case "message":
            case "formatted_message":
            case "no_mention_message":
            case "author":
            case "mentions":
            case "author_id":
            case "author_name":
            case "mention_names":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return null;
            case "message_id":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getMessageId());
            case "is_direct":
                DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
                return new ElementTag(getEvent().getChannel() instanceof PrivateChannel);
        }
        return super.getContext(name);
    }
}
