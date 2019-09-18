package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import org.bukkit.Bukkit;

public class DiscordConnection {

    public String botID;

    public DiscordClient client;

    public void registerHandlers() {
        EventDispatcher events = client.getEventDispatcher();
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
}
