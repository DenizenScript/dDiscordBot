package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordMessageDeletedScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import org.bukkit.Bukkit;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;

public class DiscordMessageDelete implements IListener<MessageDeleteEvent> {

    public String botID;

    @EventSubscriber
    public void handle(MessageDeleteEvent messageDeleteEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordMessageDeletedScriptEvent mdse = DiscordMessageDeletedScriptEvent.instance;
            if (!mdse.enabled) {
                return;
            }
            mdse.botID = botID;
            mdse.mre = messageDeleteEvent;
            mdse.cancelled = false;
            mdse.fire();
        });
    }
}
