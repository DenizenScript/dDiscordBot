package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordMessageDeleteScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import org.bukkit.Bukkit;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;

public class DiscordDeleteMessage implements IListener<MessageDeleteEvent> {

    public String botID;

    @EventSubscriber
    public void handle(MessageDeleteEvent messageDeleteEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordMessageDeleteScriptEvent mrse = DiscordMessageDeleteScriptEvent.instance;
            mrse.botID = botID;
            mrse.mre = messageDeleteEvent;
            mrse.cancelled = false;
            mrse.fire();
        });
    }
}
