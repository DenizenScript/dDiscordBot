package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class DiscordInviteCreateScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord invitation created
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch group:<group_id> to only process the event for a specified Discord group.
    //
    // @Triggers when a Discord user creates an invitation.
    //
    // @Plugin dDiscordBot
    //
    // @Group Discord
    //
    // @Context
    // <context.bot> returns the relevant DiscordBotTag.
    // <context.group> returns the DiscordGroupTag.
    // <context.channel> returns the DiscordChannelTag.
    // <context.user> returns the DiscordUserTag of the invitation creator.
    // <context.code> returns the ElementTag of the invitation code (after the "/" in the URL.
    // <context.url> returns the ElementTag of the invitation URL
    // -->

    public static DiscordInviteCreateScriptEvent instance;

    public DiscordInviteCreateScriptEvent() {
        instance = this;
        registerCouldMatcher("discord invitation created");
        registerSwitches("channel", "group");
    }

    public GuildInviteCreateEvent getEvent() {
        return (GuildInviteCreateEvent) event;
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
            case "channel":
                return new DiscordChannelTag(botID, getEvent().getChannel());
            case "group":
                return new DiscordGroupTag(botID, getEvent().getGuild());
            case "user":
                return new DiscordUserTag(botID, getEvent().getInvite().getInviter());
            case "code":
                return new ElementTag(getEvent().getInvite().getCode());
            case "url":
                return new ElementTag(getEvent().getInvite().getUrl());
        }
        return super.getContext(name);
    }
}
