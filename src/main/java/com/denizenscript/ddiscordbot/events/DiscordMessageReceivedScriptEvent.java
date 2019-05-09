package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class DiscordMessageReceivedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received
    //
    // @Regex ^on discord message received$
    // @Switch for <bot>
    //
    // @Triggers when a Discord bot receives a message.
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
    // <context.message> returns the message (raw).
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message (mentions/etc. are written cleanly). CURRENTLY NON-FUNCTIONAL.
    // <context.author_id> returns the author's internal ID.
    // <context.author_name> returns the author's name.
    // <context.mentions> returns a list of all mentioned user IDs.
    // <context.mention_names> returns a list of all mentioned user names.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    //
    // -->

    public MessageCreateEvent getEvent() {
        return (MessageCreateEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord message received");
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("channel")) {
            return new Element(getEvent().getMessage().getChannelId().asLong());
        }
        else if (name.equals("channel_name")) {
            MessageChannel channel = getEvent().getMessage().getChannel().block();
            if (channel instanceof GuildChannel) {
                return new Element(((GuildChannel) channel).getName());
            }
        }
        else if (name.equals("group")) {
            if (getEvent().getGuildId().isPresent()) {
                return new Element(getEvent().getGuildId().get().asLong());
            }
        }
        else if (name.equals("group_name")) {
            if (getEvent().getGuildId().isPresent()) {
                return new Element(getEvent().getGuild().block().getName());
            }
        }
        else if (name.equals("message")) {
            return new Element(getEvent().getMessage().getContent().orElse(""));
        }
        else if (name.equals("no_mention_message")) {
            return new Element(stripMentions(getEvent().getMessage().getContent().orElse(""),
                    getEvent().getMessage().getUserMentions()));
        }
        else if (name.equals("formatted_message")) {
            return new Element(getEvent().getMessage().getContent().orElse(""));
        }
        else if (name.equals("author_id")) {
            return new Element(getEvent().getMessage().getAuthor().get().getId().asLong());
        }
        else if (name.equals("author_name")) {
            return new Element(getEvent().getMessage().getAuthor().get().getUsername());
        }
        else if (name.equals("mentions")) {
            dList list = new dList();
            for (Snowflake user : getEvent().getMessage().getUserMentionIds()) {
                list.add(String.valueOf(user.asLong()));
            }
            return list;
        }
        else if (name.equals("mention_names")) {
            dList list = new dList();
            for (User user : getEvent().getMessage().getUserMentions().toIterable()) {
                list.add(String.valueOf(user.getUsername()));
            }
            return list;
        }
        else if (name.equals("is_direct")) {
            return new Element(!(getEvent().getMessage().getChannel().block() instanceof GuildChannel));
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageReceived";
    }
}
