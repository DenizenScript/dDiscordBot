package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DiscordConnection extends ListenerAdapter {

    public String botID;

    public JDA client;

    public SavableMapFlagTracker flags;

    public CacheHelper cache = new CacheHelper();

    public Message getMessage(long channel, long message) {
        Message result = cache.getMessage(channel, message);
        if (result != null) {
            return result;
        }
        if (!DenizenDiscordBot.allowMessageRetrieval) {
            return null;
        }
        Channel chan = getChannel(channel);
        if (!(chan instanceof MessageChannel)) {
            return null;
        }
        return ((MessageChannel) chan).retrieveMessageById(message).complete();
    }

    public void registerHandlers() {
        client.addEventListener(this);
    }

    public Channel getChannel(long id) {
        Channel result = client.getGuildChannelById(id);
        if (result != null) {
            return result;
        }
        result = client.getPrivateChannelById(id);
        if (result != null) {
            return result;
        }
        return null;
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
        cache.onMessageReceived(event);
        autoHandle(event, DiscordMessageReceivedScriptEvent.instance);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        Message oldMessage = cache.getMessage(event.getChannel().getIdLong(), event.getMessageIdLong());
        cache.onMessageUpdate(event);
        autoHandle(event, DiscordMessageModifiedScriptEvent.instance, (e) -> {
            e.oldMessage = oldMessage;
        });
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        autoHandle(event, DiscordSlashCommandScriptEvent.instance);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        autoHandle(event, DiscordButtonClickedScriptEvent.instance);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        autoHandle(event, DiscordModalSubmittedScriptEvent.instance);
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        autoHandle(event, DiscordSelectionUsedScriptEvent.instance);
    }

    @Override
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {
        autoHandle(event, DiscordChannelCreateScriptEvent.instance);
    }

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        autoHandle(event, DiscordChannelDeleteScriptEvent.instance);
    }

    @Override
    public void onThreadRevealed(@Nonnull ThreadRevealedEvent event) {
        autoHandle(event, DiscordThreadRevealedScriptEvent.instance);
    }

    @Override
    public void onThreadHidden(@Nonnull ThreadHiddenEvent event) { // TODO: Is 'hidden' the same as 'archived'?
        autoHandle(event, DiscordThreadArchivedScriptEvent.instance);
    }

    public void autoHandle(Event event, DiscordScriptEvent scriptEvent) {
        autoHandle(event, scriptEvent, null);
    }

    public <T extends DiscordScriptEvent> void autoHandle(Event event, T scriptEvent, Consumer<T> configure) {
        Bukkit.getScheduler().runTask(DenizenDiscordBot.instance, () -> {
            if (!scriptEvent.enabled) {
                return;
            }
            if (configure != null) {
                configure.accept(scriptEvent);
            }
            scriptEvent.botID = botID;
            scriptEvent.event = event;
            scriptEvent.cancelled = false;
            scriptEvent.fire();
        });
    }
}
