package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;

public class DiscordChannelCreateScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord channel created
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch type:<type> to only process the event if the channel is a specific channel_type.
    //
    // @Triggers when a Discord channel is created.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.channel> returns the new channel.
    // -->

    public static DiscordChannelCreateScriptEvent instance;

    public DiscordChannelCreateScriptEvent() {
        instance = this;
        registerCouldMatcher("discord channel created");
        registerSwitches("group", "type");
    }

    public ChannelCreateEvent getEvent() {
        return (ChannelCreateEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "type", getEvent().getChannelType().name())) {
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
        return "DiscordChannelCreated";
    }
}
