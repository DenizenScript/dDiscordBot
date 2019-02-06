package com.denizenscript.ddiscordbot.listeners;

import com.denizenscript.ddiscordbot.events.DiscordUserLeavesScriptEvent;
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
            DiscordUserLeavesScriptEvent luse = DiscordUserLeavesScriptEvent.instance;
            if (!luse.enabled) {
                return;
            }
            luse.botID = botID;
            luse.mre = userJoinEvent;
            luse.cancelled = false;
            luse.fire();
        });
    }
}
