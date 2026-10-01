package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;

public class DiscordThreadArchivedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord thread archived
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch parent:<channel_id> to only process the event for a specific parent channel ID.
    //
    // @Triggers when a Discord thread is archived.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.thread> returns the thread DiscordChannelTag.
    // -->

    public static DiscordThreadArchivedScriptEvent instance;

    public DiscordThreadArchivedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord thread archived");
        registerSwitches("group", "parent");
    }

    public ChannelUpdateArchivedEvent getEvent() {
        return (ChannelUpdateArchivedEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        if (!tryChannel(path, ((ThreadChannel) getEvent().getChannel()).getParentChannel(), "parent")) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "group" -> new DiscordGroupTag(botID, getEvent().getGuild());
            case "thread" -> new DiscordChannelTag(botID, getEvent().getChannel());
            default -> super.getContext(name);
        };
    }
}
