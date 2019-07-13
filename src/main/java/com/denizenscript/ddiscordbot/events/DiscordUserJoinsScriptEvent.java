package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class DiscordUserJoinsScriptEvent extends DiscordScriptEvent {

    public static DiscordUserJoinsScriptEvent instance;

    // <--[event]
    // @Events
    // discord user joins
    //
    // @Regex ^on discord user join$
    //
    // @Switch for <bot>
    // @Switch group <group_id>
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
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", String.valueOf(getEvent().getGuildId().asLong()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("discord user joins");
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("group")) {
            return new ElementTag(getEvent().getGuildId().asLong());
        }
        else if (name.equals("group_name")) {
            return new ElementTag(getEvent().getGuild().block().getName());
        }
        else if (name.equals("user_id")) {
            return new ElementTag(getEvent().getMember().getId().asLong());
        }
        else if (name.equals("user_name")) {
            return new ElementTag(getEvent().getMember().getUsername());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserJoins";
    }
}
