package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class DiscordConnection extends ListenerAdapter {

    public String botID;

    public JDA client;

    public void registerHandlers() {
        client.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        autoHandle(event, DiscordMessageReceivedScriptEvent.instance);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        autoHandle(event, DiscordMessageModifiedScriptEvent.instance);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        autoHandle(event, DiscordMessageDeletedScriptEvent.instance);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        autoHandle(event, DiscordUserJoinsScriptEvent.instance);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        autoHandle(event, DiscordUserLeavesScriptEvent.instance);
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        autoHandle(event, DiscordUserRoleChangeScriptEvent.instance);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        autoHandle(event, DiscordUserRoleChangeScriptEvent.instance);
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
