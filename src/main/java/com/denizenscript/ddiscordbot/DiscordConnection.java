package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.UserUpdateEvent;
import discord4j.core.event.domain.channel.*;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.Snowflake;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class DiscordConnection {

    public String botID;

    public GatewayDiscordClient client;

    public void registerHandlers() {
        EventDispatcher events = client.getEventDispatcher();
        registerScriptEventHandlers(events);
        registerCacheTrackers(events);
    }

    public void registerScriptEventHandlers(EventDispatcher events) {
        events.on(MessageCreateEvent.class).subscribe(event ->
                autoHandle(event, DiscordMessageReceivedScriptEvent.instance));
        events.on(MessageUpdateEvent.class).subscribe(event ->
                autoHandle(event, DiscordMessageModifiedScriptEvent.instance));
        events.on(MessageDeleteEvent.class).subscribe(event ->
                autoHandle(event, DiscordMessageDeletedScriptEvent.instance));
        events.on(MemberJoinEvent.class).subscribe(event ->
                autoHandle(event, DiscordUserJoinsScriptEvent.instance));
        events.on(MemberLeaveEvent.class).subscribe(event ->
                autoHandle(event, DiscordUserLeavesScriptEvent.instance));
        events.on(MemberUpdateEvent.class).subscribe(event ->
                autoHandle(event, DiscordUserRoleChangeScriptEvent.instance));
    }

    public void autoHandle(Event event, DiscordScriptEvent scriptEvent) {
        Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, () -> {
            if (!scriptEvent.enabled) {
                return;
            }
            scriptEvent.botID = botID;
            scriptEvent.event = event;
            scriptEvent.cancelled = false;
            scriptEvent.fire();
        });
    }

    public static class UserCache {
        public long id;
        public String username;
        public String discriminator;
        public UserCache(Member member) {
            id = member.getId().asLong();
            username = member.getUsername();
            discriminator = member.getDiscriminator();
        }
    }

    public static class ChannelCache {
        public long id;
        public String name;
        public long guildId;
        public ChannelCache(GuildChannel channel) {
            id = channel.getId().asLong();
            name = channel.getName();
            guildId = channel.getGuildId().asLong();
        }
    }

    public static class GuildCache {
        public long id;
        public String name;
        public ArrayList<ChannelCache> channels = new ArrayList<>();
        public ArrayList<UserCache> users = new ArrayList<>();
    }

    public ArrayList<GuildCache> guildsCached = new ArrayList<>();

    public void uncacheGuild(long id) {
        synchronized (this) {
            for (int i = 0; i < guildsCached.size(); i++) {
                if (guildsCached.get(i).id == id) {
                    guildsCached.remove(i);
                    return;
                }
            }
        }
    }

    public GuildCache cacheGuild(Guild guild) {
        uncacheGuild(guild.getId().asLong());
        GuildCache cache = new GuildCache();
        cache.id = guild.getId().asLong();
        cache.name = guild.getName();
        for (GuildChannel channel : guild.getChannels().toIterable()) {
            cache.channels.add(new ChannelCache(channel));
        }
        for (Member member : guild.getMembers().toIterable()) {
            cache.users.add(new UserCache(member));
        }
        synchronized (this) {
            guildsCached.add(cache);
        }
        return cache;
    }

    public GuildCache getCachedGuild(long id) {
        for (GuildCache cache : guildsCached) {
            if (cache.id == id) {
                return cache;
            }
        }
        Guild guild = client.getGuildById(Snowflake.of(id)).block();
        if (guild == null) {
            return null;
        }
        return cacheGuild(guild);
    }

    public void newMember(long guild, Member member) {
        GuildCache cache = getCachedGuild(guild);
        synchronized (this) {
            cache.users.add(new UserCache(member));
        }
    }

    public void memberLeave(long guild, long userId) {
        GuildCache cache = getCachedGuild(guild);
        synchronized (this) {
            for (int i = 0; i < cache.users.size(); i++) {
                if (cache.users.get(i).id == userId) {
                    cache.users.remove(i);
                    return;
                }
            }
        }
    }

    public void userUpdate(User user) {
        long id = user.getId().asLong();
        for (GuildCache cache : guildsCached) {
            for (UserCache userCache : cache.users) {
                if (userCache.id == id) {
                    userCache.username = user.getUsername();
                    userCache.discriminator = user.getDiscriminator();
                }
            }
        }
    }

    public void uncacheChannel(GuildCache cache, long channelId) {
        synchronized (this) {
            for (int i = 0; i < cache.channels.size(); i++) {
                if (cache.channels.get(i).id == channelId) {
                    cache.channels.remove(i);
                    return;
                }
            }
        }
    }

    public void channelCreate(Channel channel) {
        if (!(channel instanceof GuildChannel)) {
            return;
        }
        GuildCache cache = getCachedGuild(((GuildChannel) channel).getGuildId().asLong());
        uncacheChannel(cache, channel.getId().asLong());
        synchronized (this) {
            cache.channels.add(new ChannelCache((GuildChannel) channel));
        }
    }

    public void channelDelete(long guildId, long channelId) {
        GuildCache cache = getCachedGuild(guildId);
        uncacheChannel(cache, channelId);
    }

    public void registerCacheTrackers(EventDispatcher events) {
        events.on(GuildCreateEvent.class).subscribe(event -> cacheGuild(event.getGuild()));
        events.on(GuildUpdateEvent.class).subscribe(event -> cacheGuild(event.getCurrent()));
        events.on(GuildDeleteEvent.class).subscribe(event -> uncacheGuild(event.getGuildId().asLong()));
        events.on(MemberJoinEvent.class).subscribe(event -> newMember(event.getGuildId().asLong(), event.getMember()));
        events.on(MemberLeaveEvent.class).subscribe(event -> memberLeave(event.getGuildId().asLong(), event.getUser().getId().asLong()));
        events.on(UserUpdateEvent.class).subscribe(event -> userUpdate(event.getCurrent()));
        events.on(TextChannelCreateEvent.class).subscribe(event -> channelCreate(event.getChannel()));
        events.on(VoiceChannelCreateEvent.class).subscribe(event -> channelCreate(event.getChannel()));
        events.on(TextChannelUpdateEvent.class).subscribe(event -> channelCreate(event.getCurrent()));
        events.on(VoiceChannelUpdateEvent.class).subscribe(event -> channelCreate(event.getCurrent()));
        events.on(TextChannelDeleteEvent.class).subscribe(event -> channelDelete(event.getChannel().getGuildId().asLong(), event.getChannel().getId().asLong()));
        events.on(VoiceChannelDeleteEvent.class).subscribe(event -> channelDelete(event.getChannel().getGuildId().asLong(), event.getChannel().getId().asLong()));
    }
}
