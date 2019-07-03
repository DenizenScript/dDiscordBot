package com.denizenscript.ddiscordbot;

import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.User;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import reactor.core.publisher.Flux;


public abstract class DiscordScriptEvent extends ScriptEvent {

    public String botID;

    public Event event;

    @Override
    public boolean matches(ScriptPath path) {
        return path.checkSwitch("for", botID);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("bot")) {
            return new Element(botID);
        }
        else if (name.equals("self")) {
            return new Element(event.getClient().getSelf().block().getId().asLong());
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
