package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class DiscordConnection extends ListenerAdapter {

    public String botID;

    public JDA client;

    public SavableMapFlagTracker flags;

    public void registerHandlers() {
        client.addEventListener(this);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        autoHandle(event, DiscordMessageReactionAddScriptEvent.instance);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        autoHandle(event, DiscordMessageReactionRemoveScriptEvent.instance);
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

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        autoHandle(event, DiscordUserNicknameChangeScriptEvent.instance);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        autoHandle(event, DiscordSlashCommandScriptEvent.instance);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        autoHandle(event, DiscordButtonClickedScriptEvent.instance);
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        autoHandle(event, DiscordSelectionUsedScriptEvent.instance);
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
