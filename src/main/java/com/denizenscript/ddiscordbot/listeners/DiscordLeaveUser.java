package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordLeaveUserScriptEvent;
import com.denizenscript.ddiscordbot.dDiscordBot;
import org.bukkit.Bukkit;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;

public class DiscordLeaveUser implements IListener<UserLeaveEvent> {

    public String botID;

    @EventSubscriber
    public void handle(UserLeaveEvent userJoinEvent) {
        Bukkit.getScheduler().runTask(dDiscordBot.instance, () -> {
            DiscordLeaveUserScriptEvent mrse = DiscordLeaveUserScriptEvent.instance;
            mrse.botID = botID;
            mrse.mre = userJoinEvent;
            mrse.cancelled = false;
            mrse.fire();
        });
    }
}
