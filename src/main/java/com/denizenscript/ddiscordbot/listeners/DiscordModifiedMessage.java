package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordMessageModifiedScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import org.bukkit.Bukkit;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent;

public class DiscordModifiedMessage implements IListener<MessageEditEvent> {

    public String botID;

    @EventSubscriber
    public void handle(MessageEditEvent messageEditEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordMessageModifiedScriptEvent mese = DiscordMessageModifiedScriptEvent.instance;
            if (!mese.enabled) {
                return;
            }
            mese.botID = botID;
            mese.mre = messageEditEvent;
            mese.cancelled = false;
            mese.fire();
        });
    }
}
