package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import java.util.Iterator;
import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.User;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.common.util.Snowflake;

public class DiscordMessageReceivedScriptEvent extends DiscordScriptEvent {

    public static DiscordMessageReceivedScriptEvent instance;

    // <--[event]
    // @Events
    // discord message received
    //
    // @Regex ^on discord message received$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event when it occurs in a specified Discord channel.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord bot receives a message.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.channel> returns the channel.
    // <context.group> returns the group.
    // <context.message> returns the message (raw).
    // <context.message_id> returns the message ID.
    // <context.no_mention_message> returns the message with all user mentions stripped.
    // <context.formatted_message> returns the formatted message (mentions/etc. are written cleanly).
    // <context.attachments> returns a list with URLs for all attachments, returns null if no attachments.
    // <context.urls> returns a list of all URLs in the message, returns null if the message contains no URLs.
    // <context.author> returns the user that authored the message (may not be valid, eg for a Webhook post).
    // <context.mentions> returns a list of all mentioned users.
    // <context.is_direct> returns whether the message was sent directly to the bot (if false, the message was sent to a public channel).
    //
    // -->

    public MessageCreateEvent getEvent() {
        return (MessageCreateEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord message received");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("channel", String.valueOf(getEvent().getMessage().getChannelId().asLong()))) {
            return false;
        }
        if (getEvent().getGuildId().isPresent() && !path.checkSwitch("group", String.valueOf(getEvent().getGuildId().get().asLong()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("channel")) {
            return new DiscordChannelTag(botID, getEvent().getMessage().getChannelId().asLong());
        }
        else if (name.equals("group")) {
            if (getEvent().getGuildId().isPresent()) {
                return new DiscordGroupTag(botID, getEvent().getGuildId().get().asLong());
            }
        }
        else if (name.equals("message")) {
            return new ElementTag(getEvent().getMessage().getContent());
        }
        else if (name.equals("message_id")) {
            return new ElementTag(getEvent().getMessage().getId().asString());
        }
        else if (name.equals("no_mention_message")) {
            return new ElementTag(stripMentions(getEvent().getMessage().getContent(),
                    getEvent().getMessage().getUserMentions()));
        }
        else if (name.equals("formatted_message")) {
            // NEW
            String m = getEvent().getMessage().getContent();
            Iterator<User> it = getEvent().getMessage().getUserMentions().toIterable().iterator();
            // replacing usernames
            while (it.hasNext()) {
                User u = it.next();
                m = m.replaceAll("(<[^<>$]!?" + u.getId().asString() + ">)", "@" + u.getUsername());
            }
            // replacing channel names
            for (String s : m.split("\\s+")) {
                if (s.matches("(^<#[0-9]{18}>$)")) {
                    String str = s.replace("<#", "").replace(">", "");
                    m = m.replaceAll("(<[^<>$]" + str + ">)", "#" + ((GuildChannel) DenizenDiscordBot.instance.connections.get(botID).client.getChannelById(Snowflake.of(str)).block()).getName().replaceAll("[^a-zA-Z-]", ""));
                }
            }
            // replacing role names
            for (String s : m.split("\\s+")) {
                if (s.matches("(^<@&[0-9]{18}>$)")) {
                    String str = s.replace("<@&", "").replace(">", "");
                    m = m.replaceAll("(<@[^<>$]" + str + ">)", "@" + (DenizenDiscordBot.instance.connections.get(botID).client.getRoleById(getEvent().getGuildId().get(), Snowflake.of(str)).block()).getName().replaceAll("[^\\Wa-zA-Z-]", ""));
                }
            }
            return new ElementTag(m);
        }
        else if (name.equals("attachments")) {
            // ALSO NEW
            if (getEvent().getMessage().getAttachments().size() != 0) {
                ListTag list = new ListTag();
                for (Attachment att : getEvent().getMessage().getAttachments()) {
                    list.addObject(new ElementTag(att.getUrl()));
                }
                return list;
            }
        }
        else if (name.equals("urls")) {
            ListTag list = new ListTag();
            for (String s : getEvent().getMessage().getContent().split("\\s+")) {
                try {
                    new java.net.URI(s);
                    list.addObject(new ElementTag(s));
                }
                catch (Exception e) {}
            }
            if(list.size() != 0) {
                return list;
            }
        }
        else if (name.equals("author")) {
            if (!getEvent().getMessage().getAuthor().isPresent()) {
                return null;
        }
            return new DiscordUserTag(botID, getEvent().getMessage().getAuthor().get());
        }
        else if (name.equals("mentions")) {
            ListTag list = new ListTag();
            for (Snowflake user : getEvent().getMessage().getUserMentionIds()) {
                list.addObject(new DiscordUserTag(botID, user.asLong()));
            }
            return list;
        }
        else if (name.equals("is_direct")) {
            return new ElementTag(!(getEvent().getMessage().getChannel().block() instanceof GuildChannel));
        }
        else if (name.equals("channel_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            MessageChannel channel = getEvent().getMessage().getChannel().block();
            if (channel instanceof GuildChannel) {
                return new ElementTag(((GuildChannel) channel).getName());
            }
        }
        else if (name.equals("mention_names")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            ListTag list = new ListTag();
            for (User user : getEvent().getMessage().getUserMentions().toIterable()) {
                list.add(String.valueOf(user.getUsername()));
            }
            return list;
        }
        else if (name.equals("group_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            if (getEvent().getGuildId().isPresent()) {
                return new ElementTag(getEvent().getGuild().block().getName());
            }
        }
        else if (name.equals("author_id")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMessage().getAuthor().get().getId().asLong());
        }
        else if (name.equals("author_name")) {
            DenizenDiscordBot.userContextDeprecation.warn();
            return new ElementTag(getEvent().getMessage().getAuthor().get().getUsername());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordMessageReceived";
    }
}
