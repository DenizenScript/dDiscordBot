package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;

import java.util.List;

public abstract class DiscordScriptEvent extends BukkitScriptEvent {

    public String botID;

    public Event event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("for", botID)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("bot")) {
            return new DiscordBotTag(botID);
        }
        return super.getContext(name);
    }

    public String stripMentions(String message, List<User> mentioned) {
        for (User user : mentioned) {
            message = message.replace(user.getAsMention(), "")
                    .replace("<@" +user.getId() + ">", "")
                    .replace("<@!" +user.getId() + ">", "");
        }
        return message;
    }

    public boolean enabled = false;

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }
}
