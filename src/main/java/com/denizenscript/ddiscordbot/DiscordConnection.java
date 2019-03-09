package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.*;
import org.bukkit.Bukkit;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;

public class DiscordConnection implements IListener {

    public String botID;

    public IDiscordClient client;

    public void autoHandle(Event event, DiscordScriptEvent scriptEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            if (!scriptEvent.enabled) {
                return;
            }
            scriptEvent.botID = botID;
            scriptEvent.event = event;
            scriptEvent.cancelled = false;
            scriptEvent.fire();
        });
    }

    @Override
    public void handle(Event event) {
        if (event instanceof MessageReceivedEvent) {
            autoHandle(event, DiscordMessageReceivedScriptEvent.instance);
        }
        else if (event instanceof MessageEditEvent) {
            autoHandle(event, DiscordMessageModifiedScriptEvent.instance);
        }
        else if (event instanceof MessageDeleteEvent) {
            autoHandle(event, DiscordMessageDeletedScriptEvent.instance);
        }
        else if (event instanceof UserJoinEvent) {
            autoHandle(event, DiscordUserJoinsScriptEvent.instance);
        }
        else if (event instanceof UserLeaveEvent) {
            autoHandle(event, DiscordUserLeavesScriptEvent.instance);
        }
    }
}
