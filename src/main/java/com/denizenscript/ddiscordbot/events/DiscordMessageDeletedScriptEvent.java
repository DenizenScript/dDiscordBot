package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordMessageTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagContext;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

public class DiscordMessageDeletedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageDeletedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message deleted
    //
    // @Regex ^on discord message deleted$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user deletes a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.old_message> returns the original DiscordMessageText (data may be missing if not cached).
    //
    // -->

    public MessageDeleteEvent getEvent() {
        return (MessageDeleteEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord message deleted");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("channel", getEvent().getChannel().getId())) {
            return false;
        }
        if (getEvent().isFromGuild() && !path.checkSwitch("group", getEvent().getGuild().getId())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("channel")) {
            return new DiscordChannelTag(botID, getEvent().getChannel());
        }
        else if (name.equals("group")) {
            if (getEvent().isFromGuild()) {
                return new DiscordGroupTag(botID, getEvent().getGuild());
            }
        }
        else if (name.equals("old_message")) {
            return new DiscordMessageTag(botID, getEvent().getChannel().getIdLong(), getEvent().getMessageIdLong());
        }
        // TODO: Message cache?
        else if (name.equals("old_message_valid")) {
            return new ElementTag(false);
        }
        else if (name.equals("message")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("message_id")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return new ElementTag(getEvent().getMessageId());
        }
        else if (name.equals("no_mention_message")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("formatted_message")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("author")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("mentions")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("is_direct")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return new ElementTag(getEvent().getChannel() instanceof PrivateChannel);
        }
        else if (name.equals("author_id")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("author_name")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        else if (name.equals("mention_names")) {
            DenizenDiscordBot.oldMessageContexts.warn((TagContext) null);
            return null;
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageDeleted";
    }
}
