package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.object.entity.User;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Snowflake;

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
    // <context.author> returns the user that authored the message.
    // <context.mentions> returns a list of all mentioned users.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.message> returns the original message (raw).
    // <context.message_id> returns the message ID.
    // <context.no_mention_message> returns the original message with all user mentions stripped.
    // <context.formatted_message> returns the formatted original message (mentions/etc. are written cleanly). CURRENTLY NON-FUNCTIONAL.
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
        if (!path.checkSwitch("channel", String.valueOf(getEvent().getChannelId().asLong()))) {
            return false;
        }
        if (getEvent().getChannel().block() instanceof GuildChannel
                && !path.checkSwitch("group", String.valueOf(((GuildChannel) getEvent().getChannel().block()).getGuildId().asLong()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("channel")) {
            return new DiscordChannelTag(botID, getEvent().getChannelId().asLong());
        }
        else if (name.equals("group")) {
            if (getEvent().getChannel().block() instanceof GuildChannel) {
                return new DiscordGroupTag(botID, ((GuildChannel) getEvent().getChannel().block()).getGuildId().asLong());
            }
        }
        else if (name.equals("old_message_valid")) {
            return new ElementTag(getEvent().getMessage().isPresent());
        }
        else if (name.equals("message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getContent());
            }
        }
        else if (name.equals("message_id")) {
            return new ElementTag(getEvent().getMessageId().asString());
        }
        else if (name.equals("no_mention_message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(stripMentions(getEvent().getMessage().get().getContent(),
                        getEvent().getMessage().get().getUserMentions()));
            }
        }
        else if (name.equals("formatted_message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getContent());
            }
        }
        else if (name.equals("author")) {
            return new DiscordUserTag(botID, getEvent().getMessage().get().getAuthor().get());
        }
        else if (name.equals("mentions")) {
            if (getEvent().getMessage().isPresent()) {
                ListTag list = new ListTag();
                for (Snowflake user : getEvent().getMessage().get().getUserMentionIds()) {
                    list.addObject(new DiscordUserTag(botID, user.asLong()));
                }
                return list;
            }
        }
        else if (name.equals("is_direct")) {
            return new ElementTag(!(getEvent().getChannel().block() instanceof GuildChannel));
        }
        else if (name.equals("channel_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            MessageChannel channel = getEvent().getChannel().block();
            if (channel instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) channel).getName());
            }
        }
        else if (name.equals("group_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            if (getEvent().getChannel().block() instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) getEvent().getChannel().block()).getGuild().block().getName());
            }
        }
        else if (name.equals("author_id")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getAuthor().get().getId().asLong());
            }
        }
        else if (name.equals("author_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getAuthor().get().getUsername());
            }
        }
        else if (name.equals("mention_names")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            if (getEvent().getMessage().isPresent()) {
                ListTag list = new ListTag();
                for (User user : getEvent().getMessage().get().getUserMentions().toIterable()) {
                    list.add(String.valueOf(user.getUsername()));
                }
                return list;
            }
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageDeleted";
    }
}
