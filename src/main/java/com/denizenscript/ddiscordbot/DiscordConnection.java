package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.events.DiscordMessageReceivedScriptEvent;
import org.bukkit.Bukkit;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class DiscordConnection implements IListener<MessageReceivedEvent> {

    public String botID;

    public IDiscordClient client;

    @Override
    public void handle(MessageReceivedEvent messageReceivedEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordMessageReceivedScriptEvent mrse = DiscordMessageReceivedScriptEvent.instance;
            if (!mrse.enabled) {
                return;
            }
            mrse.botID = botID;
            mrse.mre = messageReceivedEvent;
            mrse.cancelled = false;
            mrse.fire();
        });
    }
}
