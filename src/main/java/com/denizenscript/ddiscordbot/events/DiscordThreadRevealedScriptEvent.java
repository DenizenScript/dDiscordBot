package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;

public class DiscordThreadRevealedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord thread revealed
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch parent:<channel_id> to only process the event for a specific parent channel ID.
    //
    // @Triggers when a Discord thread is pulled out of archive.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.thread> returns the thread channel.
    // -->

    public static DiscordThreadRevealedScriptEvent instance;

    public DiscordThreadRevealedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord thread revealed");
        registerSwitches("group", "parent");
    }

    public ThreadRevealedEvent getEvent() {
        return (ThreadRevealedEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        if (!tryChannel(path, getEvent().getThread().getParentChannel(), "parent")) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild());
            case "thread":
                return new DiscordChannelTag(botID, getEvent().getThread());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordThreadRevealed";
    }
}
