package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class DiscordUserJoinsScriptEvent extends DiscordScriptEvent {

    public static DiscordUserJoinsScriptEvent instance;

    // <--[event]
    // @Events
    // discord user joins
    //
    // @Regex ^on discord user join$
    // @Switch for <bot>
    //
    // @Triggers when a Discord user joins a guild.
    //
    // @Plugin dDiscordBot
    //
    // @Context
    // <context.bot> returns the Denizen ID of the bot.
    // <context.self> returns the bots own Discord user ID.
    // <context.group> returns the group ID.
    // <context.group_name> returns the group name.
    // <context.user_id> returns the user's internal ID.
    // <context.user_name> returns the user's name.
    // -->

    public MemberJoinEvent getEvent() {
        return (MemberJoinEvent) event;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord user joins");
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("group")) {
            return new Element(getEvent().getGuildId().asLong());
        }
        else if (name.equals("group_name")) {
            return new Element(getEvent().getGuild().block().getName());
        }
        else if (name.equals("user_id")) {
            return new Element(getEvent().getMember().getId().asLong());
        }
        else if (name.equals("user_name")) {
            return new Element(getEvent().getMember().getUsername());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserJoins";
    }
}
