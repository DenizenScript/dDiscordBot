package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class DiscordMessageDeletedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageDeletedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message deleted
    //
    // @Regex ^on discord message deleted$
    //
    // @Switch for <bot>
    // @Switch channel <channel_id>
    // @Switch group <group_id>
    //
    // @Triggers when a Discord user deletes a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.self> returns the bots own Discord user ID.
    // <context.channel> returns the channel ID.
    // <context.channel_name> returns the channel name.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.author> returns the user that authored the message.
    // <context.mentions> returns a list of all mentioned user IDs.
    // <context.mention_names> returns a list of all mentioned user names.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    // <context.old_message_valid> returns whether the old message is available (it may be lost due to caching).
    // <context.message> returns the original message (raw).
    // <context.no_mention_message> returns the original message with all user mentions stripped.
    // <context.formatted_message> returns the formatted original message (mentions/etc. are written cleanly). CURRENTLY NON-FUNCTIONAL.
    //
    // -->

    public MessageDeleteEvent getEvent() {
        return (MessageDeleteEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message deleted");
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
            return new ElementTag(getEvent().getChannelId().asLong());
        }
        else if (name.equals("channel_name")) {
            MessageChannel channel = getEvent().getChannel().block();
            if (channel instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) channel).getName());
            }
        }
        else if (name.equals("group")) {
            if (getEvent().getChannel().block() instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) getEvent().getChannel().block()).getGuildId().asLong());
            }
        }
        else if (name.equals("group_name")) {
            if (getEvent().getChannel().block() instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) getEvent().getChannel().block()).getGuild().block().getName());
            }
        }
        else if (name.equals("old_message_valid")) {
            return new ElementTag(getEvent().getMessage().isPresent());
        }
        else if (name.equals("message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getContent().get());
            }
        }
        else if (name.equals("no_mention_message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(stripMentions(getEvent().getMessage().get().getContent().get(),
                        getEvent().getMessage().get().getUserMentions()));
            }
        }
        else if (name.equals("formatted_message")) {
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getContent().get());
            }
        }
        else if (name.equals("author")) {
            return new DiscordUserTag(botID, getEvent().getMessage().get().getAuthor().get());
        }
        else if (name.equals("author_id")) { // Deprecated
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getAuthor().get().getId().asLong());
            }
        }
        else if (name.equals("author_name")) { // Deprecated
            if (getEvent().getMessage().isPresent()) {
                return new ElementTag(getEvent().getMessage().get().getAuthor().get().getUsername());
            }
        }
        else if (name.equals("mentions")) {
            if (getEvent().getMessage().isPresent()) {
                ListTag list = new ListTag();
                for (Snowflake user : getEvent().getMessage().get().getUserMentionIds()) {
                    list.add(String.valueOf(user.asLong()));
                }
                return list;
            }
        }
        else if (name.equals("mention_names")) {
            if (getEvent().getMessage().isPresent()) {
                ListTag list = new ListTag();
                for (User user : getEvent().getMessage().get().getUserMentions().toIterable()) {
                    list.add(String.valueOf(user.getUsername()));
                }
                return list;
            }
        }
        else if (name.equals("is_direct")) {
            return new ElementTag(!(getEvent().getChannel().block() instanceof GuildChannel));
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageDeleted";
    }
}
