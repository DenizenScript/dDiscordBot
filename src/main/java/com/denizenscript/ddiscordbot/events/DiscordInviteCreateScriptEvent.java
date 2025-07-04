package com.denizenscript.ddiscordbot.events;

import com.denizenscript.ddiscordbot.DiscordScriptEvent;
import com.denizenscript.ddiscordbot.objects.DiscordChannelTag;
import com.denizenscript.ddiscordbot.objects.DiscordGroupTag;
import com.denizenscript.ddiscordbot.objects.DiscordUserTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;

public class DiscordInviteCreateScriptEvent extends DiscordScriptEvent {

    // <--[event]
    // @Events
    // discord invitation created
    //
    // @Switch for:<bot> to only process the event for a specified Discord bot.
    // @Switch channel:<channel_id> to only process the event for a specified Discord channel.
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
    // <context.group> returns the DiscordGroupTag of the created invitation.
    // <context.channel> returns the DiscordChannelTag of the created invitation.
    // <context.user> returns the DiscordUserTag of the invitation creator.
    // <context.code> returns the invitation code (after the last "/" in the URL).
    // <context.url> returns the invitation URL.
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
        if (!tryChannel(path, getEvent().getChannel())) {
            return false;
        }
        if (!tryGuild(path, getEvent().getGuild())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "channel" -> new DiscordChannelTag(botID, getEvent().getChannel());
            case "group" -> new DiscordGroupTag(botID, getEvent().getGuild());
            case "user" -> new DiscordUserTag(botID, getEvent().getInvite().getInviter());
            case "code" -> new ElementTag(getEvent().getInvite().getCode(), true);
            case "url" -> new ElementTag(getEvent().getInvite().getUrl(), true);
            default -> super.getContext(name);
        };
    }
}

