package com.denizenscript.ddiscordbot;

import com.denizenscript.ddiscordbot.objects.DiscordBotTag;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.User;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import reactor.core.publisher.Flux;

public abstract class DiscordScriptEvent extends ScriptEvent {

    public String botID;

    public Event event;

    @Override
    public boolean matches(ScriptPath path) {
        return path.checkSwitch("for", botID);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("bot")) {
            return new DiscordBotTag(botID);
        }
        return super.getContext(name);
    }

    public String stripMentions(String message, Flux<User> mentioned) {
        for (User user : mentioned.toIterable()) {
            message = message.replace(user.getMention(), "")
                    .replace("<@" +user.getId().asLong() + ">", "")
                    .replace("<@!" +user.getId().asLong() + ">", "");
        }
        return message;
    }

    public boolean enabled = false;

    public boolean isProperEvent() {
        return true;
    }

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }
}
