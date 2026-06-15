package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DenizenDiscordBot;
import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;

public class DiscordThreadArchivedScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord thread archive status changes
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    // @Switch parent:<channel_id> to only process the event for a specific parent channel ID.
    //
    // @Triggers when a Discord thread's archive status changes.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.thread> returns the thread DiscordChannelTag.
    // <context.old_state> returns whether the thread was previously archived.
    // <context.new_state> returns whether the thread is now archived.
    // -->

    public static DiscordThreadArchivedScriptEvent instance;

    public DiscordThreadArchivedScriptEvent() {
        instance = this;
        registerCouldMatcher("discord thread archive status changes");
        registerCouldMatcher("discord thread archived|revealed"); // Backwards compat
        registerSwitches("group", "parent");
    }

    public ChannelUpdateArchivedEvent getEvent() {
        return (ChannelUpdateArchivedEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String type = path.eventArgLowerAt(2);
        if (type.equals("archived") || type.equals("revealed")) {
            DenizenDiscordBot.threadArchiveEvent.warn();
            if (type.equals("archived") && !getEvent().getNewValue()) {
                return false;
            }
            else if (type.equals("revealed") && getEvent().getNewValue()) {
                return false;
            }
        }
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
            case "old_state" -> getEvent().getOldValue() != null ? new ElementTag(getEvent().getOldValue()) : null;
            case "new_state" -> getEvent().getNewValue() != null ? new ElementTag(getEvent().getNewValue()) : null;
            default -> super.getContext(name);
        };
    }
}
