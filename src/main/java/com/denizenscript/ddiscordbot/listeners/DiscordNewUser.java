package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordUserJoinsScriptEvent;
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
            DiscordUserJoinsScriptEvent nuse = DiscordUserJoinsScriptEvent.instance;
            if (!nuse.enabled) {
                return;
            }
            nuse.botID = botID;
            nuse.mre = userJoinEvent;
            nuse.cancelled = false;
            nuse.fire();
        });
    }
}
