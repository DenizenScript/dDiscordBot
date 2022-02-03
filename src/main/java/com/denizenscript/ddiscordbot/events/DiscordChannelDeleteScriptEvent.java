package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

public class DiscordChannelDeleteScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord channel deleted
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord channel is created.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.channel> returns the DiscordChannelTag.
    // -->

    public static DiscordChannelDeleteScriptEvent instance;

    public DiscordChannelDeleteScriptEvent() {
        instance = this;
        registerCouldMatcher("discord channel deleted");
        registerSwitches("group");
    }

    public ChannelDeleteEvent getEvent() {
        return (ChannelDeleteEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild());
            case "channel":
                return new DiscordChannelTag(botID, getEvent().getChannel());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordChannelDeleted";
    }
}
