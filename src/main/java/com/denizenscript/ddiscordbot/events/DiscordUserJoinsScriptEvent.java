package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class DiscordUserJoinsScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord user joins
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
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.user> returns the DiscordUserTag.
    // -->

    public static DiscordUserJoinsScriptEvent instance;

    public DiscordUserJoinsScriptEvent() {
        instance = this;
        registerCouldMatcher("discord user joins");
        registerSwitches("group");
    }

    public GuildMemberJoinEvent getEvent() {
        return (GuildMemberJoinEvent) event;
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
            case "user":
                return new DiscordUserTag(botID, getEvent().getUser());
        }
        return super.getContext(name);
    }
}
