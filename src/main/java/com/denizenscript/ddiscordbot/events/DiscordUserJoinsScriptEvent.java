package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class DiscordUserJoinsScriptEvent extends DiscordScriptEvent {

    public static DiscordUserJoinsScriptEvent instance;

    // <--[event]
    // @Events
    // discord user joins
    //
    // @Regex ^on discord user joins$
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user joins a guild.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant Discord bot object.
    // <context.group> returns the group.
    // <context.user> returns the user.
    // -->

    public GuildMemberJoinEvent getEvent() {
        return (GuildMemberJoinEvent) event;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.checkSwitch("group", getEvent().getGuild().getId())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("discord user joins");
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild());
            case "user":
                return new DiscordUserTag(botID, getEvent().getUser());
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "DiscordUserJoins";
    }
}
