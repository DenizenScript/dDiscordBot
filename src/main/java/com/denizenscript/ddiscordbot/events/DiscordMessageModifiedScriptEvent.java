package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

public class DiscordMessageModifiedScriptEvent extends DiscordScriptEvent {
    public static DiscordMessageModifiedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message modified
    //
    // @Regex ^on discord message modified$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user modified a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.author> returns the user that authored the message.
    // <context.mentions> returns a list of all mentioned users.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    // <context.message> returns the message (raw).
    // <context.message_id> returns the message ID.
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message (mentions/etc. are written cleanly). CURRENTLY NON-FUNCTIONAL.
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.old_message> returns the previous message (raw).
    // <context.old_no_mention_message> returns the previous message with all user mentions stripped.
    // <context.old_formatted_message> returns the formatted previous message (mentions/etc. are written cleanly). CURRENTLY NON-FUNCTIONAL.
    //
    // -->

    public MessageUpdateEvent getEvent() {
        return (MessageUpdateEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord message modified");
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
        // TODO: Message cache
        else if (name.equals("old_message_valid")) {
            return new ElementTag(false);
        }
        else if (name.equals("old_message")) {
            return null;
        }
        else if (name.equals("old_no_mention_message")) {
            return null;
        }
        else if (name.equals("old_formatted_message")) {
            return null;
        }
        else if (name.equals("message")) {
            return new ElementTag(getEvent().getMessage().getContentRaw());
        }
        else if (name.equals("message_id")) {
            return new ElementTag(getEvent().getMessage().getId());
        }
        else if (name.equals("no_mention_message")) {
            return new ElementTag(stripMentions(getEvent().getMessage().getContentRaw(), getEvent().getMessage().getMentionedUsers()));
        }
        else if (name.equals("formatted_message")) {
            return new ElementTag(getEvent().getMessage().getContentDisplay());
        }
        else if (name.equals("author")) {
            return new DiscordUserTag(botID, getEvent().getMessage().getAuthor());
        }
        else if (name.equals("mentions")) {
            ListTag list = new ListTag();
            for (User user : getEvent().getMessage().getMentionedUsers()) {
                list.addObject(new DiscordUserTag(botID, user));
            }
            return list;
        }
        else if (name.equals("is_direct")) {
            return new ElementTag(getEvent().getChannel() instanceof PrivateChannel);
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordModifiedMessage";
    }
}
