package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordNewUserScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import org.bukkit.Bukkit;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;

public class DiscordNewUser implements IListener<UserJoinEvent> {

    public String botID;

    @EventSubscriber
    public void handle(UserJoinEvent userJoinEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordNewUserScriptEvent mrse = DiscordNewUserScriptEvent.instance;
            mrse.botID = botID;
            mrse.mre = userJoinEvent;
            mrse.cancelled = false;
            mrse.fire();
        });
    }
}
